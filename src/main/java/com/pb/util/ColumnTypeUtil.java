package com.pb.util;

import static com.pb.util.ColumnType.*;

public class ColumnTypeUtil {

    /**
     * Returns the default column type for a given header.
     *
     * @param header The column header.
     * @return The default column type.
     */
    public static String getDefaultTypeForHeader(String header) {
        if (header == null) {
            return TEXT.toString();
        } else if (header.equalsIgnoreCase("FIO")) {
            return TEXT.toString();
        } else if (header.toLowerCase().contains("id")) {
            return INTEGER.toString();
        } else {
            return TEXT.toString();
        }
    }
}
