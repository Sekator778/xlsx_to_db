package com.pb.service;

import com.pb.datasource.DataSource;
import com.pb.filereader.FileReader;
import com.pb.writer.DatabaseWriter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

public class FileProcessingService {
    private final FileReader fileReader;
    private final DataSource dataSource;
    private final DatabaseWriter databaseWriter;

    public FileProcessingService(FileReader fileReader, DataSource dataSource, DatabaseWriter databaseWriter) {
        this.fileReader = fileReader;
        this.dataSource = dataSource;
        this.databaseWriter = databaseWriter;
    }

    public void processFile(String source, String tableName) throws Exception {
        try (InputStream inputStream = dataSource.getInputStream(source)) {
            byte[] fileData = inputStream.readAllBytes();

            try (InputStream headerStream = new ByteArrayInputStream(fileData);
                 InputStream columnTypeStream = new ByteArrayInputStream(fileData);
                 InputStream dataStream = new ByteArrayInputStream(fileData)) {

                Map<Integer, String> headers = fileReader.readHeaders(headerStream);
                Map<Integer, String> columnTypes = fileReader.determineColumnTypes(columnTypeStream);
                databaseWriter.createTable(headers, columnTypes, tableName);
                databaseWriter.insertData(headers, columnTypes, tableName, dataStream);
            }
        }
    }
}
