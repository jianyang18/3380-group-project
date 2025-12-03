package logic;

import java.sql.*;
import java.util.Scanner;
import utils.ResultFormatter;
import utils.QueryUtils;

/**
 * Base class for all query handlers
 * Provides common menu display and query execution logic
 */
public abstract class QueryHandler {
    protected Connection conn;
    protected Scanner scanner;
    protected ResultFormatter formatter;
    
    public QueryHandler(Connection conn, Scanner scanner) {
        this.conn = conn;
        this.scanner = scanner;
        this.formatter = new ResultFormatter(scanner);
    }
    
    /**
     * Display the query menu and handle user selection
     */
    public void showMenu() {
        while (true) {
            printMenu();
            System.out.print("Select an option and press ENTER: ");
            
            String input = scanner.nextLine().trim();
            
            try {
                int choice = Integer.parseInt(input);
                
                if (choice == 99) {
                    System.out.println("Returning to main menu...\n");
                    break;
                }
                
                executeQueryWithErrorHandling(choice);
                
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid number.\n");
            }
        }
    }
    
    /**
     * Execute query with standardized error handling and menu prompts
     */
    private void executeQueryWithErrorHandling(int choice) {
        QueryUtils.printQueryHeader(choice);
        
        try {
            executeQuery(choice);
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage() + "\n");
        }
        
        QueryUtils.promptReturnToMenu(scanner, getMenuName());
    }
    
    /**
     * Print the specific menu for this query handler
     */
    protected abstract void printMenu();
    
    /**
     * Execute the query corresponding to the user's choice
     */
    protected abstract void executeQuery(int choice) throws SQLException;
    
    /**
     * Get the name of this menu for the return prompt
     */
    protected abstract String getMenuName();
}
