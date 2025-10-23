package LTBPaintCenter.model;

import java.util.*;

public class Inventory {
    private final Map<String, Product> map = new LinkedHashMap<>();

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

    public void updateStatus(String id, String status) {
        Product p = map.get(id);
        if (p != null) p.setStatus(status);
    }

    public List<Product> getCriticalProducts() {
        List<Product> list = new ArrayList<>();
        for (Product p : map.values()) {
            if (p.isExpired() || p.getQuantity() <= 5) {
                list.add(p);
            }
        }
        return list;
    }
}
