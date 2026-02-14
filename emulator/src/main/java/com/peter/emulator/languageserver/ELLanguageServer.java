package com.peter.emulator.languageserver;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.*;

import com.peter.emulator.lang.ELAnalysisError;
import com.peter.emulator.lang.ErrorSet;
import com.peter.emulator.lang.ProgramModule;
import com.peter.emulator.lang.ProgramUnit;

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
        capabilities.setHoverProvider(true);
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

    public void logDebug(final String msg, Object... args) {
        if (!debugEnabled)
            return;
        client.logMessage(new MessageParams(MessageType.Info, String.format(msg, args)));
    }

    public void logInfo(final String msg) {
        client.logMessage(new MessageParams(MessageType.Info, msg));
    }
    public void logInfo(final String msg, Object... args) {
        client.logMessage(new MessageParams(MessageType.Info, String.format(msg, args)));
    }

    public void logWarn(final String msg) {
        client.logMessage(new MessageParams(MessageType.Warning, msg));
    }
    public void logWarn(final String msg, Object... args) {
        client.logMessage(new MessageParams(MessageType.Warning, String.format(msg, args)));
    }

    public void logError(final String msg) {
        client.logMessage(new MessageParams(MessageType.Error, msg));
    }
    public void logError(final String msg, Object... args) {
        client.logMessage(new MessageParams(MessageType.Error, String.format(msg, args)));
    }

    public void triggerDiagnostics() {
        workspaceService.triggerRecompile();
    }

    private final HashMap<String, ArrayList<Diagnostic>> fileDiagnostics = new HashMap<>();
    public void pushDiagnostics() {
        for(ArrayList<Diagnostic> list : fileDiagnostics.values()) {
            list.clear();
        }
        if(errors == null)
            triggerDiagnostics();
        for (ELAnalysisError err : errors) {
            if (err.span == null) {
                continue;
            }
            String f = err.span.start().file();
            ArrayList<Diagnostic> diagnostics;
            if(fileDiagnostics.containsKey(f))
                diagnostics = fileDiagnostics.get(f);
            else {
                diagnostics = new ArrayList<>();
                fileDiagnostics.put(f, diagnostics);
            }
            diagnostics.add(new Diagnostic(err.span.toRange(), err.reason, err.severity.severity, "emulatorlang"));
        }
        for(Map.Entry<String, ArrayList<Diagnostic>> entry : fileDiagnostics.entrySet()) {
            client.publishDiagnostics(new PublishDiagnosticsParams(entry.getKey(), entry.getValue()));
        }
    }

    public void addFile(URI uri) {
        workspaceService.addFile(uri);
    }

    public ProgramUnit getUnit(URI uri) {
        return workspaceService.getUnit(uri);
    }
}
