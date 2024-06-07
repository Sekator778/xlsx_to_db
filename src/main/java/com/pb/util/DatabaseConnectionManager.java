package com.pb.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionManager {

    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/test";
    private static final String JDBC_USER = "postgres";
    private static final String JDBC_PASSWORD = "password";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }
}
