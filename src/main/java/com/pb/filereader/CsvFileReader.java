package com.pb.filereader;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.pb.util.ColumnType;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static com.pb.util.ColumnType.BOOLEAN;
import static com.pb.util.ColumnType.NUMERIC;
import static com.pb.util.ColumnType.TEXT;
import static com.pb.util.ColumnType.TIMESTAMP;

public class CsvFileReader implements FileReader {

    @Override
    public Map<Integer, String> readHeaders(InputStream inputStream) throws Exception {
        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(inputStream)).withSkipLines(0).build()) {
            String[] headers = reader.readNext();
            Map<Integer, String> headerMap = new HashMap<>();
            if (headers != null) {
                for (int i = 0; i < headers.length; i++) {
                    headerMap.put(i, headers[i]);
                }
            }
            return headerMap;
        }
    }

    @Override
    public Map<Integer, String> determineColumnTypes(InputStream inputStream) throws Exception {
        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(inputStream)).withSkipLines(1).build()) {
            String[] firstDataRow = reader.readNext();
            Map<Integer, String> columnTypes = new HashMap<>();
            if (firstDataRow != null) {
                for (int i = 0; i < firstDataRow.length; i++) {
                    String value = firstDataRow[i];
                    ColumnType columnType = determineColumnType(value);
                    columnTypes.put(i, columnType.toString());
                }
            }
            return columnTypes;
        }
    }

    /**
     * Determines the column type based on the value.
     *
     * @param value The value to check.
     * @return The determined column type.
     */
    private ColumnType determineColumnType(String value) {
        if (value == null || value.isEmpty()) {
            return TEXT;
        }
        if (isNumeric(value)) {
            return NUMERIC;
        }
        if (isTimestamp(value)) {
            return TIMESTAMP;
        }
        if (isBoolean(value)) {
            return BOOLEAN;
        }
        return TEXT;
    }

    /**
     * Checks if the value is a numeric type using Spring's NumberUtils.
     *
     * @param value The value to check.
     * @return True if the value is numeric, otherwise false.
     */
    private boolean isNumeric(String value) {
        try {
            return StringUtils.isNumeric(value);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if the value is a timestamp type.
     *
     * @param value The value to check.
     * @return True if the value is a timestamp, otherwise false.
     */
    private boolean isTimestamp(String value) {
        try {
            java.sql.Timestamp.valueOf(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Checks if the value is a boolean type using Apache Commons BooleanUtils.
     *
     * @param value The value to check.
     * @return True if the value is boolean, otherwise false.
     */
    private boolean isBoolean(String value) {
        return BooleanUtils.toBooleanObject(value) != null;
    }
}
