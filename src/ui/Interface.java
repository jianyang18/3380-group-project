package ui;

import java.sql.*;
import java.util.Scanner;
import database.DatabaseManager;
import logic.*;
import utils.InputValidator;
import utils.QueryUtils;

/**
 * Main UI coordinator
 * handles user interaction and navigation
 */
public class Interface {
    
    private Scanner scanner;
    private Connection conn;

    /**
     * Constructor - Initialize Interface with connection and scanner
     */
    public Interface(Connection conn, Scanner scanner) {
        this.conn = conn;
        this.scanner = scanner;
    }
    
    /**
     * Start the UI - main loop
     */
    public void start() {
        boolean running = true;
        while (running) {
            int choice = welcomeMenu();
            switch (choice) {
                case 1:
                    mainMenu();
                    break;
                case 2:
                    StatisticsDisplay.showStatistics(conn);
                    QueryUtils.waitForEnter(scanner);
                    break;
                case 3:
                    MenuSystem.printHelp();
                    QueryUtils.waitForEnter(scanner);
                    break;
                case 4:
                    running = false;
                    break;
            }
        }
        System.out.println("\nThank you for using our IMDb Database System!");
        System.out.println("Goodbye.\n");
    }


    /**
     * Display welcome menu and get user choice
     */
    private int welcomeMenu() {
        MenuSystem.printWelcomeMenu();
        return InputValidator.readInt(scanner, "Select an option (1-4) and press ENTER: ", 1, 4);
    }


    /**
     * Main menu - query categories
     */
    private void mainMenu() {
        boolean stay = true;
        while (stay) {
            MenuSystem.printMainMenu();
            int choice = InputValidator.readInt(scanner, "Select an option (1-8) and press ENTER: ", 1, 8);

            switch (choice) {
                case 1:
                    // Title Queries
                    TitleQueries titleHandler = new TitleQueries(conn, scanner);
                    titleHandler.showMenu();
                    break;
                case 2:
                    // Person Queries
                    PersonQueries personHandler = new PersonQueries(conn, scanner);
                    personHandler.showMenu();
                    break;
                case 3:
                    // Character Queries
                    CharacterQueries characterHandler = new CharacterQueries(conn, scanner);
                    characterHandler.showMenu();
                    break;
                case 4:
                    // Genre & Stats Queries
                    GenreQueries genreHandler = new GenreQueries(conn, scanner);
                    genreHandler.showMenu();
                    break;
                case 5:
                    // Database Management
                    database.DatabaseManager dbManager = new database.DatabaseManager(conn, scanner);
                    dbManager.showDatabaseManagementMenu();
                    break;
                case 6:
                    StatisticsDisplay.showStatistics(conn);
                    QueryUtils.waitForEnter(scanner);
                    break;
                case 7:
                    MenuSystem.printHelp();
                    QueryUtils.waitForEnter(scanner);
                    break;
                case 8:
                    stay = false;
                    break;
            }
        }
    }
}