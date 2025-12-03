package ui;

import java.sql.*;
import utils.QueryUtils;

/**
 * component for displaying database statistics
 */
public class StatisticsDisplay {
    
    public static void showStatistics(Connection conn) {
        System.out.println("\n+----------------------------------------------------------+");
        System.out.println("|                    Database Statistics                   |");
        System.out.println("+----------------------------------------------------------+");
        
        String[][] queries = new String[][] {
            {"SELECT COUNT(*) AS count FROM Title", "Total Titles", "%-43d"},
            {"SELECT COUNT(*) AS count FROM Person", "Total People", "%-43d"},
            {"SELECT COUNT(*) AS count FROM Character", "Total Characters", "%-39d"},
            {"SELECT COUNT(*) AS count FROM Genre", "Total Genres", "%-43d"},
            {"SELECT COUNT(*) AS count FROM Rating", "Total Ratings", "%-42d"}
        };
        
        try {
            for (String[] query : queries) {
                try (PreparedStatement pstmt = conn.prepareStatement(query[0]);
                     ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.printf("| " + query[1] + ": " + query[2] + " |\n", rs.getInt("count"));
                    }
                } catch (SQLException e) {
                    System.out.println("don't do sql injection");
                    throw e;
                }
            }
            
            System.out.println("+----------------------------------------------------------+\n");
            
        } catch (SQLException e) {
            System.out.println("Error retrieving statistics: " + e.getMessage());
        }
    }
}
