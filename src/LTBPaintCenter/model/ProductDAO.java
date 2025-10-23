package LTBPaintCenter.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public static int addProduct(Product p) throws SQLException {
        String sql = "INSERT INTO products (name, price, quantity, brand, color, type) VALUES (?, ?, ?, ?, ?, ?)";

        System.out.println("Writing to DB: " + new java.io.File("ltbpaintcenter.db").getAbsolutePath());

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false); // ensure explicit commit
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, p.getName());
                stmt.setDouble(2, p.getPrice());
                stmt.setInt(3, p.getQuantity());
                stmt.setString(4, p.getBrand());
                stmt.setString(5, p.getColor());
                stmt.setString(6, p.getType());

                int affected = stmt.executeUpdate();
                if (affected == 0) {
                    throw new SQLException("Inserting product failed, no rows affected.");
                }

                int newId;
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        newId = keys.getInt(1);
                        p.setId(newId);
                    } else {
                        throw new SQLException("Inserting product failed, no ID obtained.");
                    }
                }

                conn.commit();
                System.out.println("Product inserted: " + p.getName() + " (ID " + p.getId() + ")");
                return p.getId();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public static java.util.List<String> getDistinct(String columnName) {
        java.util.List<String> list = new java.util.ArrayList<>();
        String sql = "SELECT DISTINCT " + columnName + " FROM products " +
                "WHERE " + columnName + " IS NOT NULL AND TRIM(" + columnName + ") <> '' " +
                "ORDER BY " + columnName + " ASC";

        try (Connection conn = Database.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString(columnName));
            }
        } catch (Exception e) {
            System.err.println("Failed to load distinct " + columnName + ": " + e.getMessage());
        }
        return list;
    }

    public static void updateProduct(Product product) throws SQLException {
        String sql = "UPDATE products SET name=?, price=?, quantity=?, brand=?, color=?, type=? WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setInt(3, product.getQuantity());
            pstmt.setString(4, product.getBrand());
            pstmt.setString(5, product.getColor());
            pstmt.setString(6, product.getType());
            pstmt.setInt(7, product.getId());

            pstmt.executeUpdate();
        }
    }

    public static void deleteProduct(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }


    public static List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT id, name, price, quantity, brand, color, type FROM products ORDER BY id ASC";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Product p = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
                        rs.getString("brand"),
                        rs.getString("color"),
                        rs.getString("type")
                );
                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Fetch failed: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public static Product getById(int id) {
        String sql = "SELECT id, name, price, quantity, brand, color, type FROM products WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("quantity"),
                            rs.getString("brand"),
                            rs.getString("color"),
                            rs.getString("type")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("getById failed: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
