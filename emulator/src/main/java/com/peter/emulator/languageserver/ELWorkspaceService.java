package com.peter.emulator.languageserver;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.json.JSONException;

import com.peter.emulator.lang.LanguageServer;

public class ELWorkspaceService implements WorkspaceService {

    public final ELLanguageServer lspServer;
    private final ArrayList<File> moduleRoots = new ArrayList<>();

    public ELWorkspaceService(ELLanguageServer lspServer) {
        this.lspServer = lspServer;
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {
        
    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        for (FileEvent event : params.getChanges()) {
            URI uri = URI.create(event.getUri());
            lspServer.logDebug("Change to %s", uri.toString());
            addFile(uri);
        }
        triggerRecompile();
    }

    public void addFile(URI uri) {
        File f = new File(uri);
        boolean found = false;
        while (!found) {
            f = f.getParentFile();
            for (File f2 : f.listFiles()) {
                if (f2.isFile() && f2.getName().equals("module-info.json")) {
                    found = true;
                    if (!moduleRoots.contains(f)) {
                        lspServer.logInfo("Added new module %s to diagnostics", f.getAbsolutePath());
                        moduleRoots.add(f);
                    }
                    break;
                }
            }
        }
        if (!found) {
            lspServer.logError("Found .el file outside of module, no diagnostics available for it");
        }
    }

    public void triggerRecompile() {
        lspServer.logDebug("Recompiling modules ...");
        LanguageServer ls = new LanguageServer();
        for (File f : moduleRoots) {
            lspServer.logDebug("- %s", f.getAbsolutePath());
            if (!f.exists()) {
                lspServer.logWarn("Could not find module info at %s", f.getAbsolutePath());
                moduleRoots.remove(f);
                continue;
            }
            try {
                ls.addModule(f);
            } catch (JSONException | IOException e) {
                lspServer.logError("Error adding module at %s", f.getAbsolutePath());
            }
        }
        lspServer.errors = ls.recompile();
        lspServer.logDebug("Modules recompiled");
        lspServer.pushDiagnostics();
    }

}
