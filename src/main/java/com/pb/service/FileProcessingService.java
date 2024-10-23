package com.pb.service;

import com.pb.datasource.DataSource;
import com.pb.filereader.FileReader;
import com.pb.util.EscapeUtil;
import com.pb.writer.DatabaseWriter;

import org.apache.commons.math3.util.Pair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import static com.pb.util.EscapeUtil.sanitizeHeader;
import static com.pb.util.TableNameUtil.createTableNameAndExtension;

public class FileProcessingService {
    private final FileReader fileReader;
    private final DataSource dataSource;
    private final DatabaseWriter databaseWriter;

    public FileProcessingService(FileReader fileReader, DataSource dataSource, DatabaseWriter databaseWriter) {
        this.fileReader = fileReader;
        this.dataSource = dataSource;
        this.databaseWriter = databaseWriter;
    }

    public void processFile(String source, String fileName) throws Exception {
        Pair<String, String> tableNameAndExtension = createTableNameAndExtension(fileName);

        try (InputStream inputStream = dataSource.getInputStream(source)) {
            // Read all bytes using a ByteArrayOutputStream instead of readAllBytes()
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
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

                // Create table and insert data
                databaseWriter.createTable(headers, columnTypes, tableNameAndExtension.getFirst());
                databaseWriter.insertData(headers, columnTypes, tableNameAndExtension.getFirst(), tableNameAndExtension.getSecond(), dataStream);
            }
        }
    }
}
