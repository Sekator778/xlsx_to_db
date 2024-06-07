package com.pb.filereader;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class CsvFileReader implements FileReader {

    @Override
    public Map<Integer, String> readHeaders(InputStream inputStream) throws Exception {
        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(inputStream)).withSkipLines(0).build()) {
            String[] headers = reader.readNext();
            Map<Integer, String> headerMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(i, headers[i]);
            }
            return headerMap;
        }
    }

    @Override
    public Map<Integer, String> determineColumnTypes(InputStream inputStream) throws Exception {
        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(inputStream)).withSkipLines(1).build()) {
            String[] firstDataRow = reader.readNext();
            Map<Integer, String> columnTypes = new HashMap<>();
            for (int i = 0; i < firstDataRow.length; i++) {
                String value = firstDataRow[i];
                String columnType = determineColumnType(value);
                columnTypes.put(i, columnType);
            }
            return columnTypes;
        }
    }

    private String determineColumnType(String value) {
        if (value == null || value.isEmpty()) {
            return "TEXT";
        }
        try {
            Double.parseDouble(value);
            return "NUMERIC";
        } catch (NumberFormatException e) {
            // Not a number
        }
        try {
            java.sql.Timestamp.valueOf(value);
            return "TIMESTAMP";
        } catch (IllegalArgumentException e) {
            // Not a timestamp
        }
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return "BOOLEAN";
        }
        return "TEXT";
    }
}
