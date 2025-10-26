package LTBPaintCenter.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles creation and upgrading of the SQLite database schema.
 * Updated to include product expiration tracking and status management.
 */
public class DatabaseSetup {

    public static void initializeDatabase() {
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {

            // === PRODUCTS / INVENTORY TABLE ===
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS inventory (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    product_code TEXT,
                    name TEXT NOT NULL,
                    brand TEXT NOT NULL,
                    color TEXT,
                    type TEXT,
                    price REAL NOT NULL,
                    qty INTEGER NOT NULL DEFAULT 0,
                    date_imported TEXT DEFAULT (DATE('now')),
                    expiration_date TEXT,
                    status TEXT DEFAULT 'Active'
                );
            """);

            // Attempt to add product_code column for existing databases
            try {
                stmt.execute("ALTER TABLE inventory ADD COLUMN product_code TEXT");
            } catch (Exception ignored) {
                // Column may already exist; ignore errors
            }

            // === SALES TABLE (for POS transactions) ===
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS sales (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sale_reference TEXT,
                    product_id INTEGER NOT NULL,
                    product_name TEXT NOT NULL,
                    quantity INTEGER NOT NULL,
                    price REAL NOT NULL,
                    total REAL NOT NULL,
                    sale_date TEXT DEFAULT (DATETIME('now')),
                    FOREIGN KEY(product_id) REFERENCES inventory(id)
                );
            """);

            // Attempt to add sale_reference for existing databases
            try {
                stmt.execute("ALTER TABLE sales ADD COLUMN sale_reference TEXT");
            } catch (Exception ignored) {
                // Column may already exist; ignore errors
            }

            // === OPTIONAL: LOGS TABLE (for monitoring events) ===
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    message TEXT NOT NULL,
                    log_type TEXT,
                    log_date TEXT DEFAULT (DATETIME('now'))
                );
            """);

            System.out.println(" Database successfully initialized / verified.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println(" Database setup failed: " + e.getMessage());
        }
    }
}
