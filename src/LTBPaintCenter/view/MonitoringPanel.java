package LTBPaintCenter.view;

import LTBPaintCenter.model.Sale;
import LTBPaintCenter.model.SaleItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;

public class MonitoringPanel extends JPanel {
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Sale ID", "Date", "Items", "Total (₱)"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    private final JLabel lblTotalSales = new JLabel("Total Sales: 0");
    private final JLabel lblRevenue = new JLabel("Total Revenue: ₱0.00");

    private final JComboBox<String> cbFilterBrand = new JComboBox<>(new String[]{"All Brands"});
    private final JTextField tfDateFrom = new JTextField(10);
    private final JTextField tfDateTo = new JTextField(10);
    private final JButton btnApplyFilter = new JButton("Apply");
    private final JButton btnClearFilter = new JButton("Clear");


    public MonitoringPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        initTopFilters();
        initTable();
        initSummaryBar();
    }

    // ------------------------------------------
    // FILTER BAR
    // ------------------------------------------
    private void initTopFilters() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filters"));

        filterPanel.add(new JLabel("Brand:"));
        filterPanel.add(cbFilterBrand);
        filterPanel.add(new JLabel("Date From (yyyy-MM-dd):"));
        filterPanel.add(tfDateFrom);
        filterPanel.add(new JLabel("To:"));
        filterPanel.add(tfDateTo);
        filterPanel.add(btnApplyFilter);


        JButton btnClearFilter = new JButton("Clear");
        styleButton(btnClearFilter, new Color(108, 117, 125), Color.WHITE);
        filterPanel.add(btnClearFilter);

        styleButton(btnApplyFilter, new Color(0, 120, 215), Color.WHITE);


        add(filterPanel, BorderLayout.NORTH);
    }

    private void styleButton(JButton b, Color bg, Color fg) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setPreferredSize(new Dimension(90, 26));
    }

    // ------------------------------------------
    // SALES TABLE
    // ------------------------------------------
    private void initTable() {
        table.setRowHeight(26);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Zebra striping
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected)
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Recorded Sales"));
        add(scroll, BorderLayout.CENTER);
    }

    // ------------------------------------------
    // SUMMARY BAR
    // ------------------------------------------
    private void initSummaryBar() {
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 8));
        summaryPanel.setBackground(Color.WHITE);
        lblTotalSales.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRevenue.setFont(new Font("Segoe UI", Font.BOLD, 13));

        summaryPanel.add(lblTotalSales);
        summaryPanel.add(lblRevenue);

        add(summaryPanel, BorderLayout.SOUTH);
    }

    // ------------------------------------------
    // PUBLIC METHODS
    // ------------------------------------------
    public void refreshSales(Collection<Sale> sales) {
        tableModel.setRowCount(0);
        double totalRevenue = 0;
        int totalSales = 0;

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (Sale s : sales) {
            totalSales++;
            totalRevenue += s.getTotal();

            StringBuilder itemSummary = new StringBuilder();
            List<SaleItem> items = s.getItems();
            for (SaleItem it : items) {
                itemSummary.append(it.getName()).append(" (x").append(it.getQty()).append("), ");
            }
            if (itemSummary.length() > 2) itemSummary.setLength(itemSummary.length() - 2);

            tableModel.addRow(new Object[]{
                    s.getId(),
                    fmt.format(s.getDate()),
                    itemSummary.toString(),
                    String.format("%.2f", s.getTotal())
            });
        }

        lblTotalSales.setText("Total Sales: " + totalSales);
        lblRevenue.setText(String.format("Total Revenue: ₱%.2f", totalRevenue));
    }

    // Future: use this to update brand dropdown dynamically
    public void populateBrandFilter(Collection<String> brands) {
        cbFilterBrand.removeAllItems();
        cbFilterBrand.addItem("All Brands");
        for (String b : brands) cbFilterBrand.addItem(b);
    }

    public JButton getBtnApplyFilter() { return btnApplyFilter; }
    public JComboBox<String> getCbFilterBrand() { return cbFilterBrand; }
    public JTextField getTfDateFrom() { return tfDateFrom; }
    public JTextField getTfDateTo() { return tfDateTo; }
    public JButton getBtnClearFilter() { return btnClearFilter; }
}
