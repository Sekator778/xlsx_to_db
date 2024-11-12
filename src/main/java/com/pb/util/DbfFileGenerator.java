package com.pb.util;

import com.linuxense.javadbf.DBFDataType;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFWriter;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Random;

public class DbfFileGenerator {

    public void DBFFileGenerate(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: DbfFileGenerator <saveLocation> [<filePath>]");
            return;
        }
        String saveLocation = args[0]; // "disk" or "memory"
        String dbfFilePath = (args.length > 1) ? args[1] : "files/test_data_large.dbf"; // Default file path if not provided
        int numberOfRecords = 10000; // Adjust this value as needed to create a large file

        if ("disk".equalsIgnoreCase(saveLocation)) {
            generateDbfFileOnDisk(dbfFilePath, numberOfRecords);
        } else if ("memory".equalsIgnoreCase(saveLocation)) {
            generateDbfFileInMemory(numberOfRecords);
            System.out.println("DBF file created in memory successfully!");
        } else {
            System.out.println("Invalid save location specified. Use 'disk' or 'memory'.");
        }
    }

    public void generateDbfFileOnDisk(String dbfFilePath, int numberOfRecords) {
        try (FileOutputStream fos = new FileOutputStream(dbfFilePath);
             DBFWriter writer = new DBFWriter(fos, StandardCharsets.UTF_8)) {

            writer.setFields(getDbfFields());

            Random random = new Random();
            for (int i = 0; i < numberOfRecords; i++) {
                writer.addRecord(generateRowData(i, random));
            }

            writer.write();
            System.out.println("DBF file created successfully on disk with file name " + dbfFilePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateDbfFileInMemory(int numberOfRecords) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (DBFWriter writer = new DBFWriter(outputStream, StandardCharsets.UTF_8)) {
            writer.setFields(getDbfFields());

            Random random = new Random();
            for (int i = 0; i < numberOfRecords; i++) {
                writer.addRecord(generateRowData(i, random));
            }
            writer.write();
            System.out.println("DBF file created in memory successfully!");
        }
    }

    private static DBFField[] getDbfFields() {
        DBFField[] fields = new DBFField[5];
        fields[0] = new DBFField("ID", DBFDataType.NUMERIC, 10, 0);
        fields[1] = new DBFField("NAME", DBFDataType.CHARACTER, 50, 0);
        fields[2] = new DBFField("SALARY", DBFDataType.NUMERIC, 12, 2);
        fields[3] = new DBFField("JOIN_DATE", DBFDataType.DATE);
        fields[4] = new DBFField("ACTIVE", DBFDataType.LOGICAL);
        return fields;
    }

    private static Object[] generateRowData(int index, Random random) {
        Object[] rowData = new Object[5];
        rowData[0] = index + 1;
        rowData[1] = "Employee" + (index + 1);
        rowData[2] = 5000 + (random.nextDouble() * 10000);
        rowData[3] = new Date();
        rowData[4] = random.nextBoolean();
        return rowData;
    }
}
