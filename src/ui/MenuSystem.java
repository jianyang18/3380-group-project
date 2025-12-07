package ui;

/**
 * Centralized menu display system
 */
public class MenuSystem {

    
    /**
     * Print the welcome menu
     */
    public static void printWelcomeMenu() {
        System.out.println("+----------------------------------------------------------+");
        System.out.println("|                  IMDb Database Project                   |");
        System.out.println("+----------------------------------------------------------+");
        System.out.println("| A comprehensive movie & TV database built from           |");
        System.out.println("| official IMDb datasets. This system allows users         |");
        System.out.println("| to explore titles, ratings, genres, cast, crew,          |");
        System.out.println("| characters, TV episodes, and more using SQL queries.     |");
        System.out.println("+----------------------------------------------------------+");
        System.out.println("| 1. Enter System                                          |");
        System.out.println("| 2. Statistics - summary / aggregate reports              |");
        System.out.println("| 3. Help - usage info and examples                        |");
        System.out.println("| 4. Quit                                                  |");
        System.out.println("+----------------------------------------------------------+");
    }
    
 
    /**
     * Print the main query menu
     */
    public static void printMainMenu() {
        System.out.println("+----------------------------------------------------------------+");
        System.out.println("|                      Main Query Menu                           |");
        System.out.println("+----------------------------------------------------------------+");
        System.out.println("| 1. Title Queries - search movies / TV titles                   |");
        System.out.println("| 2. Person Queries - search actors / directors / writers        |");
        System.out.println("| 3. Character Queries - want to know about a character?         |");
        System.out.println("| 4. Genre & Stats Queries - genre analytics                     |");
        System.out.println("+----------------------------------------------------------------+");
        System.out.println("| 5. Database Management - wipe, reset, view stats               |");
        System.out.println("| 6. Statistics - summary / aggregate reports                    |");
        System.out.println("| 7. Help - usage info and examples                              |");
        System.out.println("| 8. Exit - return to previous screen                            |");
        System.out.println("+----------------------------------------------------------------+");
    }
    
    /**
     * Print the help menu
     */
    public static void printHelp() {
        System.out.println("\n+----------------------------------------------------------+");
        System.out.println("|                         Help Menu                        |");
        System.out.println("+----------------------------------------------------------+");
        System.out.println("| NAVIGATION:                                              |");
        System.out.println("|   - Use number keys to select menu options               |");
        System.out.println("|   - Press Enter to confirm your selection                |");
        System.out.println("|   - Follow on-screen prompts for query inputs            |");
        System.out.println("|                                                          |");
        System.out.println("| QUERY CATEGORIES:                                        |");
        System.out.println("|   1. Title Queries - Search for movies/TV                |");
        System.out.println("|   2. Person Queries - Search actors/directors/writers    |");
        System.out.println("|   3. Character Queries - Want to know about a character? |");
        System.out.println("|   4. Genre & Stats Queries - Genre analytics and stats   |");
        System.out.println("|                                                          |");
        System.out.println("| TIPS:                                                    |");
        System.out.println("|   - For name searches, partial matches work              |");
        System.out.println("|   - Use Simple Queries to explore data first             |");
        System.out.println("|   - Check Statistics for database overview               |");
        System.out.println("+----------------------------------------------------------+\n");
    }
    
    /**
     * Print the Title Queries menu
     */
    public static void printTitleMenu() {
        System.out.println("+----------------------------------------------------------------+");
        System.out.println("|                      Title Queries Menu                        |");
        System.out.println("+----------------------------------------------------------------+");
        System.out.println("| SIMPLE QUERIES                                                 |");
        System.out.println("| 1. Search Title by Actor/Actress name                          |");
        System.out.println("| 2. Get Title Details (by Movie Title)                          |");
        System.out.println("| 3. View Alternative Titles for a Movie (by Title)              |");
        System.out.println("| 4. View All Episodes of a Series                               |");
        System.out.println("| 5. Search Titles by Genre                                      |");
        System.out.println("| 6. Search Titles by Year Range                                 |");
        System.out.println("+----------------------------------------------------------------+");
        System.out.println("| COMPLEX QUERIES                                                |");
        System.out.println("| 10. Explore Hidden Gems                                        |");
        System.out.println("| 11. Regional Reach vs Ratings & Popularity                     |");
        System.out.println("| 12. Genre Rating Trends by Decade                              |");
        System.out.println("| 13. TV Shows with Most Consistent Ratings                      |");
        System.out.println("| 14. Countries by Total Movie Releases                          |");
        System.out.println("| 15. Season Ratings & Writers by TV Series Title                |");
        System.out.println("+----------------------------------------------------------------+");
        System.out.println("| 99. Back to Main Menu                                          |");
        System.out.println("+----------------------------------------------------------------+");
    }
    
