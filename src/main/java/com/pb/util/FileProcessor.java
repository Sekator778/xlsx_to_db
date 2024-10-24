package com.pb.util;

import com.pb.filereader.CsvFileReader;
import com.pb.filereader.DbfFileReader;
import com.pb.filereader.ExcelFileReader;
import com.pb.filereader.FileReader;
import com.pb.service.FileProcessingService;
import com.pb.writer.PostgresDatabaseWriter;

import org.apache.commons.math3.util.Pair;

import java.io.File;
import java.sql.Connection;
import java.util.logging.Logger;

public class FileProcessor {
    private static final Logger log = Logger.getLogger(FileProcessor.class.getName());

    /**
     * Processes a file and writes its data to a PostgreSQL database.
     *
     * @param file the file
     */
    public static void processFile(File file, Connection connection) {
        Pair<String, String> tableNameAndExtension = TableNameUtil.createTableNameAndExtension(file.getName());
        FileReader fileReader;
        String fileExtension = tableNameAndExtension.getSecond().toLowerCase();

        switch (fileExtension) {
            case "xlsx":
                fileReader = new ExcelFileReader();
                break;
            case "dbf":
                fileReader = new DbfFileReader();
                break;
            case "csv":
                fileReader = new CsvFileReader();
                break;
            default:
                log.severe("Unsupported file extension: " + fileExtension);
                return;
        }

        PostgresDatabaseWriter databaseWriter = new PostgresDatabaseWriter();
        FileProcessingService fileProcessingService = new FileProcessingService(fileReader, databaseWriter);
        String fileName = tableNameAndExtension.getFirst();
        String extension = tableNameAndExtension.getSecond();
        try {
            fileProcessingService.processFile(file, fileName, extension, connection);
        } catch (Exception e) {
            log.severe("An error occurred during file processing: " + e.getMessage());
        }
    }
}
