package LTBPaintCenter.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MonitoringPanel extends JPanel {
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Sale ID", "Date", "Items", "Total"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final JLabel lblSummary = new JLabel("Sales: 0 | Revenue: ₱0.00");

    public MonitoringPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setBackground(Color.WHITE);

        table.setRowHeight(25);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Sales Record"));
        add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        bottom.setBackground(new Color(245, 245, 245));
        bottom.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        bottom.add(lblSummary);
        add(bottom, BorderLayout.SOUTH);
    }

    public void clearTable() { tableModel.setRowCount(0); }

    public void addRow(Object[] row) { tableModel.addRow(row); }

    public void updateSummary(int salesCount, double revenue) {
        lblSummary.setText(String.format("Sales: %d | Revenue: ₱%.2f", salesCount, revenue));
    }
}
