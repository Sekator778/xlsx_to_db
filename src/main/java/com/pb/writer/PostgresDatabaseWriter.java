package com.pb.writer;

import com.linuxense.javadbf.DBFReader;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.io.InputStreamReader;
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
    private static final Pattern VALID_SQL_IDENTIFIER = Pattern.compile("[\\p{L}_][\\p{L}\\p{N}_\\s()]*");

    @Override
    public void createTable(Map<Integer, String> headers, Map<Integer, String> columnTypes, String tableName, Connection connection) throws Exception {
//        validateSqlIdentifier(tableName); TODO current version for ase database and syntax as CREATE TABLE #temp_table(id int);
        StringBuilder createTableSQL = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");
        for (Map.Entry<Integer, String> entry : headers.entrySet()) {
            int columnIndex = entry.getKey();
            String columnName = entry.getValue();
            String columnType = columnTypes.get(columnIndex);
            validateSqlIdentifier(columnName);
            createTableSQL.append(columnName).append(" ").append(columnType).append(",");
        }
        createTableSQL.deleteCharAt(createTableSQL.length() - 1).append(")");

        Statement statement = connection.createStatement();
        String dropTableSQL = "DROP TABLE IF EXISTS " + tableName;
        log.info("Dropping table with SQL: " + dropTableSQL);
        statement.execute(dropTableSQL);
        log.info("Creating table with SQL: " + createTableSQL);
        statement.execute(createTableSQL.toString());
    }

    @Override
    public void insertData(Map<Integer, String> headers, Map<Integer, String> columnTypes, String tableName, String extension, InputStream inputStream, Connection connection) throws Exception {
        connection.setAutoCommit(false);

        switch (extension.toLowerCase()) {
            case "xlsx":
                insertExcelData(headers, columnTypes, tableName, inputStream, connection);
                break;
            case "dbf":
                insertDbfData(headers, columnTypes, tableName, inputStream, connection);
                break;
            case "csv":
                insertCsvData(headers, columnTypes, tableName, inputStream, connection);
                break;
            default:
                throw new IllegalArgumentException("Unsupported file extension: " + extension);
        }
        connection.commit();
    }

    private void insertExcelData(Map<Integer, String> headers, Map<Integer, String> columnTypes, String tableName, InputStream inputStream, Connection connection) throws Exception {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        StringBuilder insertSQL = new StringBuilder("INSERT INTO " + tableName + " (");
        for (String columnName : headers.values()) {
            validateSqlIdentifier(columnName);
            insertSQL.append(columnName).append(",");
        }
        insertSQL.deleteCharAt(insertSQL.length() - 1).append(") VALUES (");
        for (int i = 0; i < headers.size(); i++) {
            insertSQL.append("?,");
        }
        insertSQL.deleteCharAt(insertSQL.length() - 1).append(")");

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL.toString())) {
            int count = 0;
            int total = 0;

            for (Row row : sheet) {
                if (row.getRowNum() == 0 || isRowEmpty(row)) continue;
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

    /**
     * Checks if the row is empty.
     * use in insertExcelData method
     */
    private boolean isRowEmpty(Row row) {
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private void insertDbfData(Map<Integer, String> headers, Map<Integer, String> columnTypes, String tableName, InputStream inputStream, Connection connection) throws Exception {
        DBFReader reader = new DBFReader(inputStream);

        StringBuilder insertSQL = new StringBuilder("INSERT INTO " + tableName + " (");
        for (String columnName : headers.values()) {
            validateSqlIdentifier(columnName);
            insertSQL.append(columnName).append(",");
        }
        insertSQL.deleteCharAt(insertSQL.length() - 1).append(") VALUES (");
        for (int i = 0; i < headers.size(); i++) {
            insertSQL.append("?,");
        }
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

    private void insertCsvData(Map<Integer, String> headers, Map<Integer, String> columnTypes, String tableName, InputStream inputStream, Connection connection) throws Exception {
        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(inputStream)).withSkipLines(1).build();
             PreparedStatement preparedStatement = connection.prepareStatement(buildInsertSQL(headers, tableName))) {
            connection.setAutoCommit(false);

            String[] row;
            int count = 0;
            int total = 0;

            while ((row = reader.readNext()) != null) {
                for (int i = 0; i < headers.size(); i++) {
                    String value = row[i];
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
            connection.commit();
            log.info("Total " + total + " rows have been inserted into the table.");
        }
    }

    private String buildInsertSQL(Map<Integer, String> headers, String tableName) {
        StringBuilder insertSQL = new StringBuilder("INSERT INTO " + tableName + " (");
        for (String columnName : headers.values()) {
            validateSqlIdentifier(columnName);
            insertSQL.append(columnName).append(",");
        }
        insertSQL.deleteCharAt(insertSQL.length() - 1).append(") VALUES (");
        for (int i = 0; i < headers.size(); i++) {
            insertSQL.append("?,");
        }
        insertSQL.deleteCharAt(insertSQL.length() - 1).append(")");
        return insertSQL.toString();
    }

    private void setPreparedStatementValue(PreparedStatement preparedStatement, int parameterIndex, String value, String columnType) throws SQLException {
        if (value == null || value.isEmpty()) {
            preparedStatement.setNull(parameterIndex, getSqlType(columnType));
            return;
        }
        switch (columnType) {
            case "INTEGER":
                preparedStatement.setInt(parameterIndex, Integer.parseInt(value));
                break;
            case "NUMERIC":
                preparedStatement.setDouble(parameterIndex, Double.parseDouble(value));
                break;
            case "TIMESTAMP":
                preparedStatement.setTimestamp(parameterIndex, Timestamp.valueOf(value));
                break;
            case "BOOLEAN":
                preparedStatement.setBoolean(parameterIndex, Boolean.parseBoolean(value));
                break;
            default:
                preparedStatement.setString(parameterIndex, value);
                break;
        }
    }

    private void setPreparedStatementValue(PreparedStatement preparedStatement, int parameterIndex, Cell cell, String columnType) throws SQLException {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            preparedStatement.setNull(parameterIndex, getSqlType(columnType));
            return;
        }

        switch (columnType) {
            case "INTEGER":
                if (cell.getCellType() == CellType.NUMERIC && cell.getNumericCellValue() % 1 == 0) {
                    preparedStatement.setInt(parameterIndex, (int) cell.getNumericCellValue());
                } else {
                    preparedStatement.setNull(parameterIndex, java.sql.Types.INTEGER);
                }
                break;
            case "NUMERIC":
                preparedStatement.setDouble(parameterIndex, cell.getNumericCellValue());
                break;
            case "TIMESTAMP":
                preparedStatement.setTimestamp(parameterIndex, new Timestamp(cell.getDateCellValue().getTime()));
                break;
            case "BOOLEAN":
                preparedStatement.setBoolean(parameterIndex, cell.getBooleanCellValue());
                break;
            default:
                preparedStatement.setString(parameterIndex, getCellValueAsString(cell));
                break;
        }
    }

    private void setPreparedStatementValue(PreparedStatement preparedStatement, int parameterIndex, Object value, String columnType) throws SQLException {
        if (value == null) {
            preparedStatement.setNull(parameterIndex, getSqlType(columnType));
            return;
        }
        switch (columnType) {
            case "INTEGER":
                if (value instanceof Number) {
                    preparedStatement.setInt(parameterIndex, ((Number) value).intValue());
                } else {
                    preparedStatement.setNull(parameterIndex, java.sql.Types.INTEGER);
                }
                break;
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

    private int getSqlType(String columnType) {
        switch (columnType) {
            case "INTEGER":
                return java.sql.Types.INTEGER;
            case "NUMERIC":
                return java.sql.Types.NUMERIC;
            case "TIMESTAMP":
                return java.sql.Types.TIMESTAMP;
            case "BOOLEAN":
                return java.sql.Types.BOOLEAN;
            default:
                return java.sql.Types.VARCHAR;
        }
    }

    private String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return DateUtil.isCellDateFormatted(cell) ? cell.getDateCellValue().toString() : String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private void validateSqlIdentifier(String identifier) {
        if (!VALID_SQL_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier: " + identifier);
        }
    }
}
