package LTBPaintCenter.model;

import java.util.*;

public class Sale {
    private String saleId;
    private Date date;
    private List<SaleItem> items = new ArrayList<>();
    private double total;

    public Sale(String saleId) {
        this.saleId = saleId;
        this.date = new Date();
    }

    public void addItem(SaleItem it) { items.add(it); total += it.getSubtotal(); }
    public String getId() { return saleId; }
    public Date getDate() { return date; }
    public double getTotal() { return total; }
    public List<SaleItem> getItems() { return items; }
}
