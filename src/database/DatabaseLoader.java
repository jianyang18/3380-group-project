package database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * database population utility
 */
public class DatabaseLoader {

    /**
     * Load a SQL file into the database
     * 
     * @param sqlFilePath Path to the .sql file
     * @param conn Active database connection
     * @return true if successful, false otherwise
     */
    public static boolean loadSqlFile(String sqlFilePath, Connection conn) {
        System.out.println("Reading " + sqlFilePath + "...");
        
        try (Statement stmt = conn.createStatement();
             BufferedReader reader = new BufferedReader(new FileReader(sqlFilePath))) {

            StringBuilder command = new StringBuilder();
            String line;
            long lineCount = 0;
            int batchCount = 0;

            // Read file line-by-line
            while ((line = reader.readLine()) != null) {
                lineCount++;
                String trimmed = line.trim();

                // Skip comments and empty lines
                // NOTE: -- is comments in the sql file
                if (trimmed.startsWith("--") || trimmed.isEmpty()) {
                    continue;
                }

                // Handle "GO" batch separator
                // T-SQL uses "GO" to separate batches. JDBC doesn't understand it.
                // When we hit "GO", we send the accumulated SQL to the server.
                if (trimmed.equalsIgnoreCase("GO")) {
                    if (command.length() > 0) {
                        try {
                            stmt.execute(command.toString());
                            batchCount++;
                            
                            // Progress indicator (so the user knows it's still populating)
                            System.out.print(".");
                            if (lineCount % 1000 == 0) {
                                System.out.println(" (" + lineCount + " lines, " + batchCount + " batches)");
                            }
                        } catch (SQLException e) {
                            System.out.println("\nError executing batch at line " + lineCount + ":");
                            System.out.println(e.getMessage());
                        }
                        command.setLength(0); // Clear buffer
                    }
                } else {
                    // Add line to current batch
                    command.append(line).append("\n");
                }
            }
            
            // Execute anything left at the end
            if (command.length() > 0) {
                stmt.execute(command.toString());
                batchCount++;
            }

            System.out.println("\nDatabase populated successfully!");
            System.out.println("  Processed " + lineCount + " lines in " + batchCount + " batches");
            return true;

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return false;
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            return false;
        }
    }
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java database.DatabaseLoader <sql_file_path>");
            System.out.println("Example: java database.DatabaseLoader resources/create_smaller_db.sql");
            return;
        }
        
        String sqlFilePath = args[0];
        
        // Connect to database
        System.out.println("Connecting to database...");
        Connection conn = DatabaseConfig.getConnection();
        
        if (conn == null) {
            System.err.println("Failed to connect to database. Check auth.cfg");
            return;
        }
        
        System.out.println("Connected to " + DatabaseConfig.getHost() + 
                         "/" + DatabaseConfig.getDatabaseName());
        
        // Load SQL file
        boolean success = loadSqlFile(sqlFilePath, conn);
        
        // Cleanup
        try {
            conn.close();
            System.out.println("Connection closed");
        } catch (SQLException e) {
            System.err.println("Warning: Error closing connection: " + e.getMessage());
        }
        
        if (success) {
            System.out.println("\n=== DONE ===");
        } else {
            System.err.println("\n=== FAILED ===");
            System.exit(1);
        }
    }
}
