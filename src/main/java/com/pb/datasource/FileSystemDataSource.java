package com.pb.datasource;

import java.io.FileInputStream;
import java.io.InputStream;

public class FileSystemDataSource implements DataSource {
    @Override
    public InputStream getInputStream(String source) throws Exception {
        return new FileInputStream(source);
    }
}
