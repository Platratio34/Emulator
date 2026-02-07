package com.peter.emulator.languageserver;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentDiagnosticParams;
import org.eclipse.lsp4j.DocumentDiagnosticReport;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.RelatedFullDocumentDiagnosticReport;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.peter.Main;
import com.peter.emulator.lang.ELAnalysisError;
import com.peter.emulator.lang.ErrorSet;
import com.peter.emulator.lang.LanguageServer;
import com.peter.emulator.lang.ProgramModule;

public class ELTextDocumentService implements TextDocumentService {

    public final ELLanguageServer lspServer;
    protected final LanguageServer ls;

    public ELTextDocumentService(ELLanguageServer lspServer) {
        this.lspServer = lspServer;
        ls = new LanguageServer();

        // ProgramModule kernal = ls.addModule("Kernal");
        // kernal.addRefModule("SysD");
        // kernal.addFiles(Main.ROOT_PATH.resolve("lang/Kernal"));

        // ProgramModule system = ls.addModule("System");
        // system.addRefModule("SysD");
        // system.addRefModule("Kernal");
        // system.addFiles(Main.ROOT_PATH.resolve("lang/System"));
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        lspServer.client.logMessage(new MessageParams(MessageType.Info, "Change for "+params.getTextDocument().getUri()));
        
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        lspServer.client.logMessage(new MessageParams(MessageType.Info, "Close for "+params.getTextDocument().getUri()));
        
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        lspServer.client.logMessage(new MessageParams(MessageType.Info, "Open for "+params.getTextDocument().getUri()));
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        lspServer.client.logMessage(new MessageParams(MessageType.Info, "Save for "+params.getTextDocument().getUri()));
    }
    
    @Override
    public CompletableFuture<DocumentDiagnosticReport> diagnostic(DocumentDiagnosticParams params) {
        return CompletableFuture.supplyAsync(() -> {
            LanguageServer ls = new LanguageServer();
            ProgramModule m = ls.addModule("temp");
            URI uri = URI.create(params.getTextDocument().getUri());
            lspServer.client.logMessage(new MessageParams(MessageType.Info, "Async diagnostics for "+uri));
            m.addFile(Path.of(uri));
            ErrorSet errors = ls.recompile();
            ArrayList<Diagnostic> diagnostics = new ArrayList<>();
            // diagnostics.add(new Diagnostic(new Range(new Position(1, 1), new Position(1, 1)), "Test"));
            for (ELAnalysisError err : errors) {
                if (err.span == null) {
                    continue;
                }
                diagnostics.add(new Diagnostic(err.span.toRange(), err.reason, err.severity.severity, "emulatorlang"));
            }
            return new DocumentDiagnosticReport(new RelatedFullDocumentDiagnosticReport(diagnostics));
        });
    }

}
