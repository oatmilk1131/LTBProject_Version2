package LTBPaintCenter.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MonitoringPanel extends JPanel {
    private DefaultTableModel model;
    private JTable table;
    private JLabel lblSummary;

    public MonitoringPanel() {
        setLayout(new BorderLayout(8,8));

        model = new DefaultTableModel(new String[]{"Sale ID","Date","Items","Total"}, 0);
        table = new JTable(model);

        lblSummary = new JLabel("Sales: 0 | Revenue: ₱0.00");
        lblSummary.setFont(new Font("Segoe UI", Font.BOLD, 14));

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(lblSummary);
        add(bottom, BorderLayout.SOUTH);
    }

    // Helper methods for controller
    public void clearTable() {
        model.setRowCount(0);
    }

    public void addRow(Object[] rowData) {
        model.addRow(rowData);
    }

    public void updateSummary(int totalSales, double totalRevenue) {
        lblSummary.setText(String.format("Sales: %d | Revenue: ₱%.2f", totalSales, totalRevenue));
    }
}
