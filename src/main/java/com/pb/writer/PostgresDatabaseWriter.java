package com.pb.writer;

import com.linuxense.javadbf.DBFReader;
import com.pb.util.DatabaseConnectionManager;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class PostgresDatabaseWriter implements DatabaseWriter {

    public static final int BATCH_SIZE = 2500;
    private static final Logger log = Logger.getLogger(PostgresDatabaseWriter.class.getName());
    private static final Pattern VALID_SQL_IDENTIFIER = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");

    @Override
    public void createTable(Map<Integer, String> headers, Map<Integer, String> columnTypes, String tableName) throws Exception {
        validateSqlIdentifier(tableName);
        StringBuilder createTableSQL = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");
        for (Map.Entry<Integer, String> entry : headers.entrySet()) {
            int columnIndex = entry.getKey();
            String columnName = entry.getValue();
            String columnType = columnTypes.get(columnIndex);
            validateSqlIdentifier(columnName);
            createTableSQL.append("\"").append(columnName).append("\" ").append(columnType).append(",");
        }
        createTableSQL.deleteCharAt(createTableSQL.length() - 1).append(")");

        try (Connection connection = DatabaseConnectionManager.getConnection();
             Statement statement = connection.createStatement()) {
            String dropTableSQL = "DROP TABLE IF EXISTS " + tableName;
            log.info("Dropping table with SQL: " + dropTableSQL);
            statement.execute(dropTableSQL);
            log.info("Creating table with SQL: " + createTableSQL);
            statement.execute(createTableSQL.toString());
        }
    }

    @Override
    public void insertData(Map<Integer, String> headers, Map<Integer, String> columnTypes, String tableName, String extension, InputStream inputStream) throws Exception {

        try (Connection connection = DatabaseConnectionManager.getConnection()) {
            connection.setAutoCommit(false);

            if (extension.equalsIgnoreCase("xlsx")) {
                insertExcelData(headers, columnTypes, tableName, inputStream, connection);
            } else if (extension.equalsIgnoreCase("dbf")) {
                insertDbfData(headers, columnTypes, tableName, inputStream, connection);
            }

            connection.commit();
        }
    }

    private void insertExcelData(Map<Integer, String> headers, Map<Integer, String> columnTypes, String tableName, InputStream inputStream, Connection connection) throws Exception {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        StringBuilder insertSQL = new StringBuilder("INSERT INTO " + tableName + " (");
        for (String columnName : headers.values()) {
            validateSqlIdentifier(columnName);
            insertSQL.append("\"").append(columnName).append("\",");
        }
        insertSQL.deleteCharAt(insertSQL.length() - 1).append(") VALUES (");
        insertSQL.append("?,".repeat(headers.size()));
        insertSQL.deleteCharAt(insertSQL.length() - 1).append(")");

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL.toString())) {
            int count = 0;
            int total = 0;

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String columnType = columnTypes.get(i);
                    setPreparedStatementValue(preparedStatement, i + 1, cell, columnType);
                }
                preparedStatement.addBatch();

                if (++count % BATCH_SIZE == 0) {
                    preparedStatement.executeBatch();
                    total += count;
                    log.info(count + " rows have been inserted into the table.");
                    count = 0;
                }
            }
            if (count > 0) {
                preparedStatement.executeBatch();
                total += count;
                log.info(count + " rows have been inserted into the table.");
            }
            log.info("Total " + total + " rows have been inserted into the table.");
        }
    }

    private void insertDbfData(Map<Integer, String> headers, Map<Integer, String> columnTypes, String tableName, InputStream inputStream, Connection connection) throws Exception {
        DBFReader reader = new DBFReader(inputStream);

        StringBuilder insertSQL = new StringBuilder("INSERT INTO " + tableName + " (");
        for (String columnName : headers.values()) {
            validateSqlIdentifier(columnName);
            insertSQL.append("\"").append(columnName).append("\",");
        }
        insertSQL.deleteCharAt(insertSQL.length() - 1).append(") VALUES (");
        insertSQL.append("?,".repeat(headers.size()));
        insertSQL.deleteCharAt(insertSQL.length() - 1).append(")");

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL.toString())) {
            int count = 0;
            int total = 0;

            Object[] row;
            while ((row = reader.nextRecord()) != null) {
                for (int i = 0; i < headers.size(); i++) {
                    Object value = row[i];
                    String columnType = columnTypes.get(i);
                    setPreparedStatementValue(preparedStatement, i + 1, value, columnType);
                }
                preparedStatement.addBatch();

                if (++count % BATCH_SIZE == 0) {
                    preparedStatement.executeBatch();
                    total += count;
                    log.info(count + " rows have been inserted into the table.");
                    count = 0;
                }
            }
            if (count > 0) {
                preparedStatement.executeBatch();
                total += count;
                log.info(count + " rows have been inserted into the table.");
            }
            log.info("Total " + total + " rows have been inserted into the table.");
        }
    }

    private void setPreparedStatementValue(PreparedStatement preparedStatement, int parameterIndex, Cell cell, String columnType) throws SQLException {
        switch (columnType) {
            case "NUMERIC" -> preparedStatement.setDouble(parameterIndex, cell.getNumericCellValue());
            case "TIMESTAMP" -> preparedStatement.setTimestamp(parameterIndex, new Timestamp(cell.getDateCellValue().getTime()));
            case "BOOLEAN" -> preparedStatement.setBoolean(parameterIndex, cell.getBooleanCellValue());
            default -> preparedStatement.setString(parameterIndex, getCellValueAsString(cell));
        }
    }

    private void setPreparedStatementValue(PreparedStatement preparedStatement, int parameterIndex, Object value, String columnType) throws SQLException {
        if (value == null) {
            preparedStatement.setNull(parameterIndex, java.sql.Types.NULL);
            return;
        }
        switch (columnType) {
            case "NUMERIC":
                if (value instanceof Number) {
                    preparedStatement.setDouble(parameterIndex, ((Number) value).doubleValue());
                } else {
                    preparedStatement.setNull(parameterIndex, java.sql.Types.NUMERIC);
                }
                break;
            case "TIMESTAMP":
                if (value instanceof Date) {
                    preparedStatement.setTimestamp(parameterIndex, new Timestamp(((Date) value).getTime()));
                } else {
                    preparedStatement.setNull(parameterIndex, java.sql.Types.TIMESTAMP);
                }
                break;
            case "BOOLEAN":
                if (value instanceof Boolean) {
                    preparedStatement.setBoolean(parameterIndex, (Boolean) value);
                } else {
                    preparedStatement.setNull(parameterIndex, java.sql.Types.BOOLEAN);
                }
                break;
            default:
                preparedStatement.setString(parameterIndex, value.toString());
                break;
        }
    }

    private String getCellValueAsString(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell) ? cell.getDateCellValue().toString() : String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private void validateSqlIdentifier(String identifier) {
        if (!VALID_SQL_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier: " + identifier);
        }
    }
}
