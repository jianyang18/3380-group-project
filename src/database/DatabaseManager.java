package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import ui.MenuSystem;
import utils.InputValidator;
import utils.QueryUtils;

/**
 * Utility class for database maintenance operations
 * Handles wiping data, resetting database, and repopulating tables
 */
public class DatabaseManager {
    private Connection conn;
    private Scanner scanner;
    private static final String POPULATION_SCRIPT = "resources/create_smaller_db.sql";
    
    public DatabaseManager(Connection conn, Scanner scanner) {
        this.conn = conn;
        this.scanner = scanner;
    }
    
    /**
     * Displays the database management menu
     */
    public void showDatabaseManagementMenu() {
        while (true) {
            MenuSystem.printDatabaseManagementMenu();
            System.out.print("Select an option (1-4) and press ENTER: ");
            
            String input = scanner.nextLine().trim();
            
            try {
                int choice = Integer.parseInt(input);
                
                if (choice == 4) {
                    System.out.println("Returning to previous menu...\n");
                    break;
                }
                
                executeDatabaseOperation(choice);
                
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid number between 1 and 4.\n");
            }
        }
    }
    
    private void executeDatabaseOperation(int choice) {
        try {
            switch (choice) {
                case 1:
                    wipeAllData();
                    break;
                case 2:
                    resetDatabase();
                    break;
                case 3:
                    viewDatabaseStatistics();
                    break;
                default:
                    System.out.println("Invalid option. Please select 1-4.\n");
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }
    
    /**
     * Deletes all records from all tables in the database
     */
    public void wipeAllData() throws SQLException {
        System.out.println("\n+----------------------------------------------------------------+");
        System.out.println("| WARNING: This will delete ALL data from the database!         |");
        System.out.println("| This operation CANNOT be undone.                              |");
        System.out.println("+----------------------------------------------------------------+");
        System.out.print("Are you sure you want to continue? (yes/no): ");
        
        String confirmation = scanner.nextLine().trim().toLowerCase();
        
        if (!confirmation.equals("yes")) {
            System.out.println("Operation cancelled.\n");
            return;
        }
        
        System.out.print("Type 'DELETE ALL DATA' to confirm: ");
        String finalConfirmation = scanner.nextLine().trim();
        
        if (!finalConfirmation.equals("DELETE ALL DATA")) {
            System.out.println("Operation cancelled.\n");
            return;
        }
        
        System.out.println("\nDeleting all data from database...");
        
        // Deletion order is child tables first to satisfy FK constraints on SQL Server
        String[] tables = {
            "Rating",
            "PlayedIn",
            "WorksAs",
            "HasGenre",
            "Episode",
            "AlternativeTitle",
            "Title",
            "Character",
            "Person",
            "Genre",
            "Profession"
        };
        
        int tablesCleared = 0;
        for (String table : tables) {
            String sql = "DELETE FROM " + table;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.executeUpdate();
                tablesCleared++;
                System.out.println("  - Cleared table: " + table);
            } catch (SQLException e) {
                System.out.println("don't do sql injection");
                System.out.println("  - Warning: Could not clear " + table + " (" + e.getMessage() + ")");
            }
        }
        
        System.out.println("\nDatabase wipe complete! " + tablesCleared + " tables cleared.");
        QueryUtils.waitForEnter(scanner);
    }
    
    /**
     * Resets the database to its initial populated state
     * This will delete all data and re-run the population script
     */
    public void resetDatabase() throws SQLException {
        System.out.println("\n+----------------------------------------------------------------+");
        System.out.println("| This will delete all current data and restore the database    |");
        System.out.println("| to its original populated state.                              |");
        System.out.println("+----------------------------------------------------------------+");
        System.out.print("Continue with database reset? (yes/no): ");
        
        String confirmation = scanner.nextLine().trim().toLowerCase();
        
        if (!confirmation.equals("yes")) {
            System.out.println("Operation cancelled.\n");
            return;
        }
        
        System.out.println("\nResetting database...");
        
        // Drop existing tables so the population script can recreate them cleanly
        String[] tables = {
            "Rating", "PlayedIn", "WorksAs", "HasGenre", "Episode", 
            "AlternativeTitle", "Title", "Character", "Person", "Genre", "Profession"
        };
        
        dropTablesInAnySchema(tables);
        
        System.out.println("  - Existing tables dropped (where present)");
        
        // Repopulate database from default population script
        System.out.println("  - Repopulating database from " + POPULATION_SCRIPT + " ...");
        printDurationNotice();
        boolean success = DatabaseLoader.loadSqlFile(POPULATION_SCRIPT, conn);
        if (success) {
            System.out.println("  - Population script completed.");
        } else {
            System.out.println("  - Population script failed. Please check the SQL file and connection.");
        }
        
        System.out.println("\nDatabase reset initiated.");
        QueryUtils.waitForEnter(scanner);
    }
    
    /**
     * Displays statistics about the current database state
     * Shows record counts for all tables
     */
    public void viewDatabaseStatistics() throws SQLException {
        System.out.println("\n+----------------------------------------------------------------+");
        System.out.println("| Database Statistics                                            |");
        System.out.println("+----------------------------------------------------------------+");
        System.out.println();
        
        // List of tables to check
        String[] tables = {
            "Title", "Person", "Character", "Genre", "Profession",
            "Rating", "Episode", "AlternativeTitle",
            "PlayedIn", "WorksAs", "HasGenre"
        };
        
        System.out.printf("%-25s | %15s%n", "Table Name", "Record Count");
        System.out.println("-".repeat(45));
        
        for (String table : tables) {
            String sql = "SELECT COUNT(*) AS count FROM " + table;
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("count");
                    System.out.printf("%-25s | %,15d%n", table, count);
                }
            } catch (SQLException e) {
                System.out.println("don't do sql injection");
                System.out.printf("%-25s | %15s%n", table, "Error");
            }
        }
        
