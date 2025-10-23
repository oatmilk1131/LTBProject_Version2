package LTBPaintCenter.model;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseSetup {
    public static void initialize() {
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {

            String sql = """
                CREATE TABLE IF NOT EXISTS products (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL,
                            brand TEXT NOT NULL,
                            color TEXT NOT NULL,
                            type TEXT NOT NULL,
                            price REAL NOT NULL,
                            quantity INTEGER NOT NULL
                        );
            """;
            stmt.execute(sql);
            System.out.println("Product table ready.");
        } catch (Exception e) {
            System.err.println("Table setup failed: " + e.getMessage());
        }
    }
}
