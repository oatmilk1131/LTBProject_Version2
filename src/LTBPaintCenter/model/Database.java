package LTBPaintCenter.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String URL = "jdbc:sqlite:ltbpaintcenter.db";
    private static Connection connection;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(URL);
                System.out.println("Connected to SQLite database.");
            } catch (SQLException e) {
                System.err.println("Failed to connect: " + e.getMessage());
            }
        }
        return connection;
    }
}