package ui;

import java.sql.*;

/**
 * component for displaying database statistics
 */
public class StatisticsDisplay {
    
    public static void showStatistics(Connection conn) {
        System.out.println("\n+----------------------------------------------------------+");
        System.out.println("|                    Database Statistics                   |");
        System.out.println("+----------------------------------------------------------+");
        
        try {
            Statement stmt = conn.createStatement();
            
            // Count total titles
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM Title");
            if (rs.next()) {
                System.out.printf("| Total Titles: %-43d |\n", rs.getInt("count"));
            }
            
            // Count total people
            rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM Person");
            if (rs.next()) {
                System.out.printf("| Total People: %-43d |\n", rs.getInt("count"));
            }
            
            // Count total characters
            rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM Character");
            if (rs.next()) {
                System.out.printf("| Total Characters: %-39d |\n", rs.getInt("count"));
            }
            
            // Count total genres
            rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM Genre");
            if (rs.next()) {
                System.out.printf("| Total Genres: %-43d |\n", rs.getInt("count"));
            }
            
            // Count total ratings
            rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM Rating");
            if (rs.next()) {
                System.out.printf("| Total Ratings: %-42d |\n", rs.getInt("count"));
            }
            
            System.out.println("+----------------------------------------------------------+\n");
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            System.out.println("Error retrieving statistics: " + e.getMessage());
        }
    }
}
