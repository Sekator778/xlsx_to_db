package com.pb;

import com.pb.datasource.FileSystemDataSource;
import com.pb.filereader.CsvFileReader;
import com.pb.filereader.DbfFileReader;
import com.pb.filereader.ExcelFileReader;
import com.pb.filereader.FileReader;
import com.pb.service.FileProcessingService;
import com.pb.util.DatabaseConnectionManager;
import com.pb.util.TableNameUtil;
import com.pb.writer.PostgresDatabaseWriter;
import org.apache.commons.math3.util.Pair;

import java.io.File;
import java.util.logging.Logger;

public class Application {
    private static final Logger log = Logger.getLogger(Application.class.getName());

    /**
     * Processes a file and writes its data to a PostgreSQL database.
     *
     * @param source the path to the source file
     * @throws Exception if an error occurs during file processing or database operations
     */
    public void processFile(String source, String properties) throws Exception {
        File file = new File(source);
        Pair<String, String> tableNameAndExtension = TableNameUtil.createTableNameAndExtension(file.getName());
        DatabaseConnectionManager.loadProperties(properties);
        FileReader fileReader = switch (tableNameAndExtension.getSecond().toLowerCase()) {
            case "xlsx" -> new ExcelFileReader();
            case "dbf" -> new DbfFileReader();
            case "csv" -> new CsvFileReader();
            default -> {
                log.severe("Unsupported file extension: " + tableNameAndExtension.getSecond());
                throw new IllegalArgumentException("Unsupported file extension: " + tableNameAndExtension.getSecond());
            }
        };

        FileSystemDataSource fileSystemDataSource = new FileSystemDataSource();
        PostgresDatabaseWriter databaseWriter = new PostgresDatabaseWriter();
        FileProcessingService fileProcessingService = new FileProcessingService(fileReader, fileSystemDataSource, databaseWriter);

        fileProcessingService.processFile(source, file.getName());
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            log.info("Usage: Application <filePath>");
            return;
        }

        String filePath = args[0];
        try {
            new Application().processFile(filePath, "application.yml");
        } catch (Exception e) {
            log.severe("An error occurred during file processing: " + e.getMessage());
        }
    }
}
