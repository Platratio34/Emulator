package com.peter.emulator.languageserver;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.DiagnosticRegistrationOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SetTraceParams;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import com.peter.emulator.lang.ErrorSet;

public class ELLanguageServer extends LSPServer implements LanguageServer, LanguageClientAware {

    public static final String LANGUAGE_ID = "emulatorlang";

    protected LanguageClient client;
    private ELTextDocumentService textDocumentService;
    private ELWorkspaceService workspaceService;
    protected ErrorSet errors = null;

    @Override
    public void connect(LanguageClient client) {
        this.client = client;
        // telemetry manager?
    }

    @Override
    public void exit() {
        super.stopServer();
        System.exit(0);
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        client.logMessage(new MessageParams(MessageType.Info, "Emulator lang initializing ..."));
        Integer processId = params.getProcessId();
        if (processId != null) {
            setParentProcessId(processId);
        } else {
            // missing parent ID!
            setParentProcessId(0);
        }

        ServerCapabilities capabilities = createServerCapabilities();
        InitializeResult result = new InitializeResult(capabilities);
        return CompletableFuture.completedFuture(result);
    }
    
    @Override
    public void initialized(InitializedParams params) {
        LanguageServer.super.initialized(params);
        // telemetry
        client.logMessage(new MessageParams(MessageType.Info, "Emulator lang initialized"));
    }

    private ServerCapabilities createServerCapabilities() {
        ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setTextDocumentSync(TextDocumentSyncKind.None);
        capabilities.setCompletionProvider(null);
        capabilities.setHoverProvider(false);
        capabilities.setDocumentSymbolProvider(false);
        capabilities.setReferencesProvider(false);
        capabilities.setDefinitionProvider(false);
        capabilities.setCodeActionProvider(false);
        capabilities.setFoldingRangeProvider(false);
        capabilities.setDiagnosticProvider(new DiagnosticRegistrationOptions(true, false));
        return capabilities;
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        logMessage(MessageType.Info, "Emulator lang shutting down ...");
        shutdownServer();
        return CompletableFuture.completedFuture(new Object());
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        if (textDocumentService == null)
            textDocumentService = new ELTextDocumentService(this);
        return textDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        if (workspaceService == null)
            workspaceService = new ELWorkspaceService(this);
        return workspaceService;
    }

    @Override
    public void setTrace(SetTraceParams params) {

    }
    
    public void sendShowMessageNotification(final MessageType type, final String msg) {
        client.showMessage(new MessageParams(type, msg));
    }

    public void logMessage(final MessageType type, final String msg) {
        client.logMessage(new MessageParams(type, msg));
    }

    public boolean debugEnabled = true;

    public void logDebug(final String msg) {
        if (!debugEnabled)
            return;
        client.logMessage(new MessageParams(MessageType.Info, msg));
    }

    public void logInfo(final String msg) {
        client.logMessage(new MessageParams(MessageType.Info, msg));
    }

    public void logWarn(final String msg) {
        client.logMessage(new MessageParams(MessageType.Warning, msg));
    }
    public void logError(final String msg) {
        client.logMessage(new MessageParams(MessageType.Error, msg));
    }

    public void triggerDiagnostics() {
        workspaceService.triggerRecompile();
    }
}
