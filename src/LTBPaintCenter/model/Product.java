package LTBPaintCenter.model;

import java.time.LocalDate;

public class Product {
    private String id;
    private String name;
    private String brand;
    private String color;
    private String type;
    private double price;
    private int quantity;
    private LocalDate dateImported;
    private LocalDate expirationDate;
    private String status; // "active" or "expired"

    // Constructor for simple creation (like seeding)
    public Product(String id, String name, double price, int quantity,
                   String brand, String color, String type) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.brand = brand;
        this.color = color;
        this.type = type;
        this.dateImported = LocalDate.now();
        this.expirationDate = LocalDate.now().plusMonths(6);
        this.status = "active";
    }

    // Constructor used when reading from MySQL
    public Product(String id, String name, String brand, String color,
                   String type, double price, int quantity,
                   LocalDate dateImported, LocalDate expirationDate, String status) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.color = color;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.dateImported = dateImported;
        this.expirationDate = expirationDate;
        this.status = status;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public LocalDate getDateImported() { return dateImported; }
    public void setDateImported(LocalDate dateImported) { this.dateImported = dateImported; }

    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isExpired() {
        return expirationDate != null && expirationDate.isBefore(LocalDate.now());
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - ₱%.2f [%d pcs] %s",
                name, type, price, quantity, status);
    }
}
