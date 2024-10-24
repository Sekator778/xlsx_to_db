package com.pb.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by dn070578noi on 24.10.24.
 */
public class FileUtils {
    private static final Logger log = Logger.getLogger(FileUtils.class.getName());

    /**
     * Unzips the given .zip file to a temporary location and returns the first file found in the .zip.
     * If an error occurs, it logs the error and returns null.
     *
     * @param zipFile The .zip file to unzip.
     * @return The unzipped file, or null if an error occurred.
     */
    public static File unzip(File zipFile) {
        File destDir = new File(zipFile.getParent(), "unzipped_" + System.currentTimeMillis());
        if (!destDir.mkdir()) {
            log.severe("Failed to create directory: " + destDir.getAbsolutePath());
            return null;
        }

        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipFile.toPath()))) {
            ZipEntry zipEntry;
            File unzippedFile = null;

            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                String fileName = zipEntry.getName();
                unzippedFile = new File(destDir, fileName);

                if (zipEntry.isDirectory()) {
                    if (!unzippedFile.mkdirs()) {
                        log.severe("Failed to create directory: " + unzippedFile.getAbsolutePath());
                    }
                } else {
                    if (!new File(unzippedFile.getParent()).mkdirs()) {
                        log.severe("Failed to create parent directory: " + unzippedFile.getParent());
                    }
                    try (FileOutputStream fileOutputStream = new FileOutputStream(unzippedFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zipInputStream.read(buffer)) > 0) {
                            fileOutputStream.write(buffer, 0, len);
                        }
                    } catch (IOException e) {
                        log.severe("Error writing file: " + unzippedFile.getAbsolutePath());
                        return null;
                    }
                }
                zipInputStream.closeEntry();
            }

            return unzippedFile;
        } catch (IOException e) {
            log.severe("Error unzipping file: " + zipFile.getAbsolutePath() + " - " + e.getMessage());
            return null;
        }
    }
}
