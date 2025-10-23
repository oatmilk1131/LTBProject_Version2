package LTBPaintCenter.model;

import java.sql.*;
import java.util.*;
import java.time.*;
import java.time.temporal.ChronoUnit;

public class InventoryDatabase {

    private static final String URL = "jdbc:mysql://127.0.0.1:3306/ltb_paint";
    private static final String USER = "root";
    private static final String PASSWORD = "admin123";

    public InventoryDatabase() {
        createTableIfNotExists();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS inventory (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL," +
                "brand VARCHAR(50)," +
                "color VARCHAR(50)," +
                "type VARCHAR(50)," +
                "price DECIMAL(10,2)," +
                "qty INT," +
                "date_imported DATE," +
                "expiration_date DATE NOT NULL," +
                "status ENUM('active','expired') DEFAULT 'active')";

        try (Connection conn = connect(); Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertSeedData() {
        String countQuery = "SELECT COUNT(*) FROM inventory";
        String insertQuery = "INSERT INTO inventory (name, brand, color, type, price, qty, expiration_date) VALUES (?,?,?,?,?,?,?)";
        Object[][] data = {
                {"Boysen - Red Acrylic", "Boysen", "Red", "Acrylic", 450.0, 30, "2025-12-01"},
                {"Boysen - White Latex", "Boysen", "White", "Latex", 420.0, 25, "2025-12-15"},
                {"Boysen - Green Enamel", "Boysen", "Green", "Enamel", 470.0, 15, "2025-11-29"},
                {"Davies - Blue Oil", "Davies", "Blue", "Oil", 460.0, 20, "2025-11-25"},
                {"Davies - Yellow Latex", "Davies", "Yellow", "Latex", 440.0, 20, "2025-12-20"},
                {"Nation - Black Primer", "Nation", "Black", "Primer", 400.0, 18, "2025-12-10"},
                {"Nation - Gray Enamel", "Nation", "Gray", "Enamel", 430.0, 22, "2025-11-27"}
        };

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countQuery)) {

            rs.next();
            int count = rs.getInt(1);

            // ✅ Only insert if table is empty
            if (count == 0) {
                try (PreparedStatement ps = conn.prepareStatement(insertQuery)) {
                    for (Object[] d : data) {
                        ps.setString(1, (String) d[0]);
                        ps.setString(2, (String) d[1]);
                        ps.setString(3, (String) d[2]);
                        ps.setString(4, (String) d[3]);
                        ps.setDouble(5, (Double) d[4]);
                        ps.setInt(6, (Integer) d[5]);
                        ps.setString(7, (String) d[6]);
                        ps.executeUpdate();
                    }
                }
                System.out.println("✅ Seed data inserted successfully.");
            } else {
                System.out.println("ℹ️ Seed data already exists, skipping insert.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Product> loadActiveProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM inventory WHERE status='active'";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Product p = new Product(
                        String.valueOf(rs.getInt("id")),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("qty"),
                        rs.getString("brand"),
                        rs.getString("color"),
                        rs.getString("type")
                );
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void updateExpiredProducts() {
        String sql = "UPDATE inventory SET status='expired' WHERE expiration_date < CURDATE()";
        try (Connection conn = connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getAlerts() {
        List<String> alerts = new ArrayList<>();
        String sql = "SELECT name, qty, expiration_date, status FROM inventory";

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/ltb_paint", "root", "admin123");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            LocalDate today = LocalDate.now();

            while (rs.next()) {
                String name = rs.getString("name");
                int qty = rs.getInt("qty");
                LocalDate expDate = rs.getDate("expiration_date").toLocalDate();
                String status = rs.getString("status");

                long daysLeft = ChronoUnit.DAYS.between(today, expDate);

                // Check for expiring soon (within a week)
                if (daysLeft <= 7 && daysLeft > 0 && !"expired".equalsIgnoreCase(status)) {
                    alerts.add(String.format("%s expires in %d day%s.", name, daysLeft, daysLeft == 1 ? "" : "s"));
                }

                // Check for expired
                if (daysLeft <= 0 && !"expired".equalsIgnoreCase(status)) {
                    alerts.add(String.format("%s has expired today or earlier.", name));

                    // Optional: auto-mark expired
                    String update = "UPDATE inventory SET status='expired' WHERE name=?";
                    try (PreparedStatement ps = conn.prepareStatement(update)) {
                        ps.setString(1, name);
                        ps.executeUpdate();
                    }
                }

                // Check for low stock (<= 5)
                if (qty <= 5 && qty > 0) {
                    alerts.add(String.format("%s is low on stock (%d left).", name, qty));
                }

                // Out of stock
                if (qty == 0) {
                    alerts.add(String.format("%s is out of stock.", name));
                }
            }

        } catch (SQLException e) {
            alerts.add("Error retrieving alerts: " + e.getMessage());
            e.printStackTrace();
        }

        // If no alerts found
        if (alerts.isEmpty()) {
            alerts.add("No alerts at this time.");
        }

        return alerts;
    }
}
