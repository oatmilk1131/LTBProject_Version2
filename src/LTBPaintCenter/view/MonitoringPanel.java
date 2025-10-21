package LTBPaintCenter.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MonitoringPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel lblSummary;

    public MonitoringPanel() {
        setLayout(new BorderLayout(8,8));

        tableModel = new DefaultTableModel(new String[]{"Sale ID","Date","Items","Total"},0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        lblSummary = new JLabel("Sales: 0 | Revenue: ₱0.00");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(lblSummary);
        add(bottom, BorderLayout.SOUTH);
    }

    public void clearTable() { tableModel.setRowCount(0); }

    public void addRow(Object[] row) { tableModel.addRow(row); }

    public void updateSummary(int salesCount, double revenue) {
        lblSummary.setText(String.format("Sales: %d | Revenue: ₱%.2f", salesCount, revenue));
    }

    public JTable getTable() { return table; }
}
