package com.pb.filereader;

import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import com.pb.util.ColumnType;
import com.pb.util.ColumnTypeUtil;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.pb.util.ColumnType.TEXT;

public class DbfFileReader implements FileReader {
    private static final Logger logger = Logger.getLogger(DbfFileReader.class.getName());

    private Map<Integer, String> headers;

    @Override
    public Map<Integer, String> readHeaders(InputStream inputStream) {
        Map<Integer, String> headers = new HashMap<>();
        try (DBFReader reader = new DBFReader(inputStream)) {
            for (int i = 0; i < reader.getFieldCount(); i++) {
                DBFField field = reader.getField(i);
                headers.put(i, field.getName());
            }
        } catch (Exception e) {
            logger.severe("Error reading DBF headers: " + e.getMessage());
        }
        this.headers = headers;
        return headers;
    }

    @Override
    public Map<Integer, String> determineColumnTypes(InputStream inputStream) {
        Map<Integer, String> columnTypes = new HashMap<>();
        int columnCount = headers.size();

        try (DBFReader reader = new DBFReader(inputStream)) {
            Object[] row;
            while ((row = reader.nextRecord()) != null) {
                for (int colIndex = 0; colIndex < columnCount; colIndex++) {
                    if (!columnTypes.containsKey(colIndex)) {
                        Object value = colIndex < row.length ? row[colIndex] : null;
                        ColumnType columnType = determineColumnType(value);
                        if (columnType != TEXT || !columnTypes.containsKey(colIndex)) {
                            columnTypes.put(colIndex, columnType.toString());
                        }
                    }
                }
            }
            for (int i = 0; i < columnCount; i++) {
                columnTypes.putIfAbsent(i, ColumnTypeUtil.getDefaultTypeForHeader(headers.get(i)));
            }
        } catch (Exception e) {
            logger.severe("Error determining DBF column types: " + e.getMessage());
        }

        return columnTypes;
    }

    /**
     * Determines the column type based on the value.
     *
     * @param value The value to check.
     * @return The determined column type.
     */
    private ColumnType determineColumnType(Object value) {
        if (value == null) {
            return ColumnType.TEXT;
        } else if (value instanceof Number) {
            // Check if it's a decimal number (contains a decimal point)
            if (value.toString().contains(".")) {
                return ColumnType.NUMERIC;
            } else {
                return ColumnType.INTEGER;
            }
        } else if (value instanceof Boolean) {
            return ColumnType.BOOLEAN;
        } else if (value instanceof java.util.Date) {
            return ColumnType.TIMESTAMP;
        }

        // Default case
        return ColumnType.TEXT;
    }

}
