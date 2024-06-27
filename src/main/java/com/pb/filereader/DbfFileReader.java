package com.pb.filereader;

import com.linuxense.javadbf.DBFDataType;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;
import com.pb.util.ColumnType;
import com.pb.util.ColumnTypeUtil;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.pb.util.ColumnType.BOOLEAN;
import static com.pb.util.ColumnType.NUMERIC;
import static com.pb.util.ColumnType.TEXT;
import static com.pb.util.ColumnType.TIMESTAMP;
import static com.pb.util.ColumnType.INTEGER;

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
        switch (value) {
            case null -> {
                return TEXT;
            }
            case Number number -> {
                if (value.toString().contains(".")) {
                    return NUMERIC;
                } else {
                    return INTEGER;
                }
            }
            case Boolean b -> {
                return BOOLEAN;
            }
            case java.util.Date date -> {
                return TIMESTAMP;
            }
            default -> {
            }
        }
        return TEXT;
    }
}
