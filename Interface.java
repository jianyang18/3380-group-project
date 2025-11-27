import java.sql.*;
import java.util.*;

public class Interface {

    // ===============================
    // GLOBALS
    // ===============================
    static Scanner scan = new Scanner(System.in);
    static Connection conn;

    // ===============================
    // MAIN
    // ===============================
    public static void main(String[] args) {

        // connectDB();

        boolean running = true;
        while (running) {
            int choice = welcomeMenu();
            switch (choice) {
                case 1:
                    mainMenu();
                    break;
                case 2:
                    statisticsMenu();
                    break;
                case 3:
                    helpMenu();
                    break;
                case 4:
                    running = false;
                    break;
                default:
                    break;
            }
        }

        // closeDB();
        System.out.println("Goodbye.");
    }


    // ===============================
    // DATABASE CONNECTION
    // ===============================
    static void connectDB() {
        try {
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/imdb",
                "your_user",
                "your_pass"
            );
            System.out.println("Connected to database.");
        } catch (SQLException e) {
            System.out.println("Cannot connect: " + e.getMessage());
            System.exit(0);
        }
    }

    static void closeDB() {
        try { if (conn != null) conn.close(); }
        catch (SQLException ignored) {}
    }


    // ===============================
    // WELCOME MENU
    // ===============================
    static int welcomeMenu() {

        final int innerWidth = 69;
        String border = "+" + "-".repeat(innerWidth) + "+";

        System.out.println();
        System.out.println(border);
        System.out.println("|" + " ".repeat(24) + "IMDb Database Project" + " ".repeat(24) + "|");
        System.out.println(border);
        System.out.printf("| %-67s |%n", " A comprehensive movie & TV database built from");
        System.out.printf("| %-67s |%n", " official IMDb datasets. This system allows users");
        System.out.printf("| %-67s |%n", " to explore titles, ratings, genres, cast, crew,");
        System.out.printf("| %-67s |%n", " characters, TV episodes, and more using SQL queries.");
        System.out.println(border);
        System.out.printf("| %-3s %-22s %-40s |%n", "1.", "Enter System", "");
        System.out.printf("| %-3s %-22s %-40s |%n", "2.", "Statistics", "- summary / aggregate reports");
        System.out.printf("| %-3s %-22s %-40s |%n", "3.", "Help", "- usage info and examples");
        System.out.printf("| %-3s %-22s %-40s |%n", "4.", "Quit", "");
        System.out.println(border);
        System.out.println();

        return readInt("    Select an option (1-4) and press ENTER: ", 1, 4);
    }


    // ===============================
    // MAIN MENU
    // ===============================
    static void mainMenu() {

        final int innerWidth = 76;
        String border = "+" + "-".repeat(innerWidth) + "+";
        String header = "|" + " ".repeat(30) + "Main Query Menu" + " ".repeat(31) + "|";
        String rowFormat = "| %-35s - %-36s |";

        boolean stay = true;
        while (stay) {

            System.out.println();
            System.out.println(border);
            System.out.println(header);
            System.out.println(border);
            System.out.printf(rowFormat + "%n", "1. Title Queries", "search movies / TV titles");
            System.out.printf(rowFormat + "%n", "2. Person Queries", "actors, directors, writers");
            System.out.printf(rowFormat + "%n", "3. Character Queries", "characters and who played them");
            System.out.printf(rowFormat + "%n", "4. Series & Episode Queries", "seasons and episodes");
            System.out.println(border);
            System.out.printf(rowFormat + "%n", "5. Statistics", "summary / aggregate reports");
            System.out.printf(rowFormat + "%n", "6. Help", "usage info and examples");
            System.out.printf(rowFormat + "%n", "7. Return", "return to previous screen");
            System.out.println(border);
            System.out.println();

            int choice = readInt("    Select an option (1-7) and press ENTER: ", 1, 7);

            switch (choice) {
                case 1:
                    titleMenu();
                    break;
                case 2:
                    System.out.println("[TODO] Person queries");
                    break;
                case 3:
                    System.out.println("[TODO] Character queries");
                    break;
                case 4:
                    System.out.println("[TODO] Series/Episode queries");
                    break;
                case 5:
                    statisticsMenu();
                    break;
                case 6:
                    helpMenu();
                    break;
                case 7:
                    stay = false;
                    break;
                default:
                    break;
            }
        }
    }


    // ===============================
    // HELP MENU
    // ===============================
    static void helpMenu() {

        final int innerWidth = 76;
        String border = "+" + "-".repeat(innerWidth) + "+";
        String header = "|" + " ".repeat(33) + "Help Menu" + " ".repeat(34) + "|";
        String rowFormat = "| %-35s - %-36s |";

        System.out.println();
        System.out.println(border);
        System.out.println(header);
        System.out.println(border);
        System.out.printf(rowFormat + "%n", "1. Explain All Queries", "descriptions of every menu option");
        System.out.printf(rowFormat + "%n", "", "and how to use them");
        System.out.printf("| %-74s |%n", "");
        System.out.printf(rowFormat + "%n", "2. Wipe Out Database", "delete all records from the");
        System.out.printf(rowFormat + "%n", "", "current database");
        System.out.printf("| %-74s |%n", "");
        System.out.printf(rowFormat + "%n", "3. Reset Database", "restore database to initial");
        System.out.printf(rowFormat + "%n", "", "state using backup copy");
        System.out.println(border);
        System.out.printf(rowFormat + "%n", "4. Statistics", "summary / aggregate reports");
        System.out.printf(rowFormat + "%n", "5. Return", "return to previous screen");
        System.out.println(border);
        System.out.println();

        readInt("    Select an option (1-6) and press ENTER: ", 1, 6);
    }


    // ===============================
    // STATISTICS (TODO)
    // ===============================
    static void statisticsMenu() {
        System.out.println("[Statistics] (placeholder).");
    }


    // ===============================
    // TITLE QUERY MENU
    // ===============================
    static void titleMenu() {

        final int innerWidth = 76;
        String border = "+" + "-".repeat(innerWidth) + "+";
        String header = "|" + " ".repeat(30) + "Title Query Menu" + " ".repeat(31) + "|";
        String rowFormat = "| %-35s - %-36s |";

        boolean stay = true;

        while (stay) {
            System.out.println();
            System.out.println(border);
            System.out.println(header);
            System.out.println(border);
            System.out.printf("| %-74s |%n", " 1. Find \"hidden gem\" movies and \"blockbuster\" movies");
            System.out.printf("| %-74s |%n", " 2. Global Title vs Regional Title Performance");
            System.out.printf("| %-74s |%n", " 3. Genre Rating Trends by Decade");
            System.out.printf("| %-74s |%n", " 4. List All Movies Featuring a Specific Actor");
            System.out.println(border);
            System.out.printf(rowFormat + "%n", "5. Statistics", "summary / aggregate reports");
            System.out.printf(rowFormat + "%n", "6. Help", "usage info and examples");
            System.out.printf(rowFormat + "%n", "7. Return", "return to previous screen");
            System.out.println(border);
            System.out.println();

            int choice = readInt("    Select an option (1-7) and press ENTER: ", 1, 7);

            switch(choice) {
                case 1:
                    System.out.println("[TODO]");
                    break;
                case 2:
                    System.out.println("[TODO]");
                    break;
                case 3:
                    System.out.println("[TODO]");
                    break;
                case 4:
                    listMoviesByActor();
                    break;
                case 5:
                    statisticsMenu();
                    break;
                case 6:
                    helpMenu();
                    break;
                case 7:
                    stay = false;
                    break;
                default:
                    break;
            }
        }
    }


    // ========================================================
    // MAIN QUERY IMPLEMENTATION
    // List all movies featuring an actor
    // ========================================================
    static void listMoviesByActor() {

        System.out.println("list Movies By Actor query (placeholder)");
    }


    // ===============================
    // Function for validating input value
    // ===============================
    static int readInt(String msg, int min, int max) {
        while (true) {
            System.out.print(msg);
            String s = scan.nextLine().trim();
            try {
                int n = Integer.parseInt(s);
                if (n < min || n > max) throw new Exception();
                return n;
            } catch (Exception e) {
                System.out.println("Invalid input.");
            }
        }
    }

}
