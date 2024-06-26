package com.pb.filereader;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.pb.util.ColumnType;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.pb.util.ColumnType.*;
import static org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted;

public class ExcelFileReader implements FileReader {

    @Override
    public Map<Integer, String> readHeaders(InputStream inputStream) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            Map<Integer, String> headers = new HashMap<>();
            for (Cell cell : headerRow) {
                headers.put(cell.getColumnIndex(), cell.getStringCellValue());
            }
            return headers;
        }
    }

    @Override
    public Map<Integer, String> determineColumnTypes(InputStream inputStream) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Map<Integer, String> columnTypes = new HashMap<>();
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                for (Cell cell : row) {
                    int columnIndex = cell.getColumnIndex();
                    ColumnType columnType = getColumnType(cell);
                    columnTypes.put(columnIndex, columnType.toString());
                }
            }
            return columnTypes;
        }
    }

    /**
     * Determines the column type based on the cell value.
     * @param cell The cell to check.
     * @return The determined column type.
     */
    private ColumnType getColumnType(Cell cell) {
        return switch (cell.getCellType()) {
            case NUMERIC -> isCellDateFormatted(cell) ? TIMESTAMP : NUMERIC;
            case BOOLEAN -> BOOLEAN;
            default -> TEXT;
        };
    }
}
