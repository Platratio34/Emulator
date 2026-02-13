package com.peter.emulator.languageserver;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.peter.emulator.lang.ELAnalysisError;

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
                if(!err.span.start().file().equals(p))
                    continue;
                diagnostics.add(new Diagnostic(err.span.toRange(), err.reason, err.severity.severity, "emulatorlang"));
            }
            return new DocumentDiagnosticReport(new RelatedFullDocumentDiagnosticReport(diagnostics));
        });
    }

}
