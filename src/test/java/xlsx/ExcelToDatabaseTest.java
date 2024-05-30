package xlsx;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExcelToDatabaseTest {

    @Mock
    private FileInputStream fileInputStreamMock;

    @Mock
    private Workbook workbookMock;

    @Mock
    private Sheet sheetMock;

    @Mock
    private Row rowMock;

    @Mock
    private Cell cellMock;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldReadHeadersCorrectly() throws IOException {
        when(workbookMock.getSheetAt(0)).thenReturn(sheetMock);
        when(sheetMock.getRow(0)).thenReturn(rowMock);
        when(rowMock.cellIterator()).thenReturn(new Iterator<Cell>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < 1;
            }

            @Override
            public Cell next() {
                index++;
                return cellMock;
            }
        });
        when(cellMock.getCellType()).thenReturn(CellType.STRING);
        when(cellMock.getStringCellValue()).thenReturn("header");

        ExcelToDatabase.main(new String[]{});

        verify(cellMock, times(1)).getStringCellValue();
    }

    @Test
    public void shouldHandleNumericHeaderCorrectly() throws IOException {
        when(workbookMock.getSheetAt(0)).thenReturn(sheetMock);
        when(sheetMock.getRow(0)).thenReturn(rowMock);
        when(rowMock.cellIterator()).thenReturn(new Iterator<Cell>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < 1;
            }

            @Override
            public Cell next() {
                index++;
                return cellMock;
            }
        });
        when(cellMock.getCellType()).thenReturn(CellType.NUMERIC);
        when(cellMock.getNumericCellValue()).thenReturn(123.0);

        ExcelToDatabase.main(new String[]{});

        verify(cellMock, times(1)).getNumericCellValue();
    }

    @Test
    public void shouldSkipHeaderRowWhenInsertingData() throws IOException, SQLException {
        when(workbookMock.getSheetAt(0)).thenReturn(sheetMock);
        when(sheetMock.iterator()).thenReturn(new Iterator<Row>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < 2;
            }

            @Override
            public Row next() {
                index++;
                return rowMock;
            }
        });
        when(rowMock.getRowNum()).thenReturn(0, 1);

        ExcelToDatabase.main(new String[]{});

        verify(rowMock, times(1)).getRowNum();
    }
}