package com.pb.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class ExcelFileGenerator {

    public void DBFFileGenerateExcel(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: ExcelFileGenerator <saveLocation> [<filePath>]");
            return;
        }
        String saveLocation = args[0]; // "disk" or "memory"
        String xlsxFilePath = (args.length > 1) ? args[1] : "files/test_data_large.xlsx"; // Default file path if not provided
        int numberOfRecords = 10000; // Adjust this value as needed to create a large file

        if ("disk".equalsIgnoreCase(saveLocation)) {
            generateXlsxFileOnDisk(xlsxFilePath, numberOfRecords);
        } else if ("memory".equalsIgnoreCase(saveLocation)) {
            generateXlsxFileInMemory(numberOfRecords);
            System.out.println("XLSX file created in memory successfully!");
        } else {
            System.out.println("Invalid save location specified. Use 'disk' or 'memory'.");
        }
    }

    public void generateXlsxFileOnDisk(String xlsxFilePath, int numberOfRecords) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");

            createHeaderRow(sheet);

            Random random = new Random();
            DecimalFormat decimalFormat = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.US));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for (int i = 0; i < numberOfRecords; i++) {
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue("Employee" + (i + 1));
                row.createCell(2).setCellValue(decimalFormat.format(5000 + (random.nextDouble() * 10000)));
                row.createCell(3).setCellValue(dateFormat.format(new Date()));
                row.createCell(4).setCellValue(random.nextBoolean());
            }

            try (FileOutputStream fos = new FileOutputStream(xlsxFilePath)) {
                workbook.write(fos);
            }

            System.out.println("XLSX file created successfully on disk with file name " + xlsxFilePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateXlsxFileInMemory(int numberOfRecords) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Sheet1");

            createHeaderRow(sheet);

            Random random = new Random();
            DecimalFormat decimalFormat = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.US));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for (int i = 0; i < numberOfRecords; i++) {
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue("Employee" + (i + 1));
                row.createCell(2).setCellValue(decimalFormat.format(5000 + (random.nextDouble() * 10000)));
                row.createCell(3).setCellValue(dateFormat.format(new Date()));
                row.createCell(4).setCellValue(random.nextBoolean());
            }

            workbook.write(outputStream);
            System.out.println("XLSX file created in memory successfully!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ID");
        headerRow.createCell(1).setCellValue("NAME");
        headerRow.createCell(2).setCellValue("SALARY");
        headerRow.createCell(3).setCellValue("JOIN_DATE");
        headerRow.createCell(4).setCellValue("ACTIVE");
    }
}

