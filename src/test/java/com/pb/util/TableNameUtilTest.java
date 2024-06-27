package com.pb.util;

import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TableNameUtilTest {

    @Test
    public void shouldHandleFileNameWithMultiplePeriods() {
        Pair<String, String> result = TableNameUtil.createTableNameAndExtension("files_test_null__1_.xlsx");
        Assertions.assertEquals("files_test_null__1_", result.getFirst());
        Assertions.assertEquals("xlsx", result.getSecond());
    }

    @Test
    public void shouldHandleEmptyFileName() {
        assertThrows(IllegalArgumentException.class, () -> TableNameUtil.createTableNameAndExtension(""));
    }

    @Test
    public void shouldHandleNullFileName() {
        assertThrows(IllegalArgumentException.class, () -> TableNameUtil.createTableNameAndExtension(null));
    }

    @Test
    public void shouldHandleFileNameWithWordsAndSpaces() {
        Pair<String, String> result = TableNameUtil.createTableNameAndExtension("file name with spaces.txt");
        Assertions.assertEquals("file_name_with_spaces", result.getFirst());
        Assertions.assertEquals("txt", result.getSecond());
    }

    @Test
    public void shouldThrowExceptionWhenFileNameIsNull() {
        assertThrows(IllegalArgumentException.class, () -> TableNameUtil.createTableNameAndExtension(null));
    }

    @Test
    public void shouldThrowExceptionWhenFileNameIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> TableNameUtil.createTableNameAndExtension(""));
    }

    @Test
    public void shouldThrowExceptionWhenFileNameHasNoExtension() {
        assertThrows(IllegalArgumentException.class, () -> TableNameUtil.createTableNameAndExtension("test"));
    }
}