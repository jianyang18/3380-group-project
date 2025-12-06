package utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Utility class for formatting and displaying query results
 * Handles pagination, column alignment, text overflow, and vertical card view
 */
public class ResultFormatter {
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_COLUMN_WIDTH = 25;
    private static final int MIN_COLUMN_WIDTH = 6;
    
    private Scanner scanner;
    
    public ResultFormatter() {
        this.scanner = new Scanner(System.in);
    }
    
    public ResultFormatter(Scanner scanner) {
        this.scanner = scanner;
    }
    
    // ============================================================================
    // TABLE FORMAT WITH PAGINATION
    // This technique required us to do a bit of online research of "how to handle large query result"
    // , but we've learned how to do it pretty well now
    // ============================================================================
    
    /**
     * Displays a ResultSet in a formatted table with pagination
     * @param rs The ResultSet to display
     * @param pageSize Number of rows per page (default 10)
     */
    public void displayResultsWithPagination(ResultSet rs, int pageSize) throws SQLException {
        if (pageSize <= 0) {
            pageSize = DEFAULT_PAGE_SIZE; 
        }
        
        ResultSetMetaData metadata = rs.getMetaData();
        int columnCount = metadata.getColumnCount();
        
        // Get column names and calculate widths
        String[] columnNames = new String[columnCount];
        int[] columnWidths = new int[columnCount];
        
        for (int i = 0; i < columnCount; i++) {
            columnNames[i] = metadata.getColumnLabel(i + 1);
            columnWidths[i] = Math.max(MIN_COLUMN_WIDTH, 
                                       Math.min(MAX_COLUMN_WIDTH, columnNames[i].length() + 2));
        }
        
        // Collect all rows
        List<String[]> rows = new ArrayList<>();
        while (rs.next()) {
            String[] row = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                String value = rs.getString(i + 1);
                row[i] = (value == null) ? "NULL" : value;
                
                // Adjust column width based on data
                columnWidths[i] = Math.max(columnWidths[i], 
                                          Math.min(MAX_COLUMN_WIDTH, row[i].length() + 2));
            }
            rows.add(row);
        }
        
        if (rows.isEmpty()) {
            System.out.println("No results found.");
            return;
        }
        
        // Display results with pagination
        int totalRows = rows.size();
        int totalPages = (int) Math.ceil((double) totalRows / pageSize);
        int currentPage = 1;
        
