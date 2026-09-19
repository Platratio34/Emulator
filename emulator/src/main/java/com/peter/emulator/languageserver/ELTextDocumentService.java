package com.peter.emulator.languageserver;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.peter.emulator.lang.ELAnalysisError;
import com.peter.emulator.lang.ELSymbol;
import com.peter.emulator.lang.ELSymbol.ELVarSymbol;
import com.peter.emulator.lang.ELSymbol.Modifier;
import com.peter.emulator.lang.ProgramUnit;

public class ELTextDocumentService implements TextDocumentService {

    public final ELLanguageServer lspServer;

    public ELTextDocumentService(ELLanguageServer lspServer) {
        this.lspServer = lspServer;

    }
    
    protected boolean changed = false;
    protected Thread waitThread = null;

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        VersionedTextDocumentIdentifier textDocument = params.getTextDocument();
        Path path = Path.of(URI.create(textDocument.getUri()));
        lspServer.logDebug("Change for " + path);
        lspServer.getFileProvider().addCachedFile(path, params.getContentChanges().getFirst().getText());
        if (waitThread == null) {
            waitThread = new Thread(this::changeLoop);
            waitThread.run();
        }
        changed = true;
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
        lspServer.logDebug("Save for "+params.getTextDocument().getUri());
        lspServer.getFileProvider().clearCachedFile(path);
    }
    
    @Override
    public CompletableFuture<DocumentDiagnosticReport> diagnostic(DocumentDiagnosticParams params) {
        return CompletableFuture.supplyAsync(() -> {
            URI uri = URI.create(params.getTextDocument().getUri());
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
    
    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        return CompletableFuture.supplyAsync(() -> {
            URI uri = URI.create(params.getTextDocument().getUri());
            ProgramUnit unit = lspServer.getUnit(uri);
            if (unit == null) {
                lspServer.logError("Hover was requested for %s, but no program unit could be found", uri);
                return null;
            }
            Position hoverPos = params.getPosition();

            for (ELSymbol symbol : unit.symbols) {
                if (symbol.hasText() && symbol.contains(hoverPos, null)) {
                    return new Hover(new MarkupContent("markdown", symbol.getText()));
                } else {
                    // lspServer.logDebug("Hover was requested for %s, but didn't match symbol "+symbol.type+": "+symbol.text, uri);
                }
            }

            if (unit.variables.isEmpty() && unit.functions.isEmpty() && unit.symbols.isEmpty()) {
                lspServer.logWarn("Hover was requested for %s, but program unit had no hover-able symbols", uri);
                return null;
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
        ProgramUnit unit = lspServer.getUnit(uri);
        if (unit == null) {
            lspServer.logError("Semantic tokens were requested for %s, but no program unit could be found", uri);
            return null;
        }
        return CompletableFuture.supplyAsync(() -> {
            SemanticTokenState state = new SemanticTokenState();
            state.addTokens(unit.symbols);
            lspServer.logDebug("Providing semantic tokens for %s (%d total symbols)", uri, unit.symbols.size());

            return new SemanticTokens(state.data);
        });
    }

}
