package com.pb.filereader;

import java.io.InputStream;
import java.util.Map;

public interface FileReader {
    Map<Integer, String> readHeaders(InputStream inputStream) throws Exception;
    Map<Integer, String> determineColumnTypes(InputStream inputStream) throws Exception;
}
