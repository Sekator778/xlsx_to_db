package com.pb.filereader;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ExcelFileReader implements FileReader {

    @Override
    public Map<Integer, String> readHeaders(InputStream inputStream) throws Exception {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Row headerRow = sheet.getRow(0);
        Map<Integer, String> headers = new HashMap<>();
        for (Cell cell : headerRow) {
            headers.put(cell.getColumnIndex(), cell.getStringCellValue());
        }
        workbook.close();
        return headers;
    }

    @Override
    public Map<Integer, String> determineColumnTypes(InputStream inputStream) throws Exception {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Map<Integer, String> columnTypes = new HashMap<>();
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;
            for (Cell cell : row) {
                int columnIndex = cell.getColumnIndex();
                String currentType = columnTypes.get(columnIndex);
                String newType = getColumnType(cell);
                columnTypes.put(columnIndex, determineMoreGeneralType(currentType, newType));
            }
        }
        workbook.close();
        return columnTypes;
    }

    private String getColumnType(Cell cell) {
        return switch (cell.getCellType()) {
            case NUMERIC -> DateUtil.isCellDateFormatted(cell) ? "TIMESTAMP" : "NUMERIC";
            case BOOLEAN -> "BOOLEAN";
            case BLANK -> "TEXT";
            default -> "TEXT";
        };
    }

    private String determineMoreGeneralType(String currentType, String newType) {
        if (currentType == null) return newType;
        if (currentType.equals(newType)) return currentType;
        return "TEXT";
    }
}
