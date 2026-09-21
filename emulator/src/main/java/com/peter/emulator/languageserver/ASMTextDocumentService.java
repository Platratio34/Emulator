package com.peter.emulator.languageserver;

import java.net.URI;
import java.nio.file.Path;

import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.peter.emulator.lang.FileProvider;

public class ASMTextDocumentService /*implements TextDocumentService*/ {

    protected final FileProvider fileProvider = new FileProvider();

    // public final ELLanguageServer lspServer;
    
    // public ASMTextDocumentService(lsp)

    protected boolean changed = false;
    protected Thread waitThread = null;

    // @Override
    // public void didChange(DidChangeTextDocumentParams params) {
    //     VersionedTextDocumentIdentifier textDocument = params.getTextDocument();
    //     Path path = Path.of(URI.create(textDocument.getUri()));
    //     lspServer.logDebug("Change for " + path);
    //     lspServer.getFileProvider().addCachedFile(path, params.getContentChanges().getFirst().getText());
    //     if (waitThread == null) {
    //         waitThread = new Thread(this::changeLoop);
    //         waitThread.run();
    //     }
    //     changed = true;
    // }
    
    // private void changeLoop() {
    //     while (true) {
    //         try {
    //             Thread.sleep(1000);
    //         } catch (InterruptedException e) {
    //             lspServer.logError("Change loop interrupted: %s", e.toString());
    //             break;
    //         }
    //         if (!changed) {
    //             break;
    //         }
    //         lspServer.triggerDiagnostics();
    //         changed = false;
    //     }
    //     waitThread = null;
    // }

    // @Override
    // public void didClose(DidCloseTextDocumentParams params) {
    //     TextDocumentIdentifier textDocument = params.getTextDocument();
    //     Path path = Path.of(URI.create(textDocument.getUri()));
    //     lspServer.logDebug("Close for "+path);
    //     lspServer.getFileProvider().clearCachedFile(path);
    // }

    // @Override
    // public void didOpen(DidOpenTextDocumentParams params) {
    //     TextDocumentItem textDocument = params.getTextDocument();
    //     Path path = Path.of(URI.create(textDocument.getUri()));
    //     lspServer.logDebug("Open for " + path);
    //     lspServer.getFileProvider().addCachedFile(path, textDocument.getText());
    // }

    // @Override
    // public void didSave(DidSaveTextDocumentParams params) {
    //     TextDocumentIdentifier textDocument = params.getTextDocument();
    //     Path path = Path.of(URI.create(textDocument.getUri()));
    //     lspServer.logDebug("Save for "+params.getTextDocument().getUri());
    //     lspServer.getFileProvider().clearCachedFile(path);
    // }

}
