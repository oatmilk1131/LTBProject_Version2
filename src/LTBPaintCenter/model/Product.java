package LTBPaintCenter.model;

public class Product {
    private String id;
    private String name;
    private double price;
    private int quantity;
    private String brand;
    private String color;
    private String type;

    public Product(String id, String name, double price, int quantity,
                   String brand, String color, String type) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.brand = brand;
        this.color = color;
        this.type = type;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getBrand() { return brand; }
    public String getColor() { return color; }
    public String getType() { return type; }

    //Setters
    public void setQuantity(int q) { this.quantity = q; }
    public void setPrice(double p) { this.price = p; }
    public void setName(String n) { this.name = n; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setColor(String color) { this.color = color; }
    public void setType(String type) { this.type = type; }
}