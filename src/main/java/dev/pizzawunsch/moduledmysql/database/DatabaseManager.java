package dev.pizzawunsch.moduledmysql.database;

import java.sql.*;

/**
 * Provides a static manager for establishing, checking,
 * using, and closing a connection to a MySQL database.
 *
 * This class is designed for global usage across the application and does not
 * support multiple concurrent connections. All methods assume a single, shared
 * connection.
 *
 * @author Lucas | PizzaWunsch
 */
public class DatabaseManager {

    private static Connection connection;

    /**
     * Establishes a connection to the MySQL database.
     *
     * @param host     the database server hostname or IP address
     * @param port     the database server port (typically 3306)
     * @param database the name of the database to connect to
     * @param username the username for authentication
     * @param password the password for authentication
     */
    public static void connect(String host, String port, String database, String username, String password) {
        if (isConnected()) {
            System.out.println("[Moduled - MySQL] Already connected to the database.");
            return;
        }

        String url = String.format("jdbc:mysql://%s:%s/%s?autoReconnect=true&useSSL=false&serverTimezone=UTC", host, port, database);
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("[Moduled - MySQL] Database connection established.");
        } catch (ClassNotFoundException e) {
            System.err.println("[Moduled - MySQL] MySQL JDBC driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("[Moduled - MySQL] Failed to connect to the database.");
            e.printStackTrace();
        }
    }

    /**
     * Closes the active database connection.
     *
     * Should be called when the application shuts down or no longer requires
     * database access.
     */
    public static void disconnect() {
        if (!isConnected()) {
            System.out.println("[Moduled - MySQL] No active database connection to disconnect.");
            return;
        }

        try {
            connection.close();
            connection = null;
            System.out.println("[Moduled - MySQL] Disconnected from the database.");
        } catch (SQLDisconnectingException e) {
            System.err.println("[Moduled - MySQL] Error while disconnecting.");
            e.printStackTrace();
        }
    }

    /**
     * Checks if the connection to the database is currently active and valid.
     *
     * @return true if the connection is open and usable; false otherwise
     */
    public static boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            System.err.println("[Moduled - MySQL] Error while checking connection state.");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves the current active database connection.
     *
     * @return the {@link Connection} object if connected; otherwise null
     */
    public static Connection getConnection() {
        if (!isConnected()) {
            System.err.println("[Moduled - MySQL] No database connection available.");
        }
        return connection;
    }

    /**
     * Executes a raw SQL SELECT query and returns a {@link ResultSet}.
     *
     * @param sql the SQL SELECT statement to execute
     * @return the {@link ResultSet} containing query results, or null if an error occurs
     */
    public static ResultSet query(String sql) {
        if (!isConnected()) {
            System.err.println("[Moduled - MySQL] Cannot execute query. No database connection.");
            return null;
        }

        try {
            return connection.createStatement().executeQuery(sql);
        } catch (SQLException e) {
            System.err.println("[Moduled - MySQL] Failed to execute query: " + sql);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Executes an SQL UPDATE, INSERT, or DELETE statement.
     *
     * @param sql the SQL update statement to execute
     */
    public static void update(String sql) {
        if (!isConnected()) {
            System.err.println("[Moduled - MySQL] Cannot execute update. No database connection.");
            return;
        }

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("[Moduled - MySQL] Update failed: " + sql);
            e.printStackTrace();
        }
    }

    /**
     * Prepares a SQL statement for execution with parameters.
     *
     * @param sql the SQL string with placeholders (e.g. "SELECT * FROM users WHERE id = ?")
     * @return a {@link PreparedStatement} ready to be configured and executed, or null if an error occurs
     */
    public static PreparedStatement prepare(String sql) {
        if (!isConnected()) {
            System.err.println("[Moduled - MySQL] Cannot prepare statement. No database connection.");
            return null;
        }

        try {
            return connection.prepareStatement(sql);
        } catch (SQLException e) {
            System.err.println("[Moduled - MySQL] Failed to prepare statement: " + sql);
            e.printStackTrace();
            return null;
        }
    }
}
