package LTBPaintCenter.model;

import java.util.*;

public class Report {
    private final List<Sale> sales = new ArrayList<>();
    private final Map<String, Integer> cumulativeProductSales = new HashMap<>();

    public void recordSale(Sale s) {
        sales.add(s);
        for (SaleItem it : s.getItems()) {
            cumulativeProductSales.put(
                    it.getProductId(),
                    cumulativeProductSales.getOrDefault(it.getProductId(), 0) + it.getQty()
            );
        }
    }

    public List<Sale> getSales() {
        return Collections.unmodifiableList(sales);
    }

    public int getTotalSold(String productId) {
        return cumulativeProductSales.getOrDefault(productId, 0);
    }

    public double getTotalRevenue() {
        return sales.stream().mapToDouble(Sale::getTotal).sum();
    }
}
