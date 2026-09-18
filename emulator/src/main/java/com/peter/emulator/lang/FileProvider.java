package com.peter.emulator.lang;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class FileProvider {

    protected HashMap<Path, String> cachedFiles = new HashMap<>();

    public String readFile(Path path) throws IOException {
        if (cachedFiles.containsKey(path)) {
            return cachedFiles.get(path);
        }
        return Files.readString(path);
    }

    public void addCachedFile(Path path, String file) {
        cachedFiles.put(path, file);
    }

    public void clearCachedFile(Path path) {
        cachedFiles.remove(path);
    }
}
