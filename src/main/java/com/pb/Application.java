package com.pb;


import com.pb.datasource.FileSystemDataSource;
import com.pb.filereader.ExcelFileReader;
import com.pb.service.FileProcessingService;
import com.pb.util.TableNameUtil;
import com.pb.writer.PostgresDatabaseWriter;

import java.io.File;

import static com.pb.util.TableNameUtil.createTableName;

public class Application {
    public static void main(String[] args) {
        try {
            // Initialize the components
            ExcelFileReader fileReader = new ExcelFileReader();
            FileSystemDataSource fileSystemDataSource = new FileSystemDataSource();
            PostgresDatabaseWriter databaseWriter = new PostgresDatabaseWriter();

            // Initialize the service with the components
            FileProcessingService fileProcessingService = new FileProcessingService(fileReader, fileSystemDataSource, databaseWriter);

            // Process the file
            String source = "files/test_data_large.xlsx";
            File file = new File(source);
            String tableName = createTableName(file.getName());
            fileProcessingService.processFile(source, tableName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
