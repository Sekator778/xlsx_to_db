package com.pb.util;

public class TableNameUtil {
    /**
     * Creates a table name from a file name by removing the file extension.
     *
     * @param fileName The name of the file.
     * @return The table name derived from the file name.
     */
    public static String createTableName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        return fileName.replaceFirst("[.][^.]+$", "");
    }
}
