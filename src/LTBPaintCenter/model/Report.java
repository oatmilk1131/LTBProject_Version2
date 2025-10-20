package LTBPaintCenter.model;

import java.util.*;

public class Report {
    private List<Sale> sales = new ArrayList<>();

    public void recordSale(Sale s) { sales.add(s); }
    public List<Sale> getSales() { return Collections.unmodifiableList(sales); }
    public int getTotalSalesCount() { return sales.size(); }
    public double getTotalRevenue() { return sales.stream().mapToDouble(Sale::getTotal).sum(); }
}
