# File to PostgreSQL Database Utility

## Overview

This utility reads data from various file formats (`.xlsx`, `.dbf`, `.csv`), determines the data types of each column, creates a corresponding table in a PostgreSQL database, and inserts the data into this table. It handles various data types, including strings, numbers, dates, and booleans.

## Components

### 1. Imports and Dependencies

- **Libraries**:
    - `org.apache.poi` for handling Excel files.
    - `com.github.albfernandez.javadbf` for handling DBF files.
    - `com.opencsv` for handling CSV files.
    - `java.sql` for database connections.
    - `java.util.logging` for logging.

### 2. Main Application(only for example)

The `Application` class orchestrates the process:
- Identifies the file type based on the file extension.
- Initializes the appropriate file reader.
- Reads the file, determines column types, creates the table, and inserts data into the PostgreSQL database.

### 3. File Readers

#### ExcelFileReader
Handles reading Excel files (`.xlsx`):
- **readHeaders**: Reads the first row to get column names.
- **determineColumnTypes**: Determines data types for each column.

#### DbfFileReader
Handles reading DBF files (`.dbf`):
- **readHeaders**: Reads the column names from the DBF file.
- **determineColumnTypes**: Determines data types for each column.

#### CsvFileReader
Handles reading CSV files (`.csv`):
- **readHeaders**: Reads the first row to get column names.
- **determineColumnTypes**: Determines data types for each column.

### 4. Database Writer

#### PostgresDatabaseWriter
Handles writing data to a PostgreSQL database:
- **createTable**: Creates a table with the determined column names and types.
- **insertData**: Inserts data from the file into the table.

### 5. Utility Classes

#### TableNameUtil
Generates a table name and extracts file extension:
- **createTableNameAndExtension**: Returns a pair with the table name and file extension.

#### FileProcessingService
Coordinates the process of reading the file and writing to the database:
- **processFile**: Reads headers, determines column types, creates the table, and inserts data.

### Usage

1. **Prepare the Environment**:
    - Ensure PostgreSQL is running and accessible.
    - Place the input file (`.xlsx`, `.dbf`, `.csv`) in the specified directory.

2. **Run the Application**:
    - Execute the main class with the file path as an argument.

   ```sh
   java -cp .:path/to/dependencies/* com.pb.App path/to/yourfile.xlsx
   ```

3. **Check the PostgreSQL Database**:
    - Verify the table is created and the data is inserted correctly.

### Error Handling

The utility includes error handling to manage issues such as type mismatches and database connection errors. It logs the SQL statements used for creating the table and any exceptions that occur during execution.

### Summary

This utility automates the process of transferring data from various file formats to a PostgreSQL database, ensuring that the data types are correctly handled and the data is efficiently inserted.