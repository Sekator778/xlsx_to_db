package com.pb.util;

import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TableNameUtilTest {

    @Test
    public void shouldHandleFileNameWithMultiplePeriods() {
        Pair<String, String> result = TableNameUtil.createTableNameAndExtension("files_test_null__1_.xlsx");
        Assertions.assertEquals("files_test_null__1_", result.getFirst());
        Assertions.assertEquals("xlsx", result.getSecond());
    }

    @Test
    public void shouldHandleFileNameWithoutExtension() {
        Pair<String, String> result = TableNameUtil.createTableNameAndExtension("test");
        Assertions.assertEquals("test", result.getFirst());
        Assertions.assertEquals("", result.getSecond());
    }

    @Test
    public void shouldHandleEmptyFileName() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> TableNameUtil.createTableNameAndExtension(""));
    }

    @Test
    public void shouldHandleNullFileName() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> TableNameUtil.createTableNameAndExtension(null));
    }
}