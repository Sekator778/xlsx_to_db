package com.pb;

import com.pb.util.CSVFileGenerator;
import com.pb.util.DatabaseConnectionManager;
import com.pb.util.TableNameUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationTest {

    @BeforeAll
    static void setup() {
        DatabaseConnectionManager.loadProperties("application-test.yml");
    }

    @Test
    void testCSV() throws Exception {
        File tempFile = generateTempCsvFile();

        new Application().processFile(tempFile.getAbsolutePath());

        String tableName = TableNameUtil.createTableNameAndExtension(tempFile.getName()).getFirst();

        try (Connection connection = DatabaseConnectionManager.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {
            assertTrue(rs.next(), "Result set should have at least one row");

            // Check the first row's data
            assertEquals("1", rs.getString("ID"));
            assertEquals("Employee1", rs.getString("NAME"));
            assertTrue(rs.getDouble("SALARY") >= 5000 && rs.getDouble("SALARY") <= 15000);
            assertNotNull(rs.getTimestamp("JOIN_DATE"));
            assertTrue(rs.getBoolean("ACTIVE") || !rs.getBoolean("ACTIVE"));

            for (int i = 2; i <= 10; i++) {
                assertTrue(rs.next(), "Result set should have at least " + i + " rows");
                assertEquals(String.valueOf(i), rs.getString("ID"));
                assertEquals("Employee" + i, rs.getString("NAME"));
                assertTrue(rs.getDouble("SALARY") >= 5000 && rs.getDouble("SALARY") <= 15000);
                assertNotNull(rs.getTimestamp("JOIN_DATE"));
                assertTrue(rs.getBoolean("ACTIVE") || !rs.getBoolean("ACTIVE"));
            }
        }

        tempFile.delete();
    }

    private File generateTempCsvFile() throws IOException {
        File tempFile = File.createTempFile("test", ".csv");
        CSVFileGenerator.generateCsvFileOnDisk(tempFile.getAbsolutePath(), 10); // Generate 10 records for testing
        return tempFile;
    }
}
