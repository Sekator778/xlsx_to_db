package com.pb.util;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class CSVFileGenerator {

    public static void main(String[] args) {
        String csvFilePath = "files/test_data_large3.csv";
        int numberOfRecords = 1000000; // Adjust this value as needed to create a large file

        String[] headers = {"ID", "NAME", "SALARY", "JOIN_DATE", "ACTIVE"};

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try (CSVWriter writer = new CSVWriter(new FileWriter(csvFilePath))) {
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

            System.out.println("CSV file created successfully! with file name " + csvFilePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
