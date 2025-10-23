package LTBPaintCenter.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public static void addProduct(Product product) {
        String sql = "INSERT INTO products (name, price, quantity, brand, color, type) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            //pstmt.setString(1, product.getId());
            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setInt(3, product.getQuantity());
            pstmt.setString(4, product.getBrand());
            pstmt.setString(5, product.getColor());
            pstmt.setString(6, product.getType());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Add product failed: " + e.getMessage());
        }
    }

    //String id, String name, double price, int quantity, String brand, String color, String type

    public static List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Product(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
                        rs.getString("brand"),
                        rs.getString("color"),
                        rs.getString("type")
                ));
            }
        } catch (SQLException e) {
            System.err.println("❌ Fetch failed: " + e.getMessage());
        }
        return list;
    }

    public static void deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Delete failed: " + e.getMessage());
        }
    }

    public static void updateProduct(Product product) {
        String sql = "UPDATE products SET name=?, category=?, price=?, quantity=? WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, product.getId());
            pstmt.setString(2, product.getName());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setInt(4, product.getQuantity());
            pstmt.setString(5, product.getBrand());
            pstmt.setString(6, product.getColor());
            pstmt.setString(7, product.getType());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Update failed: " + e.getMessage());
        }
    }
}