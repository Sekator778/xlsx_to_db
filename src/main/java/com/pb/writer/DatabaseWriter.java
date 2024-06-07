package com.pb.writer;

import java.io.InputStream;
import java.util.Map;

public interface DatabaseWriter {
    void createTable(Map<Integer, String> headers, Map<Integer, String> columnTypes, String tableName) throws Exception;
    void insertData(Map<Integer, String> headers, Map<Integer, String> columnTypes, String tableName, InputStream inputStream) throws Exception;
}
