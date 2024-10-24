package com.pb.writer;

import java.io.InputStream;
import java.sql.Connection;
import java.util.Map;

public interface DatabaseWriter {
    void createTable(Map<Integer, String> headers, Map<Integer, String> columnTypes, String tableName, Connection connection) throws Exception;
    void insertData(Map<Integer, String> headers, Map<Integer, String> columnTypes, String tableName, String extension, InputStream inputStream, Connection connection) throws Exception;
}
