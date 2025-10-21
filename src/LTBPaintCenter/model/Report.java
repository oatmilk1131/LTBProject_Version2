package LTBPaintCenter.model;

import java.util.*;

public class Report {
    private List<Sale> sales = new ArrayList<>();
    private Map<String,Integer> cumulativeProductSales = new HashMap<>();

    public void recordSale(Sale s) {
        sales.add(s);
        for(SaleItem it : s.getItems()) {
            cumulativeProductSales.put(it.getProductId(),
                    cumulativeProductSales.getOrDefault(it.getProductId(),0) + it.getQty());
        }
    }
    public List<Sale> getSales() { return Collections.unmodifiableList(sales); }
}