package logic;

import java.sql.*;
import java.util.Scanner;
import utils.QueryUtils;
import ui.MenuSystem;

/**
 * Handles all title-related queries 
 * Combines title search, details, episodes, genres, and analytics
 */
public class TitleQueries extends QueryHandler {
    
    public TitleQueries(Connection conn, Scanner scanner) {
        super(conn, scanner);
    }
    
    @Override
    protected void printMenu() {
        MenuSystem.printTitleMenu();
    }
    
    @Override
    protected String getMenuName() {
        return "Title Queries Menu";
    }
    
    @Override
    protected void executeQuery(int choice) throws SQLException {
        switch (choice) {
            // Simple queries
            case 1: searchTitleByName(); break;
            case 2: getTitleDetails(); break;
            case 3: viewAlternativeTitles(); break;
            case 4: viewAllEpisodes(); break;
            case 5: searchTitlesByGenre(); break;
            case 6: searchTitlesByYearRange(); break;
            
            // Complex queries
            case 10: hiddenGemsVsBlockbusters(); break;
            case 11: globalVsRegionalTitles(); break;
            case 12: genreRatingTrendsByDecade(); break;
            case 13: consistentTVShowRatings(); break;
            case 14: countriesByMovieReleases(); break;
            case 15: seasonRatingsVsWriters(); break;
            
            default:
                System.out.println("Invalid option. Please select a valid number.\n");
        }
    }
    
    // ==================== SIMPLE QUERIES ====================
    
    // Query 1: Search for titles by name (partial match)
    private void searchTitleByName() throws SQLException {
        QueryUtils.printQuerySelection(1, "Search Title by Name");
        System.out.print("Enter title name (partial match allowed): ");
        String titleName = scanner.nextLine().trim();
        
        QueryUtils.printResultsHeader("Showing Results For Your Query");
        
        String sql = "SELECT titleID, titleType, primaryTitle, startYear, runtimeMinutes " +
                     "FROM Title " +
                     "WHERE primaryTitle LIKE ? " +
                     "ORDER BY startYear DESC, primaryTitle";
        
        QueryUtils.executeQueryWithParams(conn, sql, formatter, "%" + titleName + "%");
    }
    
