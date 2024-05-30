# Excel to PostgreSQL Database Utility

## Overview

This utility reads data from an Excel file (`.xlsx` format), determines the data types of each column, creates a corresponding table in a PostgreSQL database, and inserts the data into this table. It handles various data types, including strings, numbers, dates, and booleans.

## Components

### 1. Imports and Constants
- **Imports**: Libraries for handling Excel files, database connections, and logging.
- **Constants**: Constants for the database connection and logging.

### 2. Main Method
The main method orchestrates the process:
- Reads the Excel file.
- Determines column types.
- Creates the table in the PostgreSQL database.
- Inserts data into the table.

### 3. Helper Methods

#### getCellValueAsString
Converts a cell's value to a string, handling different cell types such as strings, numbers, dates, booleans, and formulas.

#### getColumnType
Determines the SQL data type of a cell (e.g., TEXT, NUMERIC, TIMESTAMP, BOOLEAN).

#### determineMoreGeneralType
Resolves type conflicts by defaulting to text if there is a conflict between types.

#### determineColumnTypes
Iterates through the rows (excluding the header) to determine the data type for each column based on the cell values.

#### createTable
Creates the table in the PostgreSQL database using the determined column names and types. Column names are quoted to handle case sensitivity and special characters.

#### insertData
Inserts data from the Excel sheet into the created table using prepared statements and batch processing for efficiency. Handles different cell types appropriately when setting prepared statement values.

## Steps

1. **Read Headers**: The utility reads the first row of the Excel file to get the column names.
2. **Determine Column Types**: The utility iterates through the subsequent rows to determine the data type of each column (e.g., TEXT, NUMERIC, TIMESTAMP, BOOLEAN).
3. **Create Table**: A corresponding table is created in the PostgreSQL database with columns based on the headers and their determined data types. Column names are quoted to handle case sensitivity.
4. **Insert Data**: Data from the Excel file is inserted into the newly created table. The utility handles various data types appropriately to avoid type mismatch errors.

## Error Handling
The utility includes error handling to manage issues such as type mismatches and database connection errors. It logs the SQL statements used for creating the table and any exceptions that occur during execution.

## Usage
1. Place the Excel file (`.xlsx` format) in the specified directory.
2. Run the utility.
3. The utility will create a table in the PostgreSQL database and insert the data from the Excel file.

This utility is useful for automating the process of transferring data from Excel spreadsheets to a PostgreSQL database, ensuring that the data types are correctly handled and the data is efficiently inserted.