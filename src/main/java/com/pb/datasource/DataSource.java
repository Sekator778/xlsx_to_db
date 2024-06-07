package com.pb.datasource;

import java.io.InputStream;

public interface DataSource {
    InputStream getInputStream(String source) throws Exception;
}
