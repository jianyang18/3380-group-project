package database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * database connection management
 * 
 * handles connections to local SQLite database or remote SQL Server
 */
public class DatabaseConfig {
    
    private static final boolean USE_LOCAL_SQLITE = false;  // Set to false for uranium server
    
    // Local SQLite database path
    private static final String SQLITE_DB_PATH = "resources/imdb.db";
    
    // Configuration file path
    private static final String CONFIG_FILE = "auth.cfg";
    
    // SQL Server connection settings
    private static final String DB_HOST = "uranium.cs.umanitoba.ca";
    private static final String DB_PORT = "1433";
    private static final String DB_NAME = "cs3380"; 
    
    private static Properties credentials = null;
    
    /**
     * Load credentials from auth.cfg file
     * 
     * @return true if credentials loaded successfully
     */
    private static boolean loadCredentials() {
        if (credentials != null) {
            return true;  // Already loaded
        }
        
        credentials = new Properties();
        try {
            credentials.load(new FileInputStream(CONFIG_FILE));
            
            // Validate required properties exist
            if (credentials.getProperty("username") == null || 
                credentials.getProperty("password") == null) {
                System.err.println("Error: auth.cfg must contain 'username' and 'password'");
                return false;
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("Error loading " + CONFIG_FILE + ": " + e.getMessage());
            System.err.println("\nPlease create auth.cfg in project root with:");
            System.err.println("username=your_username");
            System.err.println("password=your_password");
            return false;
        }
    }
    
    /**
     * Get a connection to the database
     * 
     * @return Connection object, or null if connection fails
     */
    public static Connection getConnection() {
        if (USE_LOCAL_SQLITE) {
            return getLocalSQLiteConnection();
        } else {
            return getConnection(DB_NAME);
        }
    }
    
    /**
     * Get a connection to local SQLite database
     * 
     * @return Connection object, or null if connection fails
     */
    private static Connection getLocalSQLiteConnection() {
        String connectionUrl = "jdbc:sqlite:" + SQLITE_DB_PATH;
        
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(connectionUrl);
            System.out.println("Connected to local SQLite database: " + SQLITE_DB_PATH);
            return conn;
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found!");
            System.err.println("Please add sqlite-jdbc.jar to lib/ directory");
            return null;
        } catch (SQLException e) {
            System.err.println("SQLite connection error: " + e.getMessage());
            System.err.println("Database file: " + SQLITE_DB_PATH);
            return null;
        }
    }
    
    /**
     * Get a connection to a specific database (SQL Server only)
     * 
     * @param databaseName Name of the database to connect to
     * @return Connection object, or null if connection fails
     */
    public static Connection getConnection(String databaseName) {
        if (USE_LOCAL_SQLITE) {
            return getLocalSQLiteConnection();  // Ignore database name for SQLite
        }
        
        if (!loadCredentials()) {
            return null;
        }
        
        String username = credentials.getProperty("username");
        String password = credentials.getProperty("password");
        
        String connectionUrl = "jdbc:sqlserver://" + DB_HOST + ":" + DB_PORT + ";" +
                             "databaseName=" + databaseName + ";" +
                             "user=" + username + ";" +
                             "password=" + password + ";" +
                             "encrypt=false;" +
                             "trustServerCertificate=true;" +
                             "loginTimeout=30;";
        
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection conn = DriverManager.getConnection(connectionUrl);
            return conn;
        } catch (ClassNotFoundException e) {
            System.err.println("SQL Server JDBC driver not found! Ensure mssql-jdbc.jar is on the classpath.");
            return null;
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            System.err.println("\nTroubleshooting:");
            System.err.println("1. Verify your credentials in auth.cfg");
            System.err.println("2. Check network connection to uranium.cs.umanitoba.ca");
            System.err.println("3. Ensure SQL Server JDBC driver is in lib/");
            return null;
        }
    }
    
    /**
     * Test the database connection
     * 
     * @return true if connection successful
     */
    public static boolean testConnection() {
        System.out.println("Testing database connection...");
        
        Connection conn = getConnection();
        if (conn != null) {
            try {
                if (USE_LOCAL_SQLITE) {
                    System.out.println("Successfully connected to local SQLite: " + SQLITE_DB_PATH);
                } else {
                    System.out.println("Successfully connected to " + DB_HOST + "/" + DB_NAME);
                }
                conn.close();
                return true;
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
                return false;
            }
        } else {
            System.err.println("Connection failed");
            return false;
        }
    }
    
    /**
     * Get the configured username
     * 
     * @return username from auth.cfg, or null if not loaded
     */
    public static String getUsername() {
        if (!loadCredentials()) {
            return null;
        }
        return credentials.getProperty("username");
    }
    
    /**
     * Get the database host
     * 
     * @return database host URL
     */
    public static String getHost() {
        return DB_HOST;
    }
    
    /**
     * Get the database name
     * 
     * @return database name
     */
    public static String getDatabaseName() {
        return DB_NAME;
    }
}
