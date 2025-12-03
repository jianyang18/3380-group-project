package logic;

import java.sql.*;
import java.util.Scanner;
import utils.QueryUtils;
import ui.MenuSystem;

/**
 * Handles all genre and statistics-related queries
 * Genre analytics, language analysis, runtime statistics
 */
public class GenreQueries extends QueryHandler {
    
    public GenreQueries(Connection conn, Scanner scanner) {
        super(conn, scanner);
    }
    
    @Override
    protected void printMenu() {
        MenuSystem.printGenreMenu();
    }
    
    @Override
    protected String getMenuName() {
        return "Genre Queries Menu";
    }
    
    @Override
    protected void executeQuery(int choice) throws SQLException {
        switch (choice) {
            // Simple queries
            case 1: viewAllGenres(); break;
            
            // Complex queries
            case 10: genresByRuntime(); break;
            case 11: languageRatingsByRegion(); break;
            case 12: popularGenresPerLanguage(); break;
            
            default:
                System.out.println("Invalid option. Please select a valid number.\n");
        }
    }
    
    // ==================== SIMPLE QUERIES ====================
    
    // Query 1: View all genres with count of titles in each genre
    private void viewAllGenres() throws SQLException {
        QueryUtils.printQuerySelection(1, "View All Genres");
        QueryUtils.printResultsHeader("Showing All Genres");
        
        String sql = "SELECT g.genreID, g.genreName, COUNT(hg.titleID) AS titleCount " +
                     "FROM Genre g " +
                     "LEFT JOIN HasGenre hg ON g.genreID = hg.genreID " +
                     "GROUP BY g.genreID, g.genreName " +
                     "ORDER BY g.genreName";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            formatter.displayResultsWithPagination(rs, 10);
        } catch (SQLException e) {
            System.out.println("don't do sql injection");
            throw e;
        }
    }
    
    // ==================== COMPLEX QUERIES ====================
    
    // Query 10: Analyze total and average runtime statistics by genre
    private void genresByRuntime() throws SQLException {
        QueryUtils.printQuerySelection(10, "Genres by Total/Average Runtime");
        QueryUtils.printResultsHeader("Genre runtime statistics");
        
        String sql = "SELECT g.genreName, " +
                     "       COUNT(DISTINCT t.titleID) AS titleCount, " +
                     "       SUM(t.runtimeMinutes) AS totalRuntime, " +
                     "       AVG(t.runtimeMinutes) AS avgRuntime, " +
                     "       MIN(t.runtimeMinutes) AS minRuntime, " +
                     "       MAX(t.runtimeMinutes) AS maxRuntime " +
                     "FROM Genre g " +
                     "JOIN HasGenre hg ON g.genreID = hg.genreID " +
                     "JOIN Title t ON hg.titleID = t.titleID " +
                     "WHERE t.runtimeMinutes IS NOT NULL " +
                     "GROUP BY g.genreName " +
                     "ORDER BY totalRuntime DESC";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            formatter.displayResultsWithPagination(rs, 10);
        } catch (SQLException e) {
            System.out.println("don't do sql injection");
            throw e;
        }
    }
    
    // Query 11: Compare language ratings within a specific region
    private void languageRatingsByRegion() throws SQLException {
        QueryUtils.printQuerySelection(11, "Language Ratings by Region");
        printAvailableRegionCodes();
        System.out.print("Enter region code (e.g., US, GB, FR): ");
        String region = scanner.nextLine().trim();

        if (region.isEmpty()) {
            System.out.println("Please enter a region code.\n");
            return;
        }
        
        QueryUtils.printResultsHeader("Language ratings for region: " + region);
        
        String sql = "SELECT at.language, " +
                     "       COUNT(DISTINCT t.titleID) AS titleCount, " +
                     "       AVG(r.averageRating) AS avgRating, " +
                     "       AVG(r.numVotes) AS avgVotes " +
                     "FROM AlternativeTitle at " +
                     "JOIN Title t ON at.titleID = t.titleID " +
                     "LEFT JOIN Rating r ON t.titleID = r.titleID " +
                     "WHERE at.region = ? AND at.language IS NOT NULL " +
                     "GROUP BY at.language " +
                     "HAVING COUNT(DISTINCT t.titleID) >= 5 " +
                     "ORDER BY avgRating DESC, titleCount DESC";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, region);
            try (ResultSet rs = pstmt.executeQuery()) {
                formatter.displayResultsWithPagination(rs, 10);
            }
        } catch (SQLException e) {
            System.out.println("don't do sql injection");
            throw e;
        }
    }

    /**
     * Display available region codes to guide user input
     */
    private void printAvailableRegionCodes() {
        System.out.println("Common region codes:");
        System.out.println("US - United States");
        System.out.println("CA - Canada");
        System.out.println("CN - China");
        System.out.println("IN - India");
        System.out.println();
    }
    
    // Query 12: Find most popular genres for a specific language
    private void popularGenresPerLanguage() throws SQLException {
        QueryUtils.printQuerySelection(12, "Most Popular Genres per Language");
        printCommonLanguageCodes();
        System.out.print("Enter language code (e.g., en, fr, es): ");
        String language = scanner.nextLine().trim();

        if (language.isEmpty()) {
            System.out.println("Please enter a language code.\n");
            return;
        }
        
        QueryUtils.printResultsHeader("Popular genres in language: " + language);
        
        String sql = "SELECT g.genreName, " +
                     "       COUNT(DISTINCT t.titleID) AS titleCount, " +
                     "       AVG(r.averageRating) AS avgRating " +
                     "FROM Genre g " +
                     "JOIN HasGenre hg ON g.genreID = hg.genreID " +
                     "JOIN Title t ON hg.titleID = t.titleID " +
                     "JOIN AlternativeTitle at ON t.titleID = at.titleID " +
                     "LEFT JOIN Rating r ON t.titleID = r.titleID " +
                     "WHERE at.language = ? " +
                     "GROUP BY g.genreName " +
                     "HAVING COUNT(DISTINCT t.titleID) >= 3 " +
                     "ORDER BY titleCount DESC, avgRating DESC";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, language);
            try (ResultSet rs = pstmt.executeQuery()) {
                formatter.displayResultsWithPagination(rs, 10);
            }
        } catch (SQLException e) {
            System.out.println("don't do sql injection");
            throw e;
        }
    }

    /**
     * Display common language codes to guide user input
     */
    private void printCommonLanguageCodes() {
        System.out.println("Common language codes:");
        System.out.println("en - English");
        System.out.println("fr - French");
        System.out.println("es - Spanish");
        System.out.println();
    }
}
