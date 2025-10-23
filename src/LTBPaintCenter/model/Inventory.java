package LTBPaintCenter.model;

import java.util.*;

public class Inventory {
    private final Map<Integer, Product> map = new HashMap<>();

    public Product getProduct(int id) {
        return map.get(id);
    }

    public void removeProduct(int id) {
        map.remove(id);
    }

    public void addProduct(Product p) {
        map.put(p.getId(), p);
    }

    public Collection<Product> getAll() {
        return map.values();
    }

    public void updateQuantity(int id, int delta) {
        Product p = map.get(id);
        if (p != null) {
            p.setQuantity(Math.max(0, p.getQuantity() + delta));
        }
    }

    public void clear() {
        map.clear();
    }
}