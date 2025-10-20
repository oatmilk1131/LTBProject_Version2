package LTBPaintCenter.controller;

import LTBPaintCenter.model.*;
import LTBPaintCenter.view.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;

public class InventoryController {
    private Inventory inventory;
    private InventoryPanel view;

    public InventoryController(Inventory inv) {
        this.inventory = inv;
        this.view = new InventoryPanel();
        attach();
        refreshTable();
    }

    public JPanel getView() { return view; }

    public void refreshTable() {
        DefaultTableModel m = view.model;
        int rows = m.getRowCount();
        for (int i = rows-1; i>=0; --i) m.removeRow(i);
        for (Product p : inventory.getAll()) {
            m.addRow(new Object[]{p.getId(), p.getName(), String.format("%.2f", p.getPrice()), p.getQuantity()});
        }
    }

    private void attach() {
        view.btnAdd.addActionListener(e -> addOrUpdate());
        view.btnDelete.addActionListener(e -> delete());
        view.table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int r = view.table.getSelectedRow();
                if (r >=0 ) {
                    view.tfId.setText(view.model.getValueAt(r,0).toString());
                    view.tfName.setText(view.model.getValueAt(r,1).toString());
                    view.tfPrice.setText(view.model.getValueAt(r,2).toString());
                    view.tfQty.setText(view.model.getValueAt(r,3).toString());
                }
            }
        });
    }

    private void addOrUpdate() {
        String id = view.tfId.getText().trim();
        String name = view.tfName.getText().trim();
        double price; int qty;
        try { price = Double.parseDouble(view.tfPrice.getText().trim());
            qty = Integer.parseInt(view.tfQty.getText().trim());
        } catch (Exception ex) { JOptionPane.showMessageDialog(view,"Invalid input"); return; }

        Product existing = inventory.getProduct(id);
        if (existing == null) inventory.addProduct(new Product(id,name,price,qty));
        else { existing.setName(name); existing.setPrice(price); existing.setQuantity(qty); }

        refreshTable();
    }

    private void delete() {
        String id = view.tfId.getText().trim();
        if (id.isEmpty()) { JOptionPane.showMessageDialog(view,"Select an item first"); return; }
        inventory.removeProduct(id);
        refreshTable();
    }
}
