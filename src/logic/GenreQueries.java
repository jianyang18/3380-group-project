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
        
        QueryUtils.executeQuery(conn, sql, formatter);
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
        
        QueryUtils.executeQuery(conn, sql, formatter);
    }
    
    // Query 11: Compare language ratings within a specific region
    private void languageRatingsByRegion() throws SQLException {
        QueryUtils.printQuerySelection(11, "Language Ratings by Region");
        System.out.print("Enter region code (e.g., US, GB, FR): ");
        String region = scanner.nextLine().trim();
        
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
        
        QueryUtils.executeQueryWithParams(conn, sql, formatter, region);
    }
    
    // Query 12: Find most popular genres for a specific language
    private void popularGenresPerLanguage() throws SQLException {
        QueryUtils.printQuerySelection(12, "Most Popular Genres per Language");
        System.out.print("Enter language code (e.g., en, fr, es): ");
        String language = scanner.nextLine().trim();
        
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
        
        QueryUtils.executeQueryWithParams(conn, sql, formatter, language);
    }
}
