package LTBPaintCenter.model;

import java.util.*;

public class Inventory {
    private Map<Integer, Product> map = new HashMap<>();

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
}