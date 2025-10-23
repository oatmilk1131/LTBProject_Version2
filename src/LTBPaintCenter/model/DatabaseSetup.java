package LTBPaintCenter.model;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseSetup {

    public static void initialize() {
        // DEV: set to true to force delete and recreate DB (do not enable in production)
        boolean resetDatabase = false;
        if (resetDatabase) {
            try {
                new java.io.File("ltbpaintcenter.db").delete();
                System.out.println("⚠️ Old ltbpaintcenter.db deleted (dev reset).");
            } catch (Exception ignored) {}
        }

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {

            String sql = """
                CREATE TABLE IF NOT EXISTS products (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    price REAL NOT NULL,
                    quantity INTEGER NOT NULL,
                    brand TEXT NOT NULL,
                    color TEXT NOT NULL,
                    type TEXT NOT NULL
                );
                """;

            String sqlBrands = """
            CREATE TABLE IF NOT EXISTS brands (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT UNIQUE NOT NULL
                );
                """;

            String sqlColors = """
            CREATE TABLE IF NOT EXISTS colors (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT UNIQUE NOT NULL
                );
                """;

            String sqlTypes = """
            CREATE TABLE IF NOT EXISTS types (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT UNIQUE NOT NULL
                );
                """;

            stmt.execute(sqlBrands);
            stmt.execute(sqlColors);
            stmt.execute(sqlTypes);

            stmt.execute(sql);
            System.out.println("Product table ready.");
        } catch (Exception e) {
            System.err.println("Table setup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
