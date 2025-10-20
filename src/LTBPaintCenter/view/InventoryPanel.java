package LTBPaintCenter.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class InventoryPanel extends JPanel {
    public DefaultTableModel model = new DefaultTableModel(new String[]{"ID","Name","Price","Qty"}, 0);
    public JTable table = new JTable(model);
    public JTextField tfId = new JTextField(8);
    public JTextField tfName = new JTextField(12);
    public JTextField tfPrice = new JTextField(6);
    public JTextField tfQty = new JTextField(4);
    public JButton btnAdd = new JButton("Add/Update");
    public JButton btnDelete = new JButton("Delete");

    public InventoryPanel() {
        setLayout(new BorderLayout(8,8));
        JPanel form = new JPanel();
        form.add(new JLabel("ID:")); form.add(tfId);
        form.add(new JLabel("Name:")); form.add(tfName);
        form.add(new JLabel("Price:")); form.add(tfPrice);
        form.add(new JLabel("Qty:")); form.add(tfQty);
        form.add(btnAdd); form.add(btnDelete);
        add(form, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);
    }
}
