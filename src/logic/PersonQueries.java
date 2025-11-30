package logic;

import java.sql.*;
import java.util.Scanner;
import utils.QueryUtils;
import ui.MenuSystem;

/**
 * Handles all person-related queries (actors, directors, writers)
 * Combines person search, details, profession lookups, and analytics
 */
public class PersonQueries extends QueryHandler {
    
    public PersonQueries(Connection conn, Scanner scanner) {
        super(conn, scanner);
    }
    
    @Override
    protected void printMenu() {
        MenuSystem.printPersonMenu();
    }
    
    @Override
    protected String getMenuName() {
        return "Person Queries Menu";
    }
    
    @Override
    protected void executeQuery(int choice) throws SQLException {
        switch (choice) {
            // Simple queries
            case 1: searchPersonByName(); break;
            case 2: getPersonDetails(); break;
            case 3: searchPeopleByProfession(); break;
            case 4: getActorAge(); break;
            case 5: viewAllProfessions(); break;
            
            // Complex queries
            case 10: actorDirectorCollaborations(); break;
            case 11: topProlificActors(); break;
            case 12: mostVersatileActors(); break;
            case 13: oneHitWonders(); break;
            case 14: moviesByActor(); break;
            
            default:
                System.out.println("Invalid option. Please select a valid number.\n");
        }
    }
    
    // ==================== SIMPLE QUERIES ====================
    
    // Query 1: Search for people by name (partial match)
    private void searchPersonByName() throws SQLException {
        QueryUtils.printQuerySelection(1, "Search Person by Name");
        System.out.print("Enter person name (partial match allowed): ");
        String personName = scanner.nextLine().trim();
        
        QueryUtils.printResultsHeader("Showing Results For Your Query");
        
        String sql = "SELECT personID, primaryName, birthYear, deathYear " +
                     "FROM Person " +
                     "WHERE primaryName LIKE ? " +
                     "ORDER BY primaryName";
        
        QueryUtils.executeQueryWithParams(conn, sql, formatter, "%" + personName + "%");
    }
    
