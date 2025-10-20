package LTBPaintCenter.controller;

import LTBPaintCenter.model.Inventory;
import LTBPaintCenter.model.Product;
import LTBPaintCenter.view.InventoryPanel;

import java.util.Collection;

public class InventoryController {
    private Inventory inventory;
    private InventoryPanel view;

    public InventoryController(Inventory inventory) {
        this.inventory = inventory;
        this.view = new InventoryPanel();

        attachListeners();
        refreshInventory(); // initial load
    }

    public InventoryPanel getView() {
        return view;
    }

    private void attachListeners() {
        view.setAddOrUpdateListener(() -> addOrUpdate());
        view.setDeleteListener(() -> delete());
    }

    public void refreshInventory() {
        Collection<Product> allProducts = inventory.getAll();
        view.refreshInventory(allProducts);
    }

    private void addOrUpdate() {
        String id = view.getIdField().getText().trim();
        String name = view.getNameField().getText().trim();
        double price;
        int qty;

        try {
            price = Double.parseDouble(view.getPriceField().getText().trim());
            qty = Integer.parseInt(view.getQtyField().getText().trim());
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(view, "Invalid price or quantity");
            return;
        }

        Product existing = inventory.getProduct(id);
        if (existing != null) {
            existing.setName(name);
            existing.setPrice(price);
            existing.setQuantity(qty);
        } else {
            inventory.addProduct(new Product(id, name, price, qty));
        }
        refreshInventory();
    }

    private void delete() {
        String id = view.getIdField().getText().trim();
        if (id.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(view, "Select a product first");
            return;
        }
        inventory.removeProduct(id);
        refreshInventory();
    }
}