        while (currentPage <= totalPages) {
            // Display current page
            displayPage(rows, columnNames, columnWidths, currentPage, pageSize, totalPages);
            
            // Show pagination controls
            if (currentPage < totalPages) {
                System.out.println("\n--- Showing rows " + 
                                 ((currentPage - 1) * pageSize + 1) + "-" + 
                                 Math.min(currentPage * pageSize, totalRows) + 
                                 " of " + totalRows + " (Page " + currentPage + "/" + totalPages + ") ---");
                System.out.print("Press [Enter] for next page, [q] to quit, [r] to restart: ");
                
                String input = scanner.nextLine().trim().toLowerCase();
                
                if (input.equals("q")) {
                    break;
                } else if (input.equals("r")) {
                    currentPage = 1;
                } else {
                    currentPage++;
                }
            } else {
                System.out.println("\n--- End of results (" + totalRows + " total rows) ---");
                break;
            }
        }
    }
    
    /**
     * Displays one page of results
     */
    private void displayPage(List<String[]> rows, String[] columnNames, int[] columnWidths, 
                            int pageNumber, int pageSize, int totalPages) {
        int startIndex = (pageNumber - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, rows.size());
        
        // Print header border
        printBorder(columnWidths);
        
        // Print column headers
        printRow(columnNames, columnWidths);
        
        // Print header border
        printBorder(columnWidths);
        
        // Print data rows
        for (int i = startIndex; i < endIndex; i++) {
            String[] row = rows.get(i);
            printRow(row, columnWidths);
        }
        
        // Print bottom border
        printBorder(columnWidths);
    }
    
    /**
     * Prints a border line
     */
    private void printBorder(int[] columnWidths) {
        System.out.print("+");
        for (int width : columnWidths) {
            System.out.print("-".repeat(width) + "+");
        }
        System.out.println();
    }
    
    /**
     * Prints a single row with proper column alignment
     */
    private void printRow(String[] values, int[] columnWidths) {
        System.out.print("|");
        for (int i = 0; i < values.length; i++) {
            String value = truncateString(values[i], columnWidths[i] - 2);
            System.out.print(" " + padRight(value, columnWidths[i] - 2) + " |");
        }
        System.out.println();
    }
    
    // ============================================================================
    // VERTICAL CARD VIEW (for wide tables)
    // ============================================================================
    
    /**
     * Displays ResultSet in vertical card view format
     * Useful when table is too wide to fit on screen
     */
    public void displayResultsVertical(ResultSet rs, int pageSize) throws SQLException {
        if (pageSize <= 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        
        ResultSetMetaData metadata = rs.getMetaData();
        int columnCount = metadata.getColumnCount();
        
        // Get column names
        String[] columnNames = new String[columnCount];
        int maxLabelWidth = 0;
        
        for (int i = 0; i < columnCount; i++) {
            columnNames[i] = metadata.getColumnLabel(i + 1);
            maxLabelWidth = Math.max(maxLabelWidth, columnNames[i].length());
        }
        
        // Collect all rows
        List<String[]> rows = new ArrayList<>();
        while (rs.next()) {
            String[] row = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                String value = rs.getString(i + 1);
                row[i] = (value == null) ? "NULL" : value;
            }
            rows.add(row);
        }
        
        if (rows.isEmpty()) {
            System.out.println("No results found.");
            return;
        }
        
        // Display results with pagination
        int totalRows = rows.size();
        int totalPages = (int) Math.ceil((double) totalRows / pageSize);
        int currentPage = 1;
        
        while (currentPage <= totalPages) {
            int startIndex = (currentPage - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, rows.size());
            
            // Display each record as a card
            for (int i = startIndex; i < endIndex; i++) {
                System.out.println("\n+--- Record " + (i + 1) + " ---");
                String[] row = rows.get(i);
                
                for (int j = 0; j < columnCount; j++) {
                    System.out.printf("| %-" + maxLabelWidth + "s : %s%n", 
                                     columnNames[j], row[j]);
                }
                System.out.println("+---" + "-".repeat(maxLabelWidth + 5));
            }
            
            // Show pagination controls
            if (currentPage < totalPages) {
                System.out.println("\n--- Showing records " + 
                                 ((currentPage - 1) * pageSize + 1) + "-" + 
                                 Math.min(currentPage * pageSize, totalRows) + 
                                 " of " + totalRows + " (Page " + currentPage + "/" + totalPages + ") ---");
                System.out.print("Press [Enter] for next page, [q] to quit: ");
                
                String input = scanner.nextLine().trim().toLowerCase();
                if (input.equals("q")) {
                    break;
                }
                currentPage++;
            } else {
                System.out.println("\n--- End of results (" + totalRows + " total records) ---");
                break;
            }
        }
    }
    
    // ============================================================================
    // SIMPLE DISPLAY (no pagination)
    // ============================================================================
    
    /**
     * Displays all results without pagination (use for small result sets)
     */
    public void displayAllResults(ResultSet rs) throws SQLException {
        ResultSetMetaData metadata = rs.getMetaData();
        int columnCount = metadata.getColumnCount();
        
        // Get column names and widths
        String[] columnNames = new String[columnCount];
        int[] columnWidths = new int[columnCount];
        
        for (int i = 0; i < columnCount; i++) {
            columnNames[i] = metadata.getColumnLabel(i + 1);
            columnWidths[i] = Math.max(MIN_COLUMN_WIDTH, columnNames[i].length() + 2);
        }
        
        // Collect rows and adjust widths
        List<String[]> rows = new ArrayList<>();
        while (rs.next()) {
            String[] row = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                String value = rs.getString(i + 1);
                row[i] = (value == null) ? "NULL" : value;
                columnWidths[i] = Math.max(columnWidths[i], 
                                          Math.min(MAX_COLUMN_WIDTH, row[i].length() + 2));
            }
            rows.add(row);
        }
        
        if (rows.isEmpty()) {
            System.out.println("No results found.");
            return;
        }
        
        // Print table
        printBorder(columnWidths);
        printRow(columnNames, columnWidths);
        printBorder(columnWidths);
        
        for (String[] row : rows) {
            printRow(row, columnWidths);
        }
        
        printBorder(columnWidths);
        System.out.println("\nTotal rows: " + rows.size());
    }
    
    // ============================================================================
    // UTILITY METHODS
    // ============================================================================
    
    /**
     * Truncates a string to fit within specified width, adding ellipsis if needed
     */
    private String truncateString(String str, int maxWidth) {
        if (str == null) {
            return "NULL";
        }
        if (str.length() <= maxWidth) {
            return str;
        }
        if (maxWidth <= 3) {
            return str.substring(0, Math.max(0, maxWidth));
        }
        return str.substring(0, maxWidth - 3) + "...";
    }
    
    /**
     * Pads a string with spaces to reach specified width
     */
    private String padRight(String str, int width) {
        if (str.length() >= width) {
            return str;
        }
        return str + " ".repeat(width - str.length());
    }
    
    /**
     * Displays a simple count result
     */
    public void displayCount(int count, String label) {
        System.out.println("+------------------------------------------+");
        System.out.printf("| %-40s |%n", label);
        System.out.println("+------------------------------------------+");
        System.out.printf("| Count: %-31d |%n", count);
        System.out.println("+------------------------------------------+");
    }
    
    /**
     * Displays a message in a formatted box
     */
    public void displayMessage(String message) {
        int length = message.length() + 4;
        System.out.println("+" + "-".repeat(length) + "+");
        System.out.println("| " + message + " |");
        System.out.println("+" + "-".repeat(length) + "+");
    }
}
