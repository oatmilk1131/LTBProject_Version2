package LTBPaintCenter.model;

import java.util.*;

public class Inventory {
    private Map<String, Product> map = new LinkedHashMap<>();

    public void addProduct(Product p) { map.put(p.getId(), p); }
    public Product getProduct(String id) { return map.get(id); }
    public Collection<Product> getAll() { return map.values(); }
    public void removeProduct(String id) { map.remove(id); }

    public void updateQuantity(String id, int delta) {
        Product p = map.get(id);
        if (p != null) {
            int newQty = p.getQuantity() + delta;
            if (newQty < 0) newQty = 0;
            p.setQuantity(newQty);
        }
    }

    // Phase 2 addition: get total sold per product
    public Map<String, Integer> getTotalSold(Map<String,Integer> cumulativeSales) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (var entry : map.entrySet()) {
            String id = entry.getKey();
            int sold = cumulativeSales.getOrDefault(id, 0);
            result.put(id, sold);
        }
        return result;
    }
}