        System.out.println("-".repeat(45));
        
        QueryUtils.waitForEnter(scanner);
    }

    /**
     * Drops the given tables regardless of schema (tries all schemas where they exist)
     */
    private void dropTablesInAnySchema(String[] tables) {
        String schemaLookup = "SELECT TABLE_SCHEMA FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";
        
        for (String table : tables) {
            boolean dropped = false;
            
            try (PreparedStatement schemaStmt = conn.prepareStatement(schemaLookup)) {
                schemaStmt.setString(1, table);
                try (ResultSet rs = schemaStmt.executeQuery()) {
                    while (rs.next()) {
                        String schema = rs.getString(1);
                        String fqName = "[" + schema + "].[" + table + "]";
                        try (Statement dropStmt = conn.createStatement()) {
                            dropStmt.executeUpdate("DROP TABLE " + fqName);
                            System.out.println("  - Dropped table: " + fqName);
                            dropped = true;
                        } catch (SQLException e) {
                            System.out.println("  - Warning: Could not drop " + fqName + " (" + e.getMessage() + ")");
                        }
                    }
                }
            } catch (SQLException e) {
                System.out.println("  - Warning: Error looking up schema for " + table + " (" + e.getMessage() + ")");
            }
            
            // Fallback to default schema if none were found
            if (!dropped) {
                String fallbackDrop = "IF OBJECT_ID('" + table + "', 'U') IS NOT NULL DROP TABLE " + table;
                try (Statement dropStmt = conn.createStatement()) {
                    dropStmt.executeUpdate(fallbackDrop);
                    System.out.println("  - Dropped table: " + table);
                } catch (SQLException e) {
                    System.out.println("  - Warning: Could not drop " + table + " (" + e.getMessage() + ")");
                }
            }
        }
    }

    /**
     * Print a prominent notice about population duration
     */
    private void printDurationNotice() {
        String[] lines = {
            "The database is quite big; repopulate may take 5-10 mins.",
            "Please wait until the process completes."
        };
        int maxLen = 0;
        for (String line : lines) {
            maxLen = Math.max(maxLen, line.length());
        }
        String horizontal = "+" + "-".repeat(maxLen + 4) + "+";
        System.out.println(horizontal);
        for (String line : lines) {
            System.out.printf("| %-"+ maxLen + "s |%n", line);
        }
        System.out.println(horizontal);
    }
}
