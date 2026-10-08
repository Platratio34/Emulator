package com.peter.emulator.languageserver;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.peter.emulator.assembly.ASMParser;
import com.peter.emulator.assembly.AsmError;
import com.peter.emulator.lang.ELAnalysisError;
import com.peter.emulator.lang.ELSymbol;
import com.peter.emulator.lang.ELSymbol.ELVarSymbol;
import com.peter.emulator.lang.ELSymbol.Modifier;
import com.peter.emulator.lang.FileProvider;
import com.peter.emulator.lang.ProgramUnit;

public class ELTextDocumentService implements TextDocumentService {

    public final ELLanguageServer lspServer;

    public final HashMap<Path, ASMParser> parsers = new HashMap<>();

    public ELTextDocumentService(ELLanguageServer lspServer) {
        this.lspServer = lspServer;

    }
    
    protected boolean changed = false;
    protected Thread waitThread = null;

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        VersionedTextDocumentIdentifier textDocument = params.getTextDocument();
        Path path = Path.of(URI.create(textDocument.getUri()));
        if (path.endsWith(".asm")) {
            parsers.remove(path);
        } else {
            lspServer.logDebug("Change for " + path);
            lspServer.getFileProvider().addCachedFile(path, params.getContentChanges().getFirst().getText());
            if (waitThread == null) {
                waitThread = new Thread(this::changeLoop);
                waitThread.run();
            }
            changed = true;
        }
    }
    
    private void changeLoop() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                lspServer.logError("Change loop interrupted: %s", e.toString());
                break;
            }
            if (!changed) {
                break;
            }
            lspServer.triggerDiagnostics();
            changed = false;
        }
        waitThread = null;
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        TextDocumentIdentifier textDocument = params.getTextDocument();
        Path path = Path.of(URI.create(textDocument.getUri()));
        lspServer.logDebug("Close for "+path);
        lspServer.getFileProvider().clearCachedFile(path);
        parsers.remove(path);
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        TextDocumentItem textDocument = params.getTextDocument();
        Path path = Path.of(URI.create(textDocument.getUri()));
        lspServer.logDebug("Open for " + path);
        lspServer.getFileProvider().addCachedFile(path, textDocument.getText());
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        TextDocumentIdentifier textDocument = params.getTextDocument();
        Path path = Path.of(URI.create(textDocument.getUri()));
        lspServer.logDebug("Save for " + params.getTextDocument().getUri());
        lspServer.getFileProvider().clearCachedFile(path);
        parsers.remove(path);
    }
   
    private ASMParser getParser(Path path) {
        if (parsers.containsKey(path)) {
            ASMParser parser = parsers.get(path);
            parser.parseLock.lock();
            parser.parseLock.unlock();
            return parser;
        } else {
            FileProvider fileProvider = lspServer.getFileProvider();
            try {
                ASMParser parser = new ASMParser(fileProvider, fileProvider.readFile(path),
                        new com.peter.emulator.lang.Location(path.toAbsolutePath().toString(), 1, 2));
                parsers.put(path, parser);
                parser.parse();
                lspServer.logInfo("New ASM parser for %s with %d symbols", path, parser.symbols.size());

                ArrayList<Diagnostic> diagnostics = new ArrayList<>();

                for (AsmError err : parser.errors) {
                    if (err.span == null) {
                        continue;
                    }
                    diagnostics
                            .add(new Diagnostic(err.span.toRange(), err.message, err.severity.severity, "emulatorasm"));
                }
                lspServer.client.publishDiagnostics(new PublishDiagnosticsParams(path.toString(), diagnostics));
                lspServer.client.refreshSemanticTokens();
                return parser;
            } catch (IOException e) {
                lspServer.logError("Error opening %s for ASM parsing", path);
                return null;
            }
        }
    }

    @Override
    public CompletableFuture<DocumentDiagnosticReport> diagnostic(DocumentDiagnosticParams params) {
        URI uri = URI.create(params.getTextDocument().getUri());
        Path path = Path.of(uri);
        if (uri.getPath().endsWith(".asm")) {
            return CompletableFuture.supplyAsync(() -> {
                ASMParser parser = getParser(path);
                if(parser == null)
                    return null;
                ArrayList<Diagnostic> diagnostics = new ArrayList<>();
                
                for (AsmError err : parser.errors) {
                    if (err.span == null) {
                        continue;
                    }
                    diagnostics.add(new Diagnostic(err.span.toRange(), err.message, err.severity.severity, "emulatorasm"));
                }
                return new DocumentDiagnosticReport(new RelatedFullDocumentDiagnosticReport(diagnostics));
            });
        } else if (uri.getPath().endsWith(".el")) {
            return CompletableFuture.supplyAsync(() -> {
                String p = Path.of(uri).toAbsolutePath().toString();
                lspServer.logDebug("Async diagnostics for %s", uri);

                ArrayList<Diagnostic> diagnostics = new ArrayList<>();
                lspServer.addFile(uri);
                if (lspServer.errors == null) {
                    lspServer.triggerDiagnostics();
                }
                for (ELAnalysisError err : lspServer.errors) {
                    if (err.span == null) {
                        continue;
                    }
                    if (!err.span.start().file().equals(p))
                        continue;
                    diagnostics.add(new Diagnostic(err.span.toRange(), err.reason, err.severity.severity, "emulatorlang"));
                }
                return new DocumentDiagnosticReport(new RelatedFullDocumentDiagnosticReport(diagnostics));
            });
        }
        return CompletableFuture.supplyAsync(() -> {
            return null;
        });
    }
    
    private ArrayList<ELSymbol> getSymbols(URI uri) {
        if (uri.getPath().endsWith(".asm")) {
            ASMParser parser = getParser(Path.of(uri));
                if(parser == null)
                    return null;
            return parser.symbols;
        } else if (uri.getPath().endsWith(".el")) {
            lspServer.lsLock.lock();
            ProgramUnit unit = lspServer.getUnit(uri);
            lspServer.lsLock.unlock();
            if (unit == null) {
                return null;
            }
            return unit.symbols;
        }
        return null;
    }
    
    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        URI uri = URI.create(params.getTextDocument().getUri());
        return CompletableFuture.supplyAsync(() -> {
            ArrayList<ELSymbol> symbols = getSymbols(uri);
            if(symbols == null) {
                lspServer.logError("Hover was requested for %s, but no symbols could be found", uri);
                return null;
            }
            Position hoverPos = params.getPosition();
            for (ELSymbol symbol : symbols) {
                if (symbol.hasText() && symbol.contains(hoverPos, null)) {
                    return new Hover(new MarkupContent("markdown", symbol.getText()));
                }
            }
            return null;
        });
    }

    private class SemanticTokenState {
        public final List<Integer> data = new ArrayList<>();
        public int lastLine = 0;
        public int lastChar = 0;

        public void addTokens(ArrayList<ELSymbol> symbols) {
            symbols.sort((a, b) -> {
                int aL = a.span.start().line();
                int bL = b.span.start().line();
                if (aL != bL) {
                    return aL - bL;
                }
                return a.span.start().col() - b.span.start().col();
            });
            for (ELSymbol symbol : symbols) {
                if (!symbol.isWrapper()) {
                    if(symbol.span.end() == null)
                        continue;
                    int type = symbol.type.semanticTypeIndex();
                    int modifier = symbol.getModifier();
                    if (symbol instanceof ELVarSymbol vs) {
                        if (vs.var.finalVal) {
                            modifier |= Modifier.READ_ONLY.value;
                        }
                        switch (vs.var.varType) {
                            case CONST -> modifier |= Modifier.READ_ONLY.value;
                            case STATIC -> modifier |= Modifier.STATIC.value;
                            case SCOPE -> {
                                if (vs.var.offset < 0) {
                                    type = ELSymbol.Type.PARAMETER.semanticTypeIndex();
                                }
                            }
                            case MEMBER -> {
                                type = ELSymbol.Type.PROPERTY.semanticTypeIndex();
                            }

                            default -> {
                            }
                        }
                    }

                    int line = symbol.span.start().line();
                    int startCol = symbol.span.start().col();
                    while (line <= symbol.span.end().line()) {
                        int lineOff = (line - 1) - lastLine;
                        int colOff = (startCol - 2) - ((lineOff != 0) ? 0 : lastChar);
                        lastLine = line - 1;
                        lastChar = startCol - 2;

                        int length = ( (line == symbol.span.end().line()) ? symbol.span.end().col() : 9999 ) - startCol + 1;

                        data.add(lineOff);
                        data.add(colOff);
                        data.add(length);
                        data.add(type);
                        data.add(modifier);

                        line++;
                        startCol = 2;
                    }
                }

                addTokens(symbol.getSub());
            }
    }

    }
    
    @Override
    public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
        URI uri = URI.create(params.getTextDocument().getUri());

        return CompletableFuture.supplyAsync(() -> {
            ArrayList<ELSymbol> symbols = getSymbols(uri);
            if (symbols == null) {
                lspServer.logError("Semantic tokens were requested for %s, but no symbols could be found",
                        uri);
                return null;
            }
            SemanticTokenState state = new SemanticTokenState();
            state.addTokens(symbols);
            lspServer.logDebug("Providing semantic tokens for %s (%d total symbols)", uri, symbols.size());
            return new SemanticTokens(state.data);
        });
    }
    
    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(
            DefinitionParams params) {
        URI uri = URI.create(params.getTextDocument().getUri());

        return CompletableFuture.supplyAsync(() -> {
            ArrayList<ELSymbol> symbols = getSymbols(uri);
            if (symbols == null) {
                lspServer.logError("Semantic tokens were requested for %s, but no symbols could be found",
                        uri);
                return null;
            }
            Position hoverPos = params.getPosition();
            for (ELSymbol symbol : symbols) {
                if (symbol.definition != null && symbol.contains(hoverPos, null)) {
                    // return new Hover(new MarkupContent("markdown", symbol.getText()));
                    ArrayList<LocationLink> locations = new ArrayList<>();
                    locations.add(symbol.definition.span.locationLink());
                    return Either.forRight(locations);
                }
            }
            return null;
        });
    }

}
