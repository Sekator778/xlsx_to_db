package com.pb.util;

import com.pb.filereader.CsvFileReader;
import com.pb.filereader.DbfFileReader;
import com.pb.filereader.ExcelFileReader;
import com.pb.filereader.FileReader;
import com.pb.writer.PostgresDatabaseWriter;

import org.apache.commons.math3.util.Pair;

import java.io.InputStream;
import java.sql.Connection;
import java.util.Map;

public class FileProcessor {

    public static void processFile(String filePath, Connection connection) throws Exception {
        FileReader fileReader;
        PostgresDatabaseWriter databaseWriter = new PostgresDatabaseWriter();

        Pair<String, String> tableNameAndExtension = TableNameUtil.createTableNameAndExtension(filePath);
        String tableName = tableNameAndExtension.getFirst();
        String fileExtension = tableNameAndExtension.getSecond().toLowerCase();

        fileReader = switch (fileExtension) {
            case "xlsx" -> new ExcelFileReader();
            case "dbf" -> new DbfFileReader();
            case "csv" -> new CsvFileReader();
            default -> throw new IllegalArgumentException("Unsupported file extension: " + fileExtension);
        };

        try (InputStream inputStream = new java.io.FileInputStream(filePath)) {
            Map<Integer, String> headers = fileReader.readHeaders(inputStream);
            inputStream.close();

            try (InputStream inputStreamForColumnTypes = new java.io.FileInputStream(filePath)) {
                Map<Integer, String> columnTypes = fileReader.determineColumnTypes(inputStreamForColumnTypes);
                databaseWriter.createTable(headers, columnTypes, tableName);

                try (InputStream inputStreamForData = new java.io.FileInputStream(filePath)) {
                    databaseWriter.insertData(headers, columnTypes, tableName, fileExtension, inputStreamForData);
                }
            }
        }
    }
}
