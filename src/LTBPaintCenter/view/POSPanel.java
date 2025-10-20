package LTBPaintCenter.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class POSPanel extends JPanel {
    public JTextField tfProductId = new JTextField(10);
    public JTextField tfQty = new JTextField(4);
    public JButton btnAdd = new JButton("Add");
    public JButton btnCheckout = new JButton("Checkout");
    public DefaultTableModel tableModel = new DefaultTableModel(new String[]{"ID","Name","Price","Qty","Subtotal"}, 0);
    JTable table = new JTable(tableModel);
    public JLabel lblTotal = new JLabel("Total: ₱0.00");
    public JButton btnClear = new JButton("Clear");

    public POSPanel() {
        setLayout(new BorderLayout(8,8));
        JPanel top = new JPanel();
        top.add(new JLabel("Product ID:")); top.add(tfProductId);
        top.add(new JLabel("Qty:")); top.add(tfQty);
        top.add(btnAdd); top.add(btnClear); top.add(btnCheckout);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(lblTotal);
        add(bottom, BorderLayout.SOUTH);
    }
}