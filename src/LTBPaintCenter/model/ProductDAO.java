package LTBPaintCenter.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public static int addProduct(Product product) throws SQLException {
        String sql = "INSERT INTO products (name, price, quantity, brand, color, type) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setInt(3, product.getQuantity());
            pstmt.setString(4, product.getBrand());
            pstmt.setString(5, product.getColor());
            pstmt.setString(6, product.getType());

            int affected = pstmt.executeUpdate();
            if (affected == 0) throw new SQLException("Insert failed, no rows affected.");

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    product.setId(id);
                    return id;
                } else {
                    throw new SQLException("Insert succeeded but no ID obtained.");
                }
            }
        }
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
            System.err.println("❌ Fetch failed: " + e.getMessage());
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
            System.err.println("❌ getById failed: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static void deleteById(int id) {
        String sql = "DELETE FROM products WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Delete failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
