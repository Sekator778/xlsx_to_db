package com.pb.service;

import com.pb.filereader.FileReader;
import com.pb.writer.DatabaseWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.util.Map;

import static com.pb.util.EscapeUtil.sanitizeHeader;

public class FileProcessingService {
    private final FileReader fileReader;
    private final DatabaseWriter databaseWriter;

    public FileProcessingService(FileReader fileReader, DatabaseWriter databaseWriter) {
        this.fileReader = fileReader;
        this.databaseWriter = databaseWriter;
    }

    public void processFile(File file, String tableName, String extension, Connection connection) throws Exception {

        try (InputStream fileInputStream = Files.newInputStream(file.toPath())) {
            // Read all bytes using a ByteArrayOutputStream
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fileInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
            byte[] fileData = byteArrayOutputStream.toByteArray();

            // Create multiple InputStreams from the byte array
            try (InputStream headerStream = new ByteArrayInputStream(fileData);
                 InputStream columnTypeStream = new ByteArrayInputStream(fileData);
                 InputStream dataStream = new ByteArrayInputStream(fileData)) {

                // Process headers, sanitize, and column types
                Map<Integer, String> headers = fileReader.readHeaders(headerStream);
                headers.replaceAll((k, v) -> sanitizeHeader(v));
                Map<Integer, String> columnTypes = fileReader.determineColumnTypes(columnTypeStream);

                // Create table and insert data using connection
                databaseWriter.createTable(headers, columnTypes, tableName, connection);
                databaseWriter.insertData(headers, columnTypes, tableName, extension, dataStream, connection);
            }
        }
    }
}
