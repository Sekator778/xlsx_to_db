package com.pb.util;

import com.linuxense.javadbf.DBFDataType;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Random;

public class DbfFileGenerator {

    public static void main(String[] args) {
        String dbfFilePath = "files/test_data_large.dbf";
        int numberOfRecords = 10000; // Adjust this value as needed to create a large file

        try (FileOutputStream fos = new FileOutputStream(dbfFilePath);
             DBFWriter writer = new DBFWriter(fos, StandardCharsets.UTF_8)) {

            DBFField[] fields = new DBFField[5];

            fields[0] = new DBFField("ID", DBFDataType.NUMERIC, 10, 0);
            fields[1] = new DBFField("NAME", DBFDataType.CHARACTER, 50, 0);
            fields[2] = new DBFField("SALARY", DBFDataType.NUMERIC, 12, 2);
            fields[3] = new DBFField("JOIN_DATE", DBFDataType.DATE);
            fields[4] = new DBFField("ACTIVE", DBFDataType.LOGICAL);

            writer.setFields(fields);

            Random random = new Random();
            for (int i = 0; i < numberOfRecords; i++) {
                Object[] rowData = new Object[5];
                rowData[0] = i + 1;
                rowData[1] = "Employee" + (i + 1);
                rowData[2] = 5000 + (random.nextDouble() * 10000);
                rowData[3] = new Date();
                rowData[4] = random.nextBoolean();
                writer.addRecord(rowData);
            }

            writer.write();
            System.out.println("DBF file created successfully!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