    // Query 2: Get detailed information for a specific person by ID
    private void getPersonDetails() throws SQLException {
        System.out.println("You Selected: [2 - Get Person Details (by ID)]");
        System.out.print("Enter Person ID (e.g., nm0000001): ");
        String personID = scanner.nextLine().trim();
        
        System.out.println("\n--- Showing Person Details ---\n");
        
        String sql = "SELECT personID, primaryName, birthYear, deathYear " +
                     "FROM Person " +
                     "WHERE personID = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, personID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Person ID: " + rs.getString("personID"));
                    System.out.println("Name: " + rs.getString("primaryName"));
                    System.out.println("Birth Year: " + (rs.getObject("birthYear") != null ? rs.getInt("birthYear") : "N/A"));
                    System.out.println("Death Year: " + (rs.getObject("deathYear") != null ? rs.getInt("deathYear") : "Living"));
                } else {
                    System.out.println("No person found with ID: " + personID);
                }
            }
        }
    }
    
    // Query 3: Search for people by their profession (e.g., actor, director, writer)
    private void searchPeopleByProfession() throws SQLException {
        QueryUtils.printQuerySelection(3, "Search People by Profession");
        System.out.print("Enter Profession (e.g., actor, director, writer): ");
        String professionName = scanner.nextLine().trim();
        
        QueryUtils.printResultsHeader("Showing People with Profession: " + professionName);
        
        String sql = "SELECT DISTINCT p.personID, p.primaryName, p.birthYear, p.deathYear " +
                     "FROM Person p " +
                     "JOIN WorksAs w ON p.personID = w.personID " +
                     "JOIN Profession pr ON w.professionID = pr.professionID " +
                     "WHERE pr.professionName = ? " +
                     "ORDER BY p.primaryName";
        
        QueryUtils.executeQueryWithParams(conn, sql, formatter, professionName);
    }
    
    // Query 4: Calculate and display age for actors/actresses (living or at time of death)
    private void getActorAge() throws SQLException {
        QueryUtils.printQuerySelection(4, "Get Actor/Actress Age");
        System.out.print("Enter Actor/Actress Name (partial match allowed): ");
        String actorName = scanner.nextLine().trim();
        
        QueryUtils.printResultsHeader("Showing Age Information");
        
        String sql = "SELECT p.personID, p.primaryName, p.birthYear, p.deathYear, " +
                     "       CASE " +
                     "           WHEN p.deathYear IS NOT NULL THEN p.deathYear - p.birthYear " +
                     "           WHEN p.birthYear IS NOT NULL THEN CAST(strftime('%Y', 'now') AS INTEGER) - p.birthYear " +
                     "           ELSE NULL " +
                     "       END AS age, " +
                     "       CASE " +
                     "           WHEN p.deathYear IS NOT NULL THEN 'Deceased' " +
                     "           ELSE 'Living' " +
                     "       END AS status " +
                     "FROM Person p " +
                     "WHERE p.primaryName LIKE ? " +
                     "ORDER BY p.primaryName";
        
        QueryUtils.executeQueryWithParams(conn, sql, formatter, "%" + actorName + "%");
    }
    
    // Query 5: View all professions with count of people in each profession
    private void viewAllProfessions() throws SQLException {
        QueryUtils.printQuerySelection(5, "View All Professions");
        QueryUtils.printResultsHeader("Showing All Professions");
        
        String sql = "SELECT p.professionID, p.professionName, COUNT(w.personID) AS peopleCount " +
                     "FROM Profession p " +
                     "LEFT JOIN WorksAs w ON p.professionID = w.professionID " +
                     "GROUP BY p.professionID, p.professionName " +
                     "ORDER BY p.professionName";
        
        QueryUtils.executeQuery(conn, sql, formatter);
    }
    
    // ==================== COMPLEX QUERIES ====================
    
    // Query 10: Find actor/director pairs with most collaborations
    private void actorDirectorCollaborations() throws SQLException {
        QueryUtils.printQuerySelection(10, "Actor/Director Pairs with Most Collaborations");
        System.out.print("Enter minimum number of collaborations (e.g., 3): ");
        int minCollabs = Integer.parseInt(scanner.nextLine().trim());
        
        QueryUtils.printResultsHeader("Top Actor/Director Collaborations");
        
        String sql = "SELECT actor.primaryName AS actorName, " +
                     "       director.primaryName AS directorName, " +
                     "       COUNT(DISTINCT t.titleID) AS collaborationCount " +
                     "FROM Person actor " +
                     "JOIN PlayedIn pi ON actor.personID = pi.personID " +
                     "JOIN Title t ON pi.titleID = t.titleID " +
                     "JOIN WorksAs wa ON t.titleID = wa.titleID " +
                     "JOIN Person director ON wa.personID = director.personID " +
                     "JOIN Profession p ON wa.professionID = p.professionID " +
                     "WHERE p.professionName = 'director' " +
                     "GROUP BY actor.primaryName, director.primaryName " +
                     "HAVING COUNT(DISTINCT t.titleID) >= ? " +
                     "ORDER BY collaborationCount DESC";
        
        QueryUtils.executeQueryWithParams(conn, sql, formatter, minCollabs);
    }
    
    // Query 11: Find most prolific actors/actresses by number of titles
    private void topProlificActors() throws SQLException {
        QueryUtils.printQuerySelection(11, "Most Prolific Actors/Actresses");
        System.out.print("Enter number of top actors to display (e.g., 20): ");
        int topN = Integer.parseInt(scanner.nextLine().trim());
        
        QueryUtils.printResultsHeader("Top " + topN + " Most Prolific Actors");
        
        String sql = "SELECT p.personID, p.primaryName, " +
                     "       COUNT(DISTINCT pi.titleID) AS movieCount, " +
                     "       MIN(t.startYear) AS firstMovie, " +
                     "       MAX(t.startYear) AS lastMovie " +
                     "FROM Person p " +
                     "JOIN PlayedIn pi ON p.personID = pi.personID " +
                     "JOIN Title t ON pi.titleID = t.titleID " +
                     "GROUP BY p.personID, p.primaryName " +
                     "ORDER BY movieCount DESC " +
                     "LIMIT ?";
        
        QueryUtils.executeQueryWithParams(conn, sql, formatter, topN);
    }
    
    // Query 12: Find most versatile actors by genre diversity
    private void mostVersatileActors() throws SQLException {
        QueryUtils.printQuerySelection(12, "Most Versatile Actors (By Genre Diversity)");
        QueryUtils.printResultsHeader("Actors with most genre diversity");
        
        String sql = "SELECT p.personID, p.primaryName, " +
                     "       COUNT(DISTINCT g.genreID) AS genreCount, " +
                     "       COUNT(DISTINCT pi.titleID) AS movieCount " +
                     "FROM Person p " +
                     "JOIN PlayedIn pi ON p.personID = pi.personID " +
                     "JOIN HasGenre hg ON pi.titleID = hg.titleID " +
                     "JOIN Genre g ON hg.genreID = g.genreID " +
                     "GROUP BY p.personID, p.primaryName " +
                     "HAVING COUNT(DISTINCT pi.titleID) >= 5 " +
                     "ORDER BY genreCount DESC, movieCount DESC " +
                     "LIMIT 20";
        
        QueryUtils.executeQuery(conn, sql, formatter);
    }
    
    // Query 13: Find one-hit wonders (people with only one highly-rated work)
    private void oneHitWonders() throws SQLException {
        QueryUtils.printQuerySelection(13, "One-Hit Wonders (Actors/Directors)");
        System.out.print("Enter minimum rating for their single work (e.g., 8.0): ");
        double minRating = Double.parseDouble(scanner.nextLine().trim());
        
        QueryUtils.printResultsHeader("One-Hit Wonders");
        
        String sql = "SELECT p.personID, p.primaryName, " +
                     "       t.primaryTitle, t.startYear, " +
                     "       r.averageRating, r.numVotes " +
                     "FROM Person p " +
                     "JOIN WorksAs wa ON p.personID = wa.personID " +
                     "JOIN Title t ON wa.titleID = t.titleID " +
                     "JOIN Rating r ON t.titleID = r.titleID " +
                     "WHERE r.averageRating >= ? " +
                     "GROUP BY p.personID, p.primaryName, t.primaryTitle, t.startYear, r.averageRating, r.numVotes " +
                     "HAVING COUNT(DISTINCT wa.titleID) = 1 " +
                     "ORDER BY r.averageRating DESC, r.numVotes DESC";
        
        QueryUtils.executeQueryWithParams(conn, sql, formatter, minRating);
    }
    
    // Query 14: List all movies featuring a specific actor with character names
    private void moviesByActor() throws SQLException {
        QueryUtils.printQuerySelection(14, "All Movies by Specific Actor");
        System.out.print("Enter Actor Name (partial match allowed): ");
        String actorName = scanner.nextLine().trim();
        
        QueryUtils.printResultsHeader("Movies featuring: " + actorName);
        
        String sql = "SELECT p.primaryName AS actorName, " +
                     "       t.titleID, t.primaryTitle, t.startYear, " +
                     "       r.averageRating, c.characterName " +
                     "FROM Person p " +
                     "JOIN PlayedIn pi ON p.personID = pi.personID " +
                     "JOIN Title t ON pi.titleID = t.titleID " +
                     "LEFT JOIN Character c ON pi.characterID = c.characterID " +
                     "LEFT JOIN Rating r ON t.titleID = r.titleID " +
                     "WHERE p.primaryName LIKE ? " +
                     "ORDER BY t.startYear DESC, r.averageRating DESC";
        
        QueryUtils.executeQueryWithParams(conn, sql, formatter, "%" + actorName + "%");
    }
}
