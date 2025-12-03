package utils;

import java.sql.*;
import java.util.Scanner;
import utils.ResultFormatter;

/**
 * Common utility functions for query handlers
 */
public class QueryUtils {
    
    /**
     * Prompt user to return to a specific menu
     */
    public static void promptReturnToMenu(Scanner scanner, String menuName) {
        System.out.print("Press [Enter] to return to " + menuName + ": ");
        scanner.nextLine();
        System.out.println();
    }
    
    /**
     * Wait for user to press Enter to continue
     */
    public static void waitForEnter(Scanner scanner) {
        System.out.print("\nPress [Enter] to continue...");
        scanner.nextLine();
    }
    
    /**
     * Print query selection header
     */
    public static void printQueryHeader(int choice) {
        System.out.println("\n--- - - - [User selected " + choice + "] - - - ---\n");
    }
    
    /**
     * Print "You Selected:" message for a query
     */
    public static void printQuerySelection(int queryNumber, String queryName) {
        System.out.println("You Selected: [" + queryNumber + " - " + queryName + "]");
    }
    
    /**
     * Print results header
     */
    public static void printResultsHeader(String headerText) {
        System.out.println("\n--- " + headerText + " ---\n");
    }
    
    /**
     * Execute a parameterized query and display paginated results
     * @param conn Database connection
     * @param sql SQL query with placeholders
     * @param formatter Result formatter for displaying results
     * @param params Parameters to bind to the query
     */
    public static void executeQueryWithParams(Connection conn, String sql, 
                                              ResultFormatter formatter, 
                                              Object... params) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Bind parameters
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param instanceof String) {
                    pstmt.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) param);
                } else if (param instanceof Double) {
                    pstmt.setDouble(i + 1, (Double) param);
                } else if (param instanceof Long) {
                    pstmt.setLong(i + 1, (Long) param);
                }
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                formatter.displayResultsWithPagination(rs, 10);
            }
        } catch (SQLException e) {
            System.out.println("don't do sql injection");
            throw e;
        }
    }
    
    /**
     * Execute a query without parameters and display paginated results
     * Use this for queries with no user input (safe from SQL injection)
     */
    public static void executeQuery(Connection conn, String sql, 
                                   ResultFormatter formatter) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            formatter.displayResultsWithPagination(rs, 10);
        } catch (SQLException e) {
            System.out.println("don't do sql injection");
            throw e;
        }
    }
}
