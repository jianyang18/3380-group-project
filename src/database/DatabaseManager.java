package database;

import java.sql.*;
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
        
        // Disable foreign key checks temporarily
        Statement stmt = conn.createStatement();
        stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
        
        // List of all tables to truncate in order to handle dependencies
        String[] tables = {
            "Rating",
            "KnownFor",
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
            try {
                stmt.execute("DELETE FROM " + table);
                tablesCleared++;
                System.out.println("  - Cleared table: " + table);
            } catch (SQLException e) {
                System.out.println("  - Warning: Could not clear " + table + " (" + e.getMessage() + ")");
            }
        }
        
        // Re-enable foreign key checks
        stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        stmt.close();
        
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
        
        // First, wipe all existing data
        Statement stmt = conn.createStatement();
        stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
        
        String[] tables = {
            "Rating", "KnownFor", "PlayedIn", "WorksAs", "HasGenre", "Episode", 
            "AlternativeTitle", "Title", "Character", "Person", "Genre", "Profession"
        };
        
        for (String table : tables) {
            try {
                stmt.execute("DELETE FROM " + table);
            } catch (SQLException e) {
                // Continue even if deletion fails
            }
        }
        
        stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        
        System.out.println("  - All existing data cleared");
        
        // TODO: Call the population script/method to repopulate the database
        
        System.out.println("  - Repopulating database from source files...");
        System.out.println("\nNOTE: Population script integration pending.");
        System.out.println("      Please run the population script separately.");
        
        stmt.close();
        
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
        
        Statement stmt = conn.createStatement();
        
        // List of tables to check
        String[] tables = {
            "Title", "Person", "Character", "Genre", "Profession",
            "Rating", "Episode", "AlternativeTitle",
            "PlayedIn","WorksAs", "HasGenre", "KnownFor"
        };
        
        System.out.printf("%-25s | %15s%n", "Table Name", "Record Count");
        System.out.println("-".repeat(45));
        
        for (String table : tables) {
            try {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM " + table);
                if (rs.next()) {
                    int count = rs.getInt("count");
                    System.out.printf("%-25s | %,15d%n", table, count);
                }
                rs.close();
            } catch (SQLException e) {
                System.out.printf("%-25s | %15s%n", table, "Error");
            }
        }
        
        System.out.println("-".repeat(45));
        
        stmt.close();
        
        QueryUtils.waitForEnter(scanner);
    }
        
    /**
     * Executes a SQL script file to populate the database
     * @param scriptPath Path to the SQL script file
     */
    private void executeSQLScript(String scriptPath) throws SQLException {
        // TODO
        System.out.println("Executing script: " + scriptPath);
    }
    
    /**
     * Creates a backup of the current database state
     */
    public void createBackup() throws SQLException {
        // TODO
    }
    
    /**
     * Restores database from a backup file
     * @param backupPath Path to the backup file
     */
    public void restoreFromBackup(String backupPath) throws SQLException {
        // TODO
    }
}
