package com.pb.filereader;

import com.linuxense.javadbf.DBFDataType;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.pb.util.ColumnType.BOOLEAN;
import static com.pb.util.ColumnType.NUMERIC;
import static com.pb.util.ColumnType.TEXT;
import static com.pb.util.ColumnType.TIMESTAMP;

public class DbfFileReader implements FileReader {
    private static final Logger logger = Logger.getLogger(DbfFileReader.class.getName());

    @Override
    public Map<Integer, String> readHeaders(InputStream inputStream) {
        Map<Integer, String> headers = new HashMap<>();
        try (DBFReader reader = new DBFReader(inputStream)) {
            for (int i = 0; i < reader.getFieldCount(); i++) {
                DBFField field = reader.getField(i);
                headers.put(i, field.getName());
            }
        } catch (Exception e) {
            logger.severe("Error reading DBF headers" + e.getMessage());
        }
        return headers;
    }

    @Override
    public Map<Integer, String> determineColumnTypes(InputStream inputStream) {
        Map<Integer, String> columnTypes = new HashMap<>();
        try (DBFReader reader = new DBFReader(inputStream)) {
            for (int i = 0; i < reader.getFieldCount(); i++) {
                DBFField field = reader.getField(i);
                String type = switch (field.getType()) {
                    case DBFDataType.NUMERIC -> NUMERIC.getValue();
                    case DBFDataType.DATE -> TIMESTAMP.getValue();
                    case DBFDataType.LOGICAL -> BOOLEAN.getValue();
                    default -> TEXT.getValue();
                };
                columnTypes.put(i, type);
            }
        } catch (Exception e) {
            logger.severe("Error determining DBF column types" + e.getMessage());
        }
        return columnTypes;
    }
}