    /**
     * Print the Person Queries menu
     */
    public static void printPersonMenu() {
        System.out.println("+----------------------------------------------------------------+");
        System.out.println("|                      Person Queries Menu                       |");
        System.out.println("+----------------------------------------------------------------+");
        System.out.println("| SIMPLE QUERIES                                                 |");
        System.out.println("| 1. Search Person by Name                                       |");
        System.out.println("| 2. Get Person Details (by ID)                                  |");
        System.out.println("| 3. Get Actor/Actress Age                                       |");
        System.out.println("+----------------------------------------------------------------+");
        System.out.println("| COMPLEX QUERIES                                                |");
        System.out.println("| 10. Actor/Director Pairs with Most Collaborations              |");
        System.out.println("| 11. Most Prolific Actors/Actresses (Top N)                     |");
        System.out.println("| 12. Most Versatile Actors (By Genre Diversity)                 |");
        System.out.println("| 13. One-Hit Wonders (Actors/Directors)                         |");
        System.out.println("| 14. All Movies by Specific Actor                               |");
        System.out.println("+----------------------------------------------------------------+");
        System.out.println("| 99. Back to Main Menu                                          |");
        System.out.println("+----------------------------------------------------------------+");
    }
    
    /**
     * Print the Character Queries menu
     */
    public static void printCharacterMenu() {
        System.out.println("+----------------------------------------------------------------+");
        System.out.println("|                   Character Queries Menu                       |");
        System.out.println("+----------------------------------------------------------------+");
        System.out.println("| SIMPLE QUERIES                                                 |");
        System.out.println("| 1. Search Character by Name                                    |");
        System.out.println("| 2. Who Has Played This Character?                              |");
        System.out.println("+----------------------------------------------------------------+");
        System.out.println("| COMPLEX QUERIES                                                |");
        System.out.println("| 10. Most Portrayed Characters                                  |");
        System.out.println("+----------------------------------------------------------------+");
        System.out.println("| 99. Back to Main Menu                                          |");
        System.out.println("+----------------------------------------------------------------+");
    }
    
    /**
     * Print the Genre Queries menu
     */
    public static void printGenreMenu() {
        System.out.println("+----------------------------------------------------------------+");
        System.out.println("|                 Genre & Statistics Queries Menu                |");
        System.out.println("+----------------------------------------------------------------+");
        System.out.println("| SIMPLE QUERIES                                                 |");
        System.out.println("| 1. View All Genres                                             |");
        System.out.println("+----------------------------------------------------------------+");
        System.out.println("| COMPLEX QUERIES                                                |");
        System.out.println("| 10. Genres by Total/Average Runtime                            |");
        System.out.println("| 11. Language Ratings by Region                                 |");
        System.out.println("| 12. Most Popular Genres per Language                           |");
        System.out.println("+----------------------------------------------------------------+");
        System.out.println("| 99. Back to Main Menu                                          |");
        System.out.println("+----------------------------------------------------------------+");
    }
    
    /**
     * Print the Database Management menu
     */
    public static void printDatabaseManagementMenu() {
        System.out.println("+----------------------------------------------------------------+");
        System.out.println("| Database Management Menu                                       |");
        System.out.println("+----------------------------------------------------------------+");
        System.out.println("| 1. Wipe All Data - Delete all records from database            |");
        System.out.println("| 2. Reset Database - Restore to initial populated state         |");
        System.out.println("| 3. View Database Statistics - Show table record counts         |");
        System.out.println("+----------------------------------------------------------------+");
        System.out.println("| 4. Back to Help Menu                                           |");
        System.out.println("+----------------------------------------------------------------+");
    }
}
