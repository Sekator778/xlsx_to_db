package com.pb.filereader;

import com.pb.util.ColumnType;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.pb.util.ColumnType.BOOLEAN;
import static com.pb.util.ColumnType.NUMERIC;
import static com.pb.util.ColumnType.TEXT;
import static com.pb.util.ColumnType.TIMESTAMP;
import static com.pb.util.ColumnTypeUtil.getDefaultTypeForHeader;

public class ExcelFileReader implements FileReader {
    private Map<Integer, String> headers;

    @Override
    public Map<Integer, String> readHeaders(InputStream inputStream) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            headers = new HashMap<>();
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
            int columnCount = headers.size();

            for (int colIndex = 0; colIndex < columnCount; colIndex++) {
                boolean columnDetermined = false;

                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;
                    Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    ColumnType columnType = getColumnType(cell);

                    if (columnType != TEXT) {
                        columnTypes.put(colIndex, columnType.toString());
                        columnDetermined = true;
                        break;
                    }
                }
                if (!columnDetermined) {
                    columnTypes.put(colIndex, getDefaultTypeForHeader(headers.get(colIndex)));
                }
            }
            return columnTypes;
        }
    }

    private ColumnType getColumnType(Cell cell) {
        return switch (cell.getCellType()) {
            case NUMERIC -> DateUtil.isCellDateFormatted(cell) ? TIMESTAMP : NUMERIC;
            case BOOLEAN -> BOOLEAN;
            default -> TEXT;
        };
    }
}
