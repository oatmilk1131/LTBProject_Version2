package LTBPaintCenter.controller;

import LTBPaintCenter.model.Inventory;
import LTBPaintCenter.model.Product;
import LTBPaintCenter.view.InventoryPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.sun.tools.attach.VirtualMachine.attach;

public class InventoryController {
    private Inventory inventory;
    private InventoryPanel view;

    public InventoryController(Inventory inv) {
        this.inventory = inv; this.view = new InventoryPanel();
        attach();
        refreshTable();
    }

    public JPanel getView() { return view; }

    private void attach() {
        view.btnAdd.addActionListener(e -> addOrUpdate());
        view.btnDelete.addActionListener(e -> delete());

        view.table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int r = view.table.getSelectedRow();
                if (r >= 0) {
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
        try {
            price = Double.parseDouble(view.tfPrice.getText().trim());
            qty = Integer.parseInt(view.tfQty.getText().trim());
        } catch (Exception ex) { JOptionPane.showMessageDialog(view, "Invalid price/qty"); return; }
        Product existing = inventory.getProduct(id);
        if (existing == null) {
            Product p = new Product(id, name, price, qty);
            inventory.addProduct(p);
        } else {
            existing.setName(name); existing.setPrice(price); existing.setQuantity(qty);
        }
        refreshTable();
    }

    private void delete() {
        String id = view.tfId.getText().trim();
        if (id.isEmpty()) { JOptionPane.showMessageDialog(view, "Select an item first"); return; }
        inventory.removeProduct(id);
        refreshTable();
    }

    private void refreshTable() {
        DefaultTableModel m = view.model;
        int rows = m.getRowCount();
        for (int i = rows-1; i>=0; --i) m.removeRow(i);
        for (Product p : inventory.getAll()) {
            m.addRow(new Object[]{p.getId(), p.getName(), String.format("%.2f", p.getPrice()), p.getQuantity()});
        }
    }
}
