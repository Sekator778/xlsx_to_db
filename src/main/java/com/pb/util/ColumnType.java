package com.pb.util;

public enum ColumnType {
    NUMERIC("NUMERIC"),
    TIMESTAMP("TIMESTAMP"),
    BOOLEAN("BOOLEAN"),
    TEXT("TEXT");

    private final String value;

    ColumnType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public String getValue() {
        return value;
    }
}
