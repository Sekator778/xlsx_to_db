package com.pb.util;

import org.apache.commons.math3.util.Pair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility class for file operations, including unzipping files.
 */
public class FileUtils {
    private static final Logger log = Logger.getLogger(FileUtils.class.getName());

    /**
     * Unzips the given .zip file or handles a regular file.
     * If it's a zip file, it returns an InputStream for the first file found,
     * otherwise, it returns the InputStream for the file directly, along with the file name and extension.
     *
     * @param file The file (could be a .zip file or any other file).
     * @return A FileUnzipResult containing the InputStream, file name without extension, and the extension.
     */
    public static FileUnzipResult unzipOrProcessFile(File file) {
        // Check if the file is a zip file
        if (file.getName().toLowerCase().endsWith(".zip")) {
            return unzip(file);
        } else {
            // For non-zip files, return the InputStream, file name without extension, and extension
            try {
                InputStream fileInputStream = Files.newInputStream(file.toPath());
                Pair<String, String> tableNameAndExtension = TableNameUtil.createTableNameAndExtension(file.getName());
                String fileNameWithoutExtension = tableNameAndExtension.getFirst();
                String extension = tableNameAndExtension.getSecond();

                return new FileUnzipResult(fileInputStream, fileNameWithoutExtension, extension);
            } catch (IOException e) {
                log.severe("Error processing file: " + file.getAbsolutePath() + " - " + e.getMessage());
                return null;
            }
        }
    }

    /**
     * Unzips the given .zip file, returning an InputStream for the first file found,
     * the file name without extension, and the extension.
     *
     * @param zipFile The .zip file to unzip.
     * @return A FileUnzipResult containing the InputStream, file name without extension, and the extension.
     */
    private static FileUnzipResult unzip(File zipFile) {
        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipFile.toPath()))) {
            ZipEntry zipEntry;

            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (!zipEntry.isDirectory()) {
                    // Get file name and extension using TableNameUtil
                    String fileNameWithExtension = zipEntry.getName();
                    Pair<String, String> tableNameAndExtension = TableNameUtil.createTableNameAndExtension(fileNameWithExtension);
                    String fileNameWithoutExtension = tableNameAndExtension.getFirst();
                    String extension = tableNameAndExtension.getSecond();

                    // Read the file content into a ByteArrayOutputStream
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zipInputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, len);
                    }

                    // Create an InputStream from the byte array
                    InputStream fileInputStream = new ByteArrayInputStream(outputStream.toByteArray());

                    // Return the result as a FileUnzipResult
                    return new FileUnzipResult(fileInputStream, fileNameWithoutExtension, extension);
                }
                zipInputStream.closeEntry();
            }
        } catch (IOException e) {
            log.severe("Error unzipping file: " + zipFile.getAbsolutePath() + " - " + e.getMessage());
        }

        return null; // Return null if no valid file is found
    }

    /**
     * Class to hold the result of the unzipping or file processing operation.
     */
    public static class FileUnzipResult {
        private final InputStream inputStream;
        private final String fileNameWithoutExtension;
        private final String extension;

        public FileUnzipResult(InputStream inputStream, String fileNameWithoutExtension, String extension) {
            this.inputStream = inputStream;
            this.fileNameWithoutExtension = fileNameWithoutExtension;
            this.extension = extension;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public String getFileNameWithoutExtension() {
            return fileNameWithoutExtension;
        }

        public String getExtension() {
            return extension;
        }
    }
}
