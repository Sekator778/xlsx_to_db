package com.pb.util;

public class EscapeUtil {
    public static String sanitizeHeader(String header) {
        return header.replace(" ", "_");
    }
}
