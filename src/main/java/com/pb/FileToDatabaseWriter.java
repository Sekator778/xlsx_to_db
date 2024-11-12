package com.pb;

import com.pb.filereader.CsvFileReader;
import com.pb.filereader.DbfFileReader;
import com.pb.filereader.ExcelFileReader;
import com.pb.filereader.FileReader;
import com.pb.service.FileProcessingService;
import com.pb.util.FileUtils;
import com.pb.util.TableNameUtil;
import com.pb.writer.PostgresDatabaseWriter;

import java.io.File;
import java.sql.Connection;
import java.util.logging.Logger;

public class FileToDatabaseWriter {
    private static final Logger log = Logger.getLogger(FileToDatabaseWriter.class.getName());

    /**
     * Processes a file and writes its data to a PostgreSQL database.
     *
     * @param file the file
     */
    public static void processFile(File file, Connection connection, String tableName) {
        FileUtils.FileUnzipResult fileUnzipResult = FileUtils.unzipOrProcessFile(file);
        if (fileUnzipResult == null) {
            log.severe("No valid file found.");
            return;
        }
        if (tableName == null) {
            tableName = fileUnzipResult.getFileNameWithoutExtension();
        }
        FileReader fileReader;

        String extension = fileUnzipResult.getExtension();
        switch (extension) {
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
                log.severe("Unsupported file extension: " + extension);
                return;
        }

        PostgresDatabaseWriter databaseWriter = new PostgresDatabaseWriter();
        FileProcessingService fileProcessingService = new FileProcessingService(fileReader, databaseWriter);
        try {
            fileProcessingService.processFile(fileUnzipResult.getInputStream(), tableName, extension, connection);
        } catch (Exception e) {
            log.severe("An error occurred during file processing: " + e.getMessage());
        }
    }
}
