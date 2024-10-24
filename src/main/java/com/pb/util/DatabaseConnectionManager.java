package com.pb.util;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

public class DatabaseConnectionManager {

    private static Map<String, Map<String, String>> properties;

    /* TODO rewrite for use datasource in main class */
    public static void loadProperties(String configFileName) {
        Yaml yaml = new Yaml();
        try (InputStream input = DatabaseConnectionManager.class.getClassLoader().getResourceAsStream(configFileName)) {
            properties = yaml.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load database properties from " + configFileName, e);
        }
    }

    public static Connection getConnection() {
        if (properties == null) {
            throw new RuntimeException("Database properties not loaded. Call loadProperties first.");
        }

        Map<String, String> jdbcProperties = properties.get("jdbc");
        if (jdbcProperties == null) {
            throw new RuntimeException("No JDBC configuration found in configuration file");
        }

        String url = jdbcProperties.get("url");
        String user = jdbcProperties.get("user");
        String password = jdbcProperties.get("password");
        Connection connection;
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database url: " + url, e);
        }
        return connection;
    }
}
