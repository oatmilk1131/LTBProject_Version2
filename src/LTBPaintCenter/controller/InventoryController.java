package LTBPaintCenter.controller;

import LTBPaintCenter.model.Inventory;
import LTBPaintCenter.model.Product;
import LTBPaintCenter.view.InventoryPanel;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class InventoryController {
    private Inventory inventory;
    private InventoryPanel view;

    public InventoryController(Inventory inventory) {
        this.inventory = inventory;
        this.view = new InventoryPanel();

        attachListeners();
        refreshInventory();
    }

    private void attachListeners() {
        view.getBtnAddOrUpdate().addActionListener(e -> addOrUpdate());
        view.getBtnDelete().addActionListener(e -> delete());

        view.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int r = view.getTable().getSelectedRow();
                if (r >= 0) {
                    view.getTfId().setText(view.getTable().getValueAt(r,0).toString());
                    view.getTfName().setText(view.getTable().getValueAt(r,1).toString());
                    view.getTfPrice().setText(view.getTable().getValueAt(r,2).toString());
                    view.getTfQty().setText(view.getTable().getValueAt(r,3).toString());
                }
            }
        });
    }

    private void addOrUpdate() {
        String id = view.getTfId().getText().trim();
        String name = view.getTfName().getText().trim();
        double price; int qty;

        try {
            price = Double.parseDouble(view.getTfPrice().getText().trim());
            qty = Integer.parseInt(view.getTfQty().getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view,"Invalid price/quantity");
            return;
        }

        Product p = inventory.getProduct(id);
        if (p == null) {
            inventory.addProduct(new Product(id, name, price, qty));
        } else {
            p.setName(name);
            p.setPrice(price);
            p.setQuantity(qty);
        }

        refreshInventory();
    }

    private void delete() {
        String id = view.getTfId().getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(view,"Select a product first");
            return;
        }
        inventory.removeProduct(id);
        refreshInventory();
    }

    public void refreshInventory() {
        view.refreshInventory(inventory.getAll());
    }

    public InventoryPanel getView() { return view; }
}
