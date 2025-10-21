package LTBPaintCenter.model;

public class SaleItem {
    private final String productId;
    private final String name;
    private final double price;
    private int qty;

    public SaleItem(String productId, String name, double price, int qty) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.qty = qty;
    }

    public void addQuantity(int additional) {
        this.qty += additional;
    }

    public double getSubtotal() { return price * qty; }
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public int getQty() { return qty; }
    public double getPrice() { return price; }

    public void setQty(int qty) { this.qty = qty; }
}