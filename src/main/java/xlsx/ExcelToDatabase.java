package xlsx;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class ExcelToDatabase {

    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/test";
    private static final String JDBC_USER = "postgres";
    private static final String JDBC_PASSWORD = "password";

    public static void main(String[] args) {
        String excelFilePath = "files/test_read_to_db.xlsx";
        File file = new File(excelFilePath);
        String fileNameWithoutExtension = file.getName().replaceFirst("[.][^.]+$", "");
        try {
            FileInputStream inputStream = new FileInputStream(excelFilePath);
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            // Read headers
            Row headerRow = sheet.getRow(0);
            Map<Integer, String> headers = new HashMap<>();
            for (Cell cell : headerRow) {
                headers.put(cell.getColumnIndex(), getCellValueAsString(cell));
            }

            // Determine column types
            Map<Integer, String> columnTypes = determineColumnTypes(sheet);

            // Create table
            createTable(headers, columnTypes, fileNameWithoutExtension);

            // Insert data
            insertData(sheet, headers, columnTypes);

            workbook.close();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private static Map<Integer, String> determineColumnTypes(Sheet sheet) {
        Map<Integer, String> columnTypes = new HashMap<>();
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Skip header row
            for (Cell cell : row) {
                int columnIndex = cell.getColumnIndex();
                String currentType = columnTypes.get(columnIndex);
                String newType = getColumnType(cell);
                columnTypes.put(columnIndex, determineMoreGeneralType(currentType, newType));
            }
        }
        return columnTypes;
    }

    private static String getColumnType(Cell cell) {
        return switch (cell.getCellType()) {
            case NUMERIC -> DateUtil.isCellDateFormatted(cell) ? "TIMESTAMP" : "NUMERIC";
            case BOOLEAN -> "BOOLEAN";
            case BLANK -> "TEXT"; // Default type for blank cells
            default -> "TEXT";
        };
    }

    private static String determineMoreGeneralType(String currentType, String newType) {
        if (currentType == null) return newType;
        if (currentType.equals(newType)) return currentType;
        return "TEXT"; // Default to TEXT if types conflict
    }

    private static void createTable(Map<Integer, String> headers, Map<Integer, String> columnTypes, String tableName) throws SQLException {
        StringBuilder createTableSQL = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");
        for (Map.Entry<Integer, String> entry : headers.entrySet()) {
            int columnIndex = entry.getKey();
            String columnName = entry.getValue();
            String columnType = columnTypes.get(columnIndex);
            createTableSQL.append(columnName).append(" ").append(columnType).append(",");
        }
        createTableSQL.deleteCharAt(createTableSQL.length() - 1).append(")");
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             Statement statement = connection.createStatement()) {
            statement.execute(createTableSQL.toString());
        }
    }

    private static void insertData(Sheet sheet, Map<Integer, String> headers, Map<Integer, String> columnTypes) throws SQLException {
        StringBuilder insertSQL = new StringBuilder("INSERT INTO test_read_to_db (");
        for (String columnName : headers.values()) {
            insertSQL.append(columnName).append(",");
        }
        insertSQL.deleteCharAt(insertSQL.length() - 1).append(") VALUES (");
        insertSQL.append("?,".repeat(headers.size()));
        insertSQL.deleteCharAt(insertSQL.length() - 1).append(")");

        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(insertSQL.toString())) {
            connection.setAutoCommit(false);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String columnType = columnTypes.get(i);
                    setPreparedStatementValue(preparedStatement, i + 1, cell, columnType);
                }
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            connection.commit();
        }
    }

    private static void setPreparedStatementValue(PreparedStatement preparedStatement, int parameterIndex, Cell cell, String columnType) throws SQLException {
        switch (columnType) {
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
        }
    }

    private static String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}
