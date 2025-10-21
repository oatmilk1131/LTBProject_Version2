package LTBPaintCenter.view;

import LTBPaintCenter.model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Collection;

public class InventoryPanel extends JPanel {
    private final JTextField tfId = new JTextField(8);
    private final JTextField tfName = new JTextField(14);
    private final JTextField tfPrice = new JTextField(6);
    private final JTextField tfQty = new JTextField(4);
    private final JButton btnAddUpdate = new JButton("Add / Update");
    private final JButton btnDelete = new JButton("Delete");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Price", "Qty"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    public InventoryPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setBackground(Color.WHITE);

        // Top form panel
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        form.setBackground(new Color(245, 245, 245));
        form.setBorder(BorderFactory.createTitledBorder("Product Details"));

        form.add(new JLabel("ID:")); form.add(tfId);
        form.add(new JLabel("Name:")); form.add(tfName);
        form.add(new JLabel("Price:")); form.add(tfPrice);
        form.add(new JLabel("Qty:")); form.add(tfQty);
        form.add(btnAddUpdate);
        form.add(btnDelete);

        // Table
        table.setRowHeight(25);
        table.setFillsViewportHeight(true);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Inventory List"));

        add(form, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    // -------------------------------
    // Public API
    // -------------------------------

    public void refreshInventory(Collection<Product> products) {
        tableModel.setRowCount(0);
        for (Product p : products) {
            tableModel.addRow(new Object[]{
                    p.getId(),
                    p.getName(),
                    String.format("%.2f", p.getPrice()),
                    p.getQuantity()
            });
        }
    }

    public JTextField getTfId() { return tfId; }
    public JTextField getTfName() { return tfName; }
    public JTextField getTfPrice() { return tfPrice; }
    public JTextField getTfQty() { return tfQty; }
    public JButton getBtnAddUpdate() { return btnAddUpdate; }
    public JButton getBtnDelete() { return btnDelete; }
    public JTable getTable() { return table; }
}
