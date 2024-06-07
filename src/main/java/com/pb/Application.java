package com.pb;

import com.pb.datasource.FileSystemDataSource;
import com.pb.filereader.CsvFileReader;
import com.pb.filereader.DbfFileReader;
import com.pb.filereader.ExcelFileReader;
import com.pb.filereader.FileReader;
import com.pb.service.FileProcessingService;
import com.pb.util.TableNameUtil;
import com.pb.writer.PostgresDatabaseWriter;

import org.apache.commons.math3.util.Pair;

import java.io.File;
import java.util.logging.Logger;

public class Application {
    private static final Logger log = Logger.getLogger(Application.class.getName());

    public static void main(String[] args) {
        try {
            if (args.length != 1) {
                log.severe("Please provide the path to the file as an argument.");
                return;
            }

            String source = args[0];
            File file = new File(source);
            Pair<String, String> tableNameAndExtension = TableNameUtil.createTableNameAndExtension(file.getName());

            FileReader fileReader;
            switch (tableNameAndExtension.getSecond().toLowerCase()) {
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
                    log.severe("Unsupported file extension: " + tableNameAndExtension.getSecond());
                    return;
            }

            FileSystemDataSource fileSystemDataSource = new FileSystemDataSource();
            PostgresDatabaseWriter databaseWriter = new PostgresDatabaseWriter();
            FileProcessingService fileProcessingService = new FileProcessingService(fileReader, fileSystemDataSource, databaseWriter);

            fileProcessingService.processFile(source, file.getName());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
