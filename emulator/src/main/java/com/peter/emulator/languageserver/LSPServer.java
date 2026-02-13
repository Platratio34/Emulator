package com.peter.emulator.languageserver;

import java.io.IOException;

public class LSPServer {

    public final String OS = System.getProperty("os.name").toLowerCase();

    private final class ClientWatcher implements Runnable {

        @Override
        public void run() {
            while (!shutdown && parentProcessStillRunning() && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if(!!Thread.currentThread().isInterrupted()) {
                // client vanish
            }
        }

    }

    private Thread clientWatcherRunner;
    private volatile boolean shutdown;
    private long parentProcessId;
    
    public void startServer() {
        clientWatcherRunner = new Thread(new ClientWatcher(), "EmulatorLang Client Watcher");
        clientWatcherRunner.start();
    }

    protected boolean parentProcessStillRunning() {
        if (parentProcessId == 0) {
            // still waiting for client connection
            return true;
        } else {
            // checking for client process pid {parentProcessId}
        }

        String command;
        if (OS.contains("win")) {
            command = String.format("cmd /c \"tasklist /FI \"PID eq %d\" | findstr %d\"", parentProcessId,
                    parentProcessId);
        } else {
            command = String.format("ps -p " + parentProcessId);
        }
        try {
            Process process = Runtime.getRuntime().exec(command);
            return process.waitFor() == 0;
        } catch (IOException e) {
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return true;
        }
    }
    
    public void stopServer() {
        if (clientWatcherRunner == null) {
            return;
        }
        clientWatcherRunner.interrupt();
    }

    public void shutdownServer() {
        shutdown = true;
    }

    protected synchronized long getParentProcessId() {
        return parentProcessId;
    }

    protected synchronized void setParentProcessId(long processId) {
        parentProcessId = processId;
    }
}
