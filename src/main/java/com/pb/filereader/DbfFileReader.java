package com.pb.filereader;

import com.linuxense.javadbf.DBFDataType;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DbfFileReader implements FileReader {

    @Override
    public Map<Integer, String> readHeaders(InputStream inputStream) throws Exception {
        DBFReader reader = new DBFReader(inputStream);
        Map<Integer, String> headers = new HashMap<>();
        for (int i = 0; i < reader.getFieldCount(); i++) {
            DBFField field = reader.getField(i);
            headers.put(i, field.getName());
        }
        return headers;
    }

    @Override
    public Map<Integer, String> determineColumnTypes(InputStream inputStream) throws Exception {
        DBFReader reader = new DBFReader(inputStream);
        Map<Integer, String> columnTypes = new HashMap<>();
        for (int i = 0; i < reader.getFieldCount(); i++) {
            DBFField field = reader.getField(i);
            String type = switch (field.getType()) {
                case DBFDataType.CHARACTER -> "TEXT";
                case DBFDataType.NUMERIC -> "NUMERIC";
                case DBFDataType.DATE -> "TIMESTAMP";
                case DBFDataType.LOGICAL -> "BOOLEAN";
                default -> "TEXT";
            };
            columnTypes.put(i, type);
        }
        return columnTypes;
    }
}
