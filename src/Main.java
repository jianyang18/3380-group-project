import java.sql.*;
import java.util.Scanner;
import database.DatabaseConfig;
import ui.Interface;

/**
 * Application entry point
 * - Initializes database connection
 * - Creates and starts UI
 * - Handles cleanup on exit
 */
public class Main {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Connection conn = null;
        
        try {
            // Establish connection using DatabaseConfig
            System.out.println("Connecting to database...");
            conn = DatabaseConfig.getConnection();
            
            if (conn == null) {
                System.err.println("\nFailed to connect to database.");
                System.err.println("Please create auth.cfg with your credentials:\n");
                System.err.println("username=your_username");
                System.err.println("password=your_password\n");
                return;
            }
            
            System.out.println("Connected to " + DatabaseConfig.getHost() + 
                             "/" + DatabaseConfig.getDatabaseName());
            System.out.println();
            
            // Create and start UI
            Interface ui = new Interface(conn, scanner);
            ui.start();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cleanup
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                    System.out.println("Database connection closed.");
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
            scanner.close();
        }
    }
}
