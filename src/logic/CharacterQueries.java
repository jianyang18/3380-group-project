package logic;

import java.sql.*;
import java.util.Scanner;
import utils.QueryUtils;
import ui.MenuSystem;

/**
 * Handles all character-related queries
 * Search characters and analyze who played them
 */
public class CharacterQueries extends QueryHandler {
    
    public CharacterQueries(Connection conn, Scanner scanner) {
        super(conn, scanner);
    }
    
    @Override
    protected void printMenu() {
        MenuSystem.printCharacterMenu();
    }
    
    @Override
    protected String getMenuName() {
        return "Character Queries Menu";
    }
    
    @Override
    protected void executeQuery(int choice) throws SQLException {
        switch (choice) {
            // Simple queries
            case 1: searchCharacterByName(); break;
            case 2: whoPlayedCharacter(); break;
            
            // Complex queries
            case 10: mostPortrayedCharacters(); break;
            
            default:
                System.out.println("Invalid option. Please select a valid number.\n");
        }
    }
    
    // ==================== SIMPLE QUERIES ====================
    
    // Query 1: Search for characters by name (partial match)
    private void searchCharacterByName() throws SQLException {
        QueryUtils.printQuerySelection(1, "Search Character by Name");
        System.out.print("Enter character name (partial match allowed): ");
        String characterName = scanner.nextLine().trim();
        
        QueryUtils.printResultsHeader("Showing Results For Your Query");
        
        String sql = "SELECT characterID, characterName " +
                     "FROM Character " +
                     "WHERE characterName LIKE ? " +
                     "ORDER BY characterName";
        
        QueryUtils.executeQueryWithParams(conn, sql, formatter, "%" + characterName + "%");
    }
    
    // Query 2: Find all actors who played a specific character across different titles
    private void whoPlayedCharacter() throws SQLException {
        QueryUtils.printQuerySelection(2, "Who Has Played This Character?");
        System.out.print("Enter Character Name (partial match allowed): ");
        String characterName = scanner.nextLine().trim();
        
        QueryUtils.printResultsHeader("Showing Actors Who Played: " + characterName);
        
        String sql = "SELECT c.characterID, c.characterName, p.personID, p.primaryName, " +
                     "       t.titleID, t.primaryTitle, t.startYear, t.titleType " +
                     "FROM Character c " +
                     "JOIN PlayedIn pi ON c.characterID = pi.characterID " +
                     "JOIN Person p ON pi.personID = p.personID " +
                     "JOIN Title t ON pi.titleID = t.titleID " +
                     "WHERE c.characterName LIKE ? " +
                     "ORDER BY c.characterName, t.startYear DESC, p.primaryName";
        
        QueryUtils.executeQueryWithParams(conn, sql, formatter, "%" + characterName + "%");
    }
    
    // ==================== COMPLEX QUERIES ====================
    
    // Query 10: Find characters portrayed by multiple actors (iconic characters)
    private void mostPortrayedCharacters() throws SQLException {
        QueryUtils.printQuerySelection(10, "Most Portrayed Characters");
        System.out.print("Enter minimum number of actors (e.g., 3): ");
        int minActors = Integer.parseInt(scanner.nextLine().trim());
        
        QueryUtils.printResultsHeader("Characters portrayed by " + minActors + "+ actors");
        
        String sql = "SELECT c.characterID, c.characterName, " +
                     "       COUNT(DISTINCT pi.personID) AS actorCount, " +
                     "       COUNT(DISTINCT pi.titleID) AS titleCount " +
                     "FROM Character c " +
                     "JOIN PlayedIn pi ON c.characterID = pi.characterID " +
                     "GROUP BY c.characterID, c.characterName " +
                     "HAVING COUNT(DISTINCT pi.personID) >= ? " +
                     "ORDER BY actorCount DESC, titleCount DESC";
        
        QueryUtils.executeQueryWithParams(conn, sql, formatter, minActors);
    }
}
