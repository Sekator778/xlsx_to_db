package com.pb.util;

import org.apache.commons.math3.util.Pair;

public class TableNameUtil {
    /**
     * Creates a Pair with the table name derived from the file name and the file extension.
     *
     * @param fileName The name of the file.
     * @return A Pair where the first element is the table name (file name without extension) and the second element is the file extension.
     */
    public static Pair<String, String> createTableNameAndExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
           throw new IllegalArgumentException("File name must have an extension");
        }
        String name = fileName.substring(0, lastDotIndex);
        String extension = fileName.substring(lastDotIndex + 1);
        return new Pair<>(sanitizeName(name), extension);
    }

    /**
     * Sanitizes the name by replacing spaces with underscores and performing other necessary transformations.
     *
     * @param name The name to sanitize.
     * @return The sanitized name.
     */
    private static String sanitizeName(String name) {
        return name.replaceAll("\\s+", "_");
    }
}
