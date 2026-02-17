package com.peter.emulator.languageserver;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.peter.emulator.lang.ELAnalysisError;
import com.peter.emulator.lang.ELFunction;
import com.peter.emulator.lang.ELSymbol;
import com.peter.emulator.lang.ELVariable;
import com.peter.emulator.lang.ProgramUnit;
import com.peter.emulator.lang.annotations.ELAnnotation;

public class ELTextDocumentService implements TextDocumentService {

    public final ELLanguageServer lspServer;

    public ELTextDocumentService(ELLanguageServer lspServer) {
        this.lspServer = lspServer;
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        lspServer.logDebug("Change for "+params.getTextDocument().getUri());
        
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        lspServer.logDebug("Close for "+params.getTextDocument().getUri());
        
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        lspServer.logDebug("Open for "+params.getTextDocument().getUri());
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        lspServer.logDebug("Save for "+params.getTextDocument().getUri());
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
                if (symbol.contains(hoverPos, null)) {
                    return new Hover(new MarkupContent("markdown", symbol.text));
                } else {
                    // lspServer.logDebug("Hover was requested for %s, but didn't match symbol "+symbol.type+": "+symbol.text, uri);
                }
            }

            if (unit.variables.size() == 0 && unit.functions.size() == 0) {
                lspServer.logWarn("Hover was requested for %s, but program unit had no hover-able symbols", uri);
            }
            for (ELVariable var : unit.variables) {
                if (var.span().contains(hoverPos, null)) {
                    String content = switch(var.varType) {
                        case CONST -> String.format("### `%s const %s.%s`", var.protection.value, var.namespace.cName, var.name);
                        case STATIC -> String.format("### `%s static %s.%s`", var.protection.value, var.namespace.cName, var.name);
                        case MEMBER -> String.format("### `%s %s.%s`", var.protection.value, var.namespace.cName, var.name);
                        case SCOPE -> String.format("### `%s %s.%s` (Scope)", var.protection.value, var.namespace.cName, var.name);
                    };
                    content += "\n" + var.debugString();
                    return new Hover(new MarkupContent("markdown", content));
                }
            }
            for (ELFunction func : unit.functions) {
                if (func.startLocation.span(func.bodyLocation).contains(hoverPos, null)) {
                    String retStr = (func.ret == null ? "void" : func.ret.typeString());
                    String content = switch (func.type) {
                        case CONSTRUCTOR -> String.format("### `%s %s(%s)`", func.protection.value, func.namespace.cName, func.getParamString());
                        case DESTRUCTOR -> String.format("### `%s ~%s(%s)`", func.protection.value, func.namespace.cName, func.getParamString());
                        case INSTANCE -> String.format("### `%s %s %s.%s(%s)`", func.protection.value, retStr, func.namespace.cName, func.cName, func.getParamString());
                        case STATIC -> String.format("### `%s static %s %s.%s(%s)`", func.protection.value, retStr, func.namespace.cName, func.cName, func.getParamString());
                        case OPERATOR -> String.format("### `%s operator %s %s.%s(%s)`", func.protection.value, retStr, func.namespace.cName, func.cName, func.getParamString());
                    };
                    if(func.annotations != null)
                        for (ELAnnotation annotation : func.annotations) {
                            content += String.format("\n`%s`", annotation.debugString());
                        }
                    return new Hover(new MarkupContent("markdown", content));
                }
            }
            // for (ELClass var : unit.classes) {
            //     if (var.span().contains(hoverPos.getLine(), hoverPos.getCharacter())) {
            //         String content = "## " + var.cName;
            //         content += "\n" + var.debugString("");
            //         return new Hover(new MarkupContent("markdown", content));
            //     }
            // }
            return null;
        });
    }

}
