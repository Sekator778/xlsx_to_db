package com.pb.util;

import com.opencsv.CSVWriter;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class CSVFileGenerator {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: CSVFileGenerator <saveLocation> [<filePath>]");
            return;
        }

        String saveLocation = args[0]; // "disk" or "memory"
        String csvFilePath = (args.length > 1) ? args[1] : "files/test_data_large3.csv"; // Default file path if not provided
        int numberOfRecords = 1000000; // Adjust this value as needed to create a large file

        if ("disk".equalsIgnoreCase(saveLocation)) {
            generateCsvFileOnDisk(csvFilePath, numberOfRecords);
        } else if ("memory".equalsIgnoreCase(saveLocation)) {
            ByteArrayOutputStream outputStream = generateCsvFileInMemory(numberOfRecords);
            // Further processing with outputStream if needed
            System.out.println("CSV file created in memory successfully!");
        } else {
            System.out.println("Invalid save location specified. Use 'disk' or 'memory'.");
        }
    }

    public static void generateCsvFileOnDisk(String csvFilePath, int numberOfRecords) {
        String[] headers = {"ID", "NAME", "SALARY", "JOIN_DATE", "ACTIVE"};

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DecimalFormat decimalFormat = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.US));

        try (CSVWriter writer = new CSVWriter(new FileWriter(csvFilePath))) {
            writer.writeNext(headers);

            Random random = new Random();
            for (int i = 0; i < numberOfRecords; i++) {
                String[] rowData = new String[5];
                rowData[0] = String.valueOf(i + 1);
                rowData[1] = "Employee" + (i + 1);
                rowData[2] = decimalFormat.format(5000 + (random.nextDouble() * 10000));
                rowData[3] = dateFormat.format(new Date());
                rowData[4] = String.valueOf(random.nextBoolean());
                writer.writeNext(rowData);
            }

            System.out.println("CSV file created successfully on disk with file name " + csvFilePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ByteArrayOutputStream generateCsvFileInMemory(int numberOfRecords) {
        String[] headers = {"ID", "NAME", "SALARY", "JOIN_DATE", "ACTIVE"};

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream))) {
            writer.writeNext(headers);

            Random random = new Random();
            for (int i = 0; i < numberOfRecords; i++) {
                String[] rowData = new String[5];
                rowData[0] = String.valueOf(i + 1);
                rowData[1] = "Employee" + (i + 1);
                rowData[2] = String.format("%.2f", 5000 + (random.nextDouble() * 10000));
                rowData[3] = dateFormat.format(new Date());
                rowData[4] = String.valueOf(random.nextBoolean());
                writer.writeNext(rowData);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputStream;
    }
}
