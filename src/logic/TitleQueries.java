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
            case 1: searchTitlesByActor(); break;
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
    
    // Query 1: Search for titles by actor/actress name (movies and TV episodes)
    private void searchTitlesByActor() throws SQLException {
        QueryUtils.printQuerySelection(1, "Search Title by Actor/Actress name");
        System.out.print("Enter Actor/Actress Name (partial match allowed): ");
        String actorName = scanner.nextLine().trim();

        if (actorName.isEmpty()) {
            System.out.println("Please enter a name.\n");
            return;
        }
        
        QueryUtils.printResultsHeader("Showing Titles Featuring: " + actorName);
        
        String sql = "SELECT p.primaryName AS actorName, " +
                     "       t.primaryTitle AS title, " +
                     "       t.titleType, " +
                     "       t.startYear, " +
                     "       r.averageRating " +
                     "FROM Person p " +
                     "JOIN PlayedIn pi ON p.personID = pi.personID " +
                     "JOIN Title t ON pi.titleID = t.titleID " +
                     "LEFT JOIN Rating r ON t.titleID = r.titleID " +
                     "WHERE p.primaryName LIKE ? " +
                     "  AND t.titleType IN ('movie', 'tvEpisode') " +
                     "ORDER BY t.startYear DESC, t.primaryTitle";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + actorName + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                formatter.displayResultsWithPagination(rs, 10);
            }
        } catch (SQLException e) {
            System.out.println("don't do sql injection");
            throw e;
        }
    }
    
    // Query 2: Get detailed information for titles by movie name (partial match)
    private void getTitleDetails() throws SQLException {
        System.out.println("You Selected: [2 - Get Title Details (by Title)]");
        System.out.print("Enter Movie Title (partial match allowed): ");
        String titleName = scanner.nextLine().trim();

        if (titleName.isEmpty()) {
            System.out.println("Please enter a movie title.\n");
            return;
        }
        
        System.out.println("\n--- Showing Title Details ---\n");
        
        String sql = "SELECT t.titleID, t.titleType, t.primaryTitle, t.isAdult, " +
                     "       t.startYear, t.endYear, t.runtimeMinutes, " +
                     "       r.averageRating, r.numVotes " +
                     "FROM Title t " +
                     "LEFT JOIN Rating r ON t.titleID = r.titleID " +
                     "WHERE t.primaryTitle LIKE ? " +
                     "ORDER BY r.averageRating DESC, t.startYear DESC, t.primaryTitle";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + titleName + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                formatter.displayResultsWithPagination(rs, 10);
            }
        } catch (SQLException e) {
            System.out.println("don't do sql injection");
            throw e;
        }
    }
    
    // Query 3: View all alternative titles for a movie by title name (different regions/languages)
    private void viewAlternativeTitles() throws SQLException {
        QueryUtils.printQuerySelection(3, "View Alternative Titles");
        System.out.print("Enter Movie Title (partial match allowed): ");
        String titleName = scanner.nextLine().trim();

        if (titleName.isEmpty()) {
            System.out.println("Please enter a movie title.\n");
            return;
        }
        
        QueryUtils.printResultsHeader("Showing Alternative Titles");
        
        String sql = "SELECT t.primaryTitle, at.ordering, at.title, at.region, at.language, at.isOriginalTitle " +
                     "FROM AlternativeTitle at " +
                     "JOIN Title t ON at.titleID = t.titleID " +
                     "WHERE t.titleType = 'movie' AND t.primaryTitle LIKE ? " +
                     "ORDER BY t.primaryTitle, at.ordering";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + titleName + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                formatter.displayResultsWithPagination(rs, 10);
            }
        } catch (SQLException e) {
            System.out.println("don't do sql injection");
            throw e;
        }
    }
    
    // Query 4: View all episodes of a TV series ordered by season and episode number
    private void viewAllEpisodes() throws SQLException {
        QueryUtils.printQuerySelection(4, "View All Episodes of a Series");
        System.out.print("Enter Series Name (partial match allowed): ");
        String seriesName = scanner.nextLine().trim();

        if (seriesName.isEmpty()) {
            System.out.println("Please enter a series name.\n");
            return;
        }
        
        QueryUtils.printResultsHeader("Showing All Episodes");
        
        String sql = "SELECT e.titleID, t.primaryTitle, e.seasonNumber, e.episodeNumber, " +
                     "       r.averageRating, r.numVotes " +
                     "FROM Episode e " +
                     "JOIN Title t ON e.titleID = t.titleID " +
                     "LEFT JOIN Rating r ON e.titleID = r.titleID " +
                     "JOIN Title s ON e.parentSeriesID = s.titleID " +
                     "WHERE s.primaryTitle LIKE ? " +
                     "ORDER BY e.seasonNumber, e.episodeNumber";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + seriesName + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                formatter.displayResultsWithPagination(rs, 10);
            }
        } catch (SQLException e) {
            System.out.println("don't do sql injection");
            throw e;
        }
    }
    
    // Query 5: Search for titles by genre name
    private void searchTitlesByGenre() throws SQLException {
        QueryUtils.printQuerySelection(5, "Search Titles by Genre");
        System.out.print("Enter Genre Name (e.g., Drama, Comedy, Action): ");
        String genreName = scanner.nextLine().trim();

        if (genreName.isEmpty()) {
            System.out.println("Please enter a genre.\n");
            return;
        }
        
        QueryUtils.printResultsHeader("Showing Titles in Genre: " + genreName);
        
        String sql = "SELECT t.titleID, t.primaryTitle, t.titleType, t.startYear, r.averageRating " +
                     "FROM Title t " +
                     "JOIN HasGenre hg ON t.titleID = hg.titleID " +
                     "JOIN Genre g ON hg.genreID = g.genreID " +
                     "LEFT JOIN Rating r ON t.titleID = r.titleID " +
                     "WHERE g.genreName = ? " +
                     "ORDER BY r.averageRating DESC, t.primaryTitle";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, genreName);
            try (ResultSet rs = pstmt.executeQuery()) {
                formatter.displayResultsWithPagination(rs, 10);
            }
        } catch (SQLException e) {
            System.out.println("don't do sql injection");
            throw e;
        }
    }
    
    // Query 6: Search for titles within a specific year range
    private void searchTitlesByYearRange() throws SQLException {
        System.out.println("You Selected: [6 - Search Titles by Year Range]");
        
        System.out.print("Enter Start Year (e.g., 2000): ");
        String startYearInput = scanner.nextLine().trim();
        System.out.print("Enter End Year (e.g., 2020): ");
        String endYearInput = scanner.nextLine().trim();
        
        int startYear;
        int endYear;
        try {
            startYear = Integer.parseInt(startYearInput);
            endYear = Integer.parseInt(endYearInput);
        } catch (NumberFormatException e) {
            System.out.println("Please enter valid numeric years.\n");
            return;
        }
        
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
        } catch (SQLException e) {
            System.out.println("don't do sql injection");
            throw e;
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
        
        String sql = "SELECT TOP 20 t.titleID, t.primaryTitle, t.startYear, " +
                     "       r.averageRating, r.numVotes " +
                     "FROM Title t " +
                     "JOIN Rating r ON t.titleID = r.titleID " +
                     "WHERE r.averageRating >= ? AND r.numVotes <= ? " +
                     "ORDER BY r.averageRating DESC, r.numVotes ASC";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, minRating);
            pstmt.setInt(2, maxVotes);
            try (ResultSet rs = pstmt.executeQuery()) {
                formatter.displayResultsWithPagination(rs, 10);
            }
        } catch (SQLException e) {
            System.out.println("don't do sql injection");
            throw e;
        }
    }
    
    // Query 11: Compare global titles (many regional versions) vs regional titles (few versions)
    private void globalVsRegionalTitles() throws SQLException {
        QueryUtils.printQuerySelection(11, "Regional Reach vs Ratings & Popularity");
        QueryUtils.printResultsHeader("Correlation of regional reach with rating and votes");
        
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
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            formatter.displayResultsWithPagination(rs, 10);
        } catch (SQLException e) {
            System.out.println("don't do sql injection");
            throw e;
        }
    }
    
    // Query 12: Analyze rating trends for a specific genre across decades
    private void genreRatingTrendsByDecade() throws SQLException {
        QueryUtils.printQuerySelection(12, "Genre Rating Trends by Decade");
        System.out.print("Enter genre (e.g., Drama, Action): ");
        String genre = scanner.nextLine().trim();

        if (genre.isEmpty()) {
            System.out.println("Please enter a genre.\n");
            return;
        }
        
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
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, genre);
            try (ResultSet rs = pstmt.executeQuery()) {
                formatter.displayResultsWithPagination(rs, 10);
            }
        } catch (SQLException e) {
            System.out.println("don't do sql injection");
            throw e;
        }
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
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            formatter.displayResultsWithPagination(rs, 10);
        } catch (SQLException e) {
            System.out.println("don't do sql injection");
            throw e;
        }
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
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            formatter.displayResultsWithPagination(rs, 10);
        } catch (SQLException e) {
            System.out.println("don't do sql injection");
            throw e;
        }
    }
    
    // Query 15: Search ratings per season and writer totals for a TV series
    private void seasonRatingsVsWriters() throws SQLException {
        QueryUtils.printQuerySelection(15, "Season Ratings & Writers by TV Series Title");
        System.out.print("Enter TV Series Title (partial match allowed): ");
        String seriesTitle = scanner.nextLine().trim();

        if (seriesTitle.isEmpty()) {
            System.out.println("Please enter a series title.\n");
            return;
        }
        
        QueryUtils.printResultsHeader("Ratings and writer totals by season");
        
        String sql = "SELECT e.seasonNumber, " +
                     "       COUNT(DISTINCT e.titleID) AS episodeCount, " +
                     "       COUNT(DISTINCT wa.personID) AS writerCount, " +
                     "       AVG(r.averageRating) AS avgRating " +
                     "FROM Episode e " +
                     "LEFT JOIN Rating r ON e.titleID = r.titleID " +
                     "LEFT JOIN WorksAs wa ON e.titleID = wa.titleID " +
                     "LEFT JOIN Profession p ON wa.professionID = p.professionID AND p.professionName = 'writer' " +
                     "JOIN Title s ON e.parentSeriesID = s.titleID " +
                     "WHERE s.titleType = 'tvSeries' AND s.primaryTitle LIKE ? " +
                     "GROUP BY e.seasonNumber " +
                     "ORDER BY e.seasonNumber";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + seriesTitle + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                formatter.displayResultsWithPagination(rs, 10);
            }
        } catch (SQLException e) {
            System.out.println("don't do sql injection");
            throw e;
        }
    }
}