    // Query 2: Get detailed information for a specific title by ID
    private void getTitleDetails() throws SQLException {
        System.out.println("You Selected: [2 - Get Title Details (by ID)]");
        System.out.print("Enter Title ID (e.g., tt0000001): ");
        String titleID = scanner.nextLine().trim();
        
        System.out.println("\n--- Showing Title Details ---\n");
        
        String sql = "SELECT t.titleID, t.titleType, t.primaryTitle, t.isAdult, " +
                     "       t.startYear, t.endYear, t.runtimeMinutes, " +
                     "       r.averageRating, r.numVotes " +
                     "FROM Title t " +
                     "LEFT JOIN Rating r ON t.titleID = r.titleID " +
                     "WHERE t.titleID = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, titleID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Title ID: " + rs.getString("titleID"));
                    System.out.println("Type: " + rs.getString("titleType"));
                    System.out.println("Title: " + rs.getString("primaryTitle"));
                    System.out.println("Year: " + rs.getInt("startYear"));
                    System.out.println("Runtime: " + rs.getInt("runtimeMinutes") + " min");
                    System.out.println("Rating: " + rs.getDouble("averageRating"));
                    System.out.println("Votes: " + rs.getInt("numVotes"));
                } else {
                    System.out.println("No title found with ID: " + titleID);
                }
            }
        }
    }
    
    // Query 3: View all alternative titles for a specific title (different regions/languages)
    private void viewAlternativeTitles() throws SQLException {
        QueryUtils.printQuerySelection(3, "View Alternative Titles");
        System.out.print("Enter Title ID (e.g., tt0000001): ");
        String titleID = scanner.nextLine().trim();
        
        QueryUtils.printResultsHeader("Showing Alternative Titles");
        
        String sql = "SELECT ordering, title, region, language, isOriginalTitle " +
                     "FROM AlternativeTitle " +
                     "WHERE titleID = ? " +
                     "ORDER BY ordering";
        
        QueryUtils.executeQueryWithParams(conn, sql, formatter, titleID);
    }
    
    // Query 4: View all episodes of a TV series ordered by season and episode number
    private void viewAllEpisodes() throws SQLException {
        QueryUtils.printQuerySelection(4, "View All Episodes of a Series");
        System.out.print("Enter Series Title ID (e.g., tt0000001): ");
        String seriesID = scanner.nextLine().trim();
        
        QueryUtils.printResultsHeader("Showing All Episodes");
        
        String sql = "SELECT e.titleID, t.primaryTitle, e.seasonNumber, e.episodeNumber, " +
                     "       r.averageRating, r.numVotes " +
                     "FROM Episode e " +
                     "JOIN Title t ON e.titleID = t.titleID " +
                     "LEFT JOIN Rating r ON e.titleID = r.titleID " +
                     "WHERE e.parentSeriesID = ? " +
                     "ORDER BY e.seasonNumber, e.episodeNumber";
        
        QueryUtils.executeQueryWithParams(conn, sql, formatter, seriesID);
    }
    
    // Query 5: Search for titles by genre name
    private void searchTitlesByGenre() throws SQLException {
        QueryUtils.printQuerySelection(5, "Search Titles by Genre");
        System.out.print("Enter Genre Name (e.g., Drama, Comedy, Action): ");
        String genreName = scanner.nextLine().trim();
        
        QueryUtils.printResultsHeader("Showing Titles in Genre: " + genreName);
        
        String sql = "SELECT t.titleID, t.primaryTitle, t.titleType, t.startYear, r.averageRating " +
                     "FROM Title t " +
                     "JOIN HasGenre hg ON t.titleID = hg.titleID " +
                     "JOIN Genre g ON hg.genreID = g.genreID " +
                     "LEFT JOIN Rating r ON t.titleID = r.titleID " +
                     "WHERE g.genreName = ? " +
                     "ORDER BY r.averageRating DESC, t.primaryTitle";
        
        QueryUtils.executeQueryWithParams(conn, sql, formatter, genreName);
    }
    
    // Query 6: Search for titles within a specific year range
    private void searchTitlesByYearRange() throws SQLException {
        System.out.println("You Selected: [6 - Search Titles by Year Range]");
        
        System.out.print("Enter Start Year (e.g., 2000): ");
        int startYear = Integer.parseInt(scanner.nextLine().trim());
        
        System.out.print("Enter End Year (e.g., 2020): ");
        int endYear = Integer.parseInt(scanner.nextLine().trim());
        
        if (startYear > endYear) {
            System.out.println("Error: Start year must be <= end year.\n");
            return;
        }
        
        System.out.println("\n--- Showing Titles from " + startYear + " to " + endYear + " ---\n");
        
        String sql = "SELECT t.titleID, t.primaryTitle, t.titleType, t.startYear, r.averageRating, r.numVotes " +
                     "FROM Title t " +
                     "LEFT JOIN Rating r ON t.titleID = r.titleID " +
                     "WHERE t.startYear BETWEEN ? AND ? " +
                     "ORDER BY t.startYear DESC, r.averageRating DESC";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, startYear);
            pstmt.setInt(2, endYear);
            try (ResultSet rs = pstmt.executeQuery()) {
                formatter.displayResultsWithPagination(rs, 10);
            }
        }
    }
    
    // ==================== COMPLEX QUERIES ====================
    
    // Query 10: Find hidden gems (high rating, low votes) vs blockbusters (high votes)
    private void hiddenGemsVsBlockbusters() throws SQLException {
        QueryUtils.printQuerySelection(10, "Hidden Gems vs Blockbusters");
        System.out.print("Enter minimum rating for hidden gems (e.g., 8.0): ");
        double minRating = Double.parseDouble(scanner.nextLine().trim());
        
        System.out.print("Enter maximum votes for hidden gems (e.g., 1000): ");
        int maxVotes = Integer.parseInt(scanner.nextLine().trim());
        
        QueryUtils.printResultsHeader("Hidden Gems (high rating, low votes)");
        
        String sql = "SELECT t.titleID, t.primaryTitle, t.startYear, " +
                     "       r.averageRating, r.numVotes " +
                     "FROM Title t " +
                     "JOIN Rating r ON t.titleID = r.titleID " +
                     "WHERE r.averageRating >= ? AND r.numVotes <= ? " +
                     "ORDER BY r.averageRating DESC, r.numVotes ASC " +
                     "LIMIT 20";
        
        QueryUtils.executeQueryWithParams(conn, sql, formatter, minRating, maxVotes);
    }
    
    // Query 11: Compare global titles (many regional versions) vs regional titles (few versions)
    private void globalVsRegionalTitles() throws SQLException {
        QueryUtils.printQuerySelection(11, "Global vs Regional Title Performance");
        QueryUtils.printResultsHeader("Comparing titles with many vs few regional versions");
        
        String sql = "SELECT t.titleID, t.primaryTitle, " +
                     "       COUNT(DISTINCT at.region) AS regionCount, " +
                     "       r.averageRating, r.numVotes, " +
                     "       CASE " +
                     "           WHEN COUNT(DISTINCT at.region) > 5 THEN 'Global' " +
                     "           ELSE 'Regional' " +
                     "       END AS titleScope " +
                     "FROM Title t " +
                     "LEFT JOIN AlternativeTitle at ON t.titleID = at.titleID " +
                     "LEFT JOIN Rating r ON t.titleID = r.titleID " +
                     "GROUP BY t.titleID, t.primaryTitle, r.averageRating, r.numVotes " +
                     "HAVING COUNT(DISTINCT at.region) > 0 " +
                     "ORDER BY regionCount DESC, r.averageRating DESC";
        
        QueryUtils.executeQuery(conn, sql, formatter);
    }
    
    // Query 12: Analyze rating trends for a specific genre across decades
    private void genreRatingTrendsByDecade() throws SQLException {
        QueryUtils.printQuerySelection(12, "Genre Rating Trends by Decade");
        System.out.print("Enter genre (e.g., Drama, Action): ");
        String genre = scanner.nextLine().trim();
        
        QueryUtils.printResultsHeader("Rating trends for " + genre + " by decade");
        
        String sql = "SELECT (t.startYear / 10) * 10 AS decade, " +
                     "       COUNT(*) AS titleCount, " +
                     "       AVG(r.averageRating) AS avgRating, " +
                     "       MIN(r.averageRating) AS minRating, " +
                     "       MAX(r.averageRating) AS maxRating " +
                     "FROM Title t " +
                     "JOIN HasGenre hg ON t.titleID = hg.titleID " +
                     "JOIN Genre g ON hg.genreID = g.genreID " +
                     "JOIN Rating r ON t.titleID = r.titleID " +
                     "WHERE g.genreName = ? AND t.startYear IS NOT NULL " +
                     "GROUP BY (t.startYear / 10) * 10 " +
                     "ORDER BY decade DESC";
        
        QueryUtils.executeQueryWithParams(conn, sql, formatter, genre);
    }
    
    // Query 13: Find TV shows with most consistent episode ratings (low variance)
    private void consistentTVShowRatings() throws SQLException {
        QueryUtils.printQuerySelection(13, "TV Shows with Most Consistent Ratings");
        QueryUtils.printResultsHeader("TV shows with low rating variance");
        
        String sql = "SELECT t.titleID, t.primaryTitle, " +
                     "       COUNT(e.titleID) AS episodeCount, " +
                     "       AVG(r.averageRating) AS avgRating, " +
                     "       MAX(r.averageRating) - MIN(r.averageRating) AS ratingRange " +
                     "FROM Title t " +
                     "JOIN Episode e ON t.titleID = e.parentSeriesID " +
                     "JOIN Rating r ON e.titleID = r.titleID " +
                     "WHERE t.titleType = 'tvSeries' " +
                     "GROUP BY t.titleID, t.primaryTitle " +
                     "HAVING COUNT(e.titleID) >= 10 " +
                     "ORDER BY ratingRange ASC, avgRating DESC";
        
        QueryUtils.executeQuery(conn, sql, formatter);
    }
    
    // Query 14: Rank countries by total movie releases and average rating
    private void countriesByMovieReleases() throws SQLException {
        QueryUtils.printQuerySelection(14, "Countries by Total Movie Releases");
        QueryUtils.printResultsHeader("Countries ranked by movie releases");
        
        String sql = "SELECT at.region AS country, " +
                     "       COUNT(DISTINCT t.titleID) AS movieCount, " +
                     "       AVG(r.averageRating) AS avgRating " +
                     "FROM Title t " +
                     "JOIN AlternativeTitle at ON t.titleID = at.titleID " +
                     "LEFT JOIN Rating r ON t.titleID = r.titleID " +
                     "WHERE t.titleType IN ('movie', 'tvMovie') AND at.region IS NOT NULL " +
                     "GROUP BY at.region " +
                     "HAVING COUNT(DISTINCT t.titleID) >= 5 " +
                     "ORDER BY movieCount DESC, avgRating DESC";
        
        QueryUtils.executeQuery(conn, sql, formatter);
    }
    
    // Query 15: Analyze TV series season performance vs number of writers
    private void seasonRatingsVsWriters() throws SQLException {
        QueryUtils.printQuerySelection(15, "TV Series: Season Ratings vs Writer Count");
        System.out.print("Enter TV Series ID (e.g., tt0000001): ");
        String seriesID = scanner.nextLine().trim();
        
        QueryUtils.printResultsHeader("Season performance vs writer count");
        
        String sql = "SELECT e.seasonNumber, " +
                     "       COUNT(DISTINCT e.titleID) AS episodeCount, " +
                     "       COUNT(DISTINCT wa.personID) AS writerCount, " +
                     "       AVG(r.averageRating) AS avgRating " +
                     "FROM Episode e " +
                     "LEFT JOIN Rating r ON e.titleID = r.titleID " +
                     "LEFT JOIN WorksAs wa ON e.titleID = wa.titleID " +
                     "LEFT JOIN Profession p ON wa.professionID = p.professionID " +
                     "WHERE e.parentSeriesID = ? AND p.professionName = 'writer' " +
                     "GROUP BY e.seasonNumber " +
                     "ORDER BY e.seasonNumber";
        
        QueryUtils.executeQueryWithParams(conn, sql, formatter, seriesID);
    }
}
