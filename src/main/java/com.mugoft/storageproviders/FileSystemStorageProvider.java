package com.mugoft.storageproviders;

import com.mugoft.storageproviders.common.StorageProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;

/**
 * Output provider which stores cookies using local filesystem
 */
public class FileSystemStorageProvider extends StorageProvider {

    public FileSystemStorageProvider(String outPath) {
        super(outPath);
    }

    private void createDirIfNotExist() throws IOException {
        File outFile = new File(outPath);
        File outDir = new File(outFile.getParent());

        if (!outDir.exists()) {
            Files.createDirectories(outDir.toPath());
        }
    }

    public void storeCookies(String jsonCookies) throws Exception {
        createDirIfNotExist();
        try (Writer writer = new FileWriter(outPath)) {
            System.out.println("Storing cookies under " + outPath);
            writer.write(jsonCookies);
        }
    }
}
