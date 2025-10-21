package LTBPaintCenter.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Collection;
import LTBPaintCenter.model.Product;

public class InventoryPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField tfId, tfName, tfPrice, tfQty;
    private JButton btnAddOrUpdate, btnDelete;

    public InventoryPanel() {
        setLayout(new BorderLayout(8,8));

        // Table
        tableModel = new DefaultTableModel(new String[]{"ID","Name","Price","Qty"},0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Form
        JPanel form = new JPanel();
        tfId = new JTextField(8);
        tfName = new JTextField(12);
        tfPrice = new JTextField(6);
        tfQty = new JTextField(4);
        btnAddOrUpdate = new JButton("Add / Update");
        btnDelete = new JButton("Delete");

        form.add(new JLabel("ID:")); form.add(tfId);
        form.add(new JLabel("Name:")); form.add(tfName);
        form.add(new JLabel("Price:")); form.add(tfPrice);
        form.add(new JLabel("Qty:")); form.add(tfQty);
        form.add(btnAddOrUpdate); form.add(btnDelete);

        add(form, BorderLayout.NORTH);
    }

    public void refreshInventory(Collection<Product> products) {
        tableModel.setRowCount(0);
        for (Product p : products) {
            tableModel.addRow(new Object[]{p.getId(), p.getName(), String.format("%.2f",p.getPrice()), p.getQuantity()});
        }
    }

    public JTable getTable() { return table; }
    public JTextField getTfId() { return tfId; }
    public JTextField getTfName() { return tfName; }
    public JTextField getTfPrice() { return tfPrice; }
    public JTextField getTfQty() { return tfQty; }
    public JButton getBtnAddOrUpdate() { return btnAddOrUpdate; }
    public JButton getBtnDelete() { return btnDelete; }
}
