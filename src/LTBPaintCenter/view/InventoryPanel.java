package LTBPaintCenter.view;

import LTBPaintCenter.model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Collection;

public class InventoryPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;

    private JTextField tfId, tfName, tfPrice, tfQty;
    private JButton btnAddOrUpdate, btnDelete;

    // Listener hooks
    private Runnable addOrUpdateListener;
    private Runnable deleteListener;

    public InventoryPanel() {
        setLayout(new BorderLayout(10, 10));

        // Form panel
        JPanel form = new JPanel();
        tfId = new JTextField(8);
        tfName = new JTextField(12);
        tfPrice = new JTextField(6);
        tfQty = new JTextField(4);
        btnAddOrUpdate = new JButton("Add/Update");
        btnDelete = new JButton("Delete");

        form.add(new JLabel("ID:")); form.add(tfId);
        form.add(new JLabel("Name:")); form.add(tfName);
        form.add(new JLabel("Price:")); form.add(tfPrice);
        form.add(new JLabel("Qty:")); form.add(tfQty);
        form.add(btnAddOrUpdate); form.add(btnDelete);

        add(form, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(new String[]{"ID", "Name", "Price", "Qty"}, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        attachListeners();
    }

    private void attachListeners() {
        btnAddOrUpdate.addActionListener(e -> {
            if (addOrUpdateListener != null) addOrUpdateListener.run();
        });
        btnDelete.addActionListener(e -> {
            if (deleteListener != null) deleteListener.run();
        });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    tfId.setText(model.getValueAt(row, 0).toString());
                    tfName.setText(model.getValueAt(row, 1).toString());
                    tfPrice.setText(model.getValueAt(row, 2).toString());
                    tfQty.setText(model.getValueAt(row, 3).toString());
                }
            }
        });
    }

    public void refreshInventory(Collection<Product> products) {
        model.setRowCount(0);
        for (Product p : products) {
            model.addRow(new Object[]{
                    p.getId(),
                    p.getName(),
                    String.format("%.2f", p.getPrice()),
                    p.getQuantity()
            });
        }
    }

    // Listener setters
    public void setAddOrUpdateListener(Runnable listener) {
        this.addOrUpdateListener = listener;
    }

    public void setDeleteListener(Runnable listener) {
        this.deleteListener = listener;
    }

    // Field getters for controller
    public JTextField getIdField() { return tfId; }
    public JTextField getNameField() { return tfName; }
    public JTextField getPriceField() { return tfPrice; }
    public JTextField getQtyField() { return tfQty; }

    public JTable getTable() { return table; }
}
