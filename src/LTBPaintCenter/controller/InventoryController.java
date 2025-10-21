package LTBPaintCenter.controller;

import LTBPaintCenter.model.Inventory;
import LTBPaintCenter.model.Product;
import LTBPaintCenter.view.InventoryPanel;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class InventoryController {
    private final Inventory inventory;
    private final InventoryPanel view;

    public InventoryController(Inventory inventory) {
        this.inventory = inventory;
        this.view = new InventoryPanel();
        attachListeners();
        refreshInventory();
    }

    private void attachListeners() {
        view.getBtnAddUpdate().addActionListener(e -> addOrUpdate());
        view.getBtnDelete().addActionListener(e -> delete());
        view.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int r = view.getTable().getSelectedRow();
                if (r >= 0) {
                    view.getTfId().setText(view.getTable().getValueAt(r, 0).toString());
                    view.getTfName().setText(view.getTable().getValueAt(r, 1).toString());
                    view.getTfPrice().setText(view.getTable().getValueAt(r, 2).toString());
                    view.getTfQty().setText(view.getTable().getValueAt(r, 3).toString());
                }
            }
        });
    }

    private void addOrUpdate() {
        String id = view.getTfId().getText().trim();
        String name = view.getTfName().getText().trim();
        if (id.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please fill in ID and Name");
            return;
        }

        try {
            double price = Double.parseDouble(view.getTfPrice().getText().trim());
            int qty = Integer.parseInt(view.getTfQty().getText().trim());
            Product existing = inventory.getProduct(id);

            if (existing == null) {
                inventory.addProduct(new Product(id, name, price, qty));
            } else {
                existing.setName(name);
                existing.setPrice(price);
                existing.setQuantity(qty);
            }
            refreshInventory();
            JOptionPane.showMessageDialog(view, "Product saved!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Invalid price or quantity");
        }
    }

    private void delete() {
        String id = view.getTfId().getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Select a product first");
            return;
        }

        if (inventory.getProduct(id) == null) {
            JOptionPane.showMessageDialog(view, "Product not found");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(view, "Delete this product?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            inventory.removeProduct(id);
            refreshInventory();
            JOptionPane.showMessageDialog(view, "Deleted.");
        }
    }

    public void refreshInventory() {
        view.refreshInventory(inventory.getAll());
    }

    public InventoryPanel getView() { return view; }
}
