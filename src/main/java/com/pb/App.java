package com.pb;

import com.pb.util.DatabaseConnectionManager;

import java.io.File;
import java.util.logging.Logger;

public class App {
    private static final Logger log = Logger.getLogger(App.class.getName());


    public static void main(String[] args) {
        if (args.length < 1) {
            log.info("Usage: Application <filePath>");
            return;
        }
        DatabaseConnectionManager.loadProperties("application.yml");

        String filePath = args[0];
        File file = new File(filePath);

        FileToDatabaseWriter.processFile(file, DatabaseConnectionManager.getConnection(), null);
    }
}
