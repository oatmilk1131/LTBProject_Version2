package LTBPaintCenter.view;

import LTBPaintCenter.model.InventoryBatch;
import LTBPaintCenter.model.Sale;
import LTBPaintCenter.model.SaleItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;

/**
 * Monitoring panel for viewing sales records, applying filters,
 * and visualizing revenue breakdown by brand or type.
 * Now also includes stock and expiration alerts.
 */
public class MonitoringPanel extends JPanel {
    // Existing components
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Sale ID", "Date", "Items", "Total (₱)"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    private final JTable table = new JTable(tableModel);
    private final JLabel lblTotalSales = new JLabel("Total Sales: 0");
    private final JLabel lblRevenue = new JLabel("Total Revenue: ₱0.00");

    private final JComboBox<String> cbFilterBrand = new JComboBox<>(new String[]{"All Brands"});
    private final JComboBox<String> cbFromDay = new JComboBox<>();
    private final JComboBox<String> cbFromMonth = new JComboBox<>();
    private final JComboBox<String> cbFromYear = new JComboBox<>();
    private final JComboBox<String> cbToDay = new JComboBox<>();
    private final JComboBox<String> cbToMonth = new JComboBox<>();
    private final JComboBox<String> cbToYear = new JComboBox<>();

    private final JButton btnApplyFilter = new JButton("Apply");
    private final JButton btnClearFilter = new JButton("Clear");

    private final JTextArea taBrandSummary = new JTextArea();
    private final JTextArea taTypeSummary = new JTextArea();
    private final JComboBox<String> cbChartMode = new JComboBox<>(new String[]{"Brand Revenue", "Type Revenue"});
    private final BarChartPanel barChartPanel = new BarChartPanel();

    private List<Sale> currentSales;

    // 🟢 NEW: Alerts Panel
    private final JTextArea taAlerts = new JTextArea();

    public MonitoringPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        initTopFilters();
        initTable();
        initSummaryBar();
        initAlertsPanel(); // 🆕 Add alerts section
    }

    // FILTER BAR (unchanged)
    private void initTopFilters() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filters"));

        filterPanel.add(new JLabel("Brand:"));
        filterPanel.add(cbFilterBrand);

        filterPanel.add(new JLabel("Date From:"));
        addDateSelectors(filterPanel, cbFromDay, cbFromMonth, cbFromYear);

        filterPanel.add(new JLabel("To:"));
        addDateSelectors(filterPanel, cbToDay, cbToMonth, cbToYear);

        styleButton(btnApplyFilter, new Color(0, 120, 215), Color.WHITE);
        styleButton(btnClearFilter, new Color(108, 117, 125), Color.WHITE);

        filterPanel.add(btnApplyFilter);
        filterPanel.add(btnClearFilter);
        add(filterPanel, BorderLayout.NORTH);
    }

    private void styleButton(JButton b, Color bg, Color fg) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setPreferredSize(new Dimension(90, 26));
    }

    private void addDateSelectors(JPanel panel, JComboBox<String> cbDay, JComboBox<String> cbMonth, JComboBox<String> cbYear) {
        cbDay.addItem("");
        for (int i = 1; i <= 31; i++) cbDay.addItem(String.valueOf(i));
        cbDay.setPreferredSize(new Dimension(50, 25));
        panel.add(cbDay);

        cbMonth.addItem("");
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        for (String m : months) cbMonth.addItem(m);
        cbMonth.setPreferredSize(new Dimension(70, 25));
        panel.add(cbMonth);

        cbYear.addItem("");
        for (int y = 2020; y <= 2026; y++) cbYear.addItem(String.valueOf(y));
        cbYear.setPreferredSize(new Dimension(70, 25));
        panel.add(cbYear);

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        cbYear.setSelectedItem(String.valueOf(currentYear));
    }

    // SALES TABLE (unchanged except showSaleDetailsDialog)
    private void initTable() {
        table.setRowHeight(26);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Recorded Sales"));
        add(scroll, BorderLayout.CENTER);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0 && currentSales != null && row < currentSales.size()) {
                    Sale sale = currentSales.get(row);
                    showSaleDetailsDialog(sale);
                }
            }
        });
    }

    // SUMMARY SECTION (mostly unchanged)
    private void initSummaryBar() {
        JPanel summaryContainer = new JPanel();
        summaryContainer.setLayout(new BoxLayout(summaryContainer, BoxLayout.Y_AXIS));
        summaryContainer.setBackground(Color.WHITE);
        summaryContainer.setBorder(BorderFactory.createTitledBorder("Sales Summary"));

        JPanel topSummary = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 8));
        topSummary.setBackground(Color.WHITE);
        lblTotalSales.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRevenue.setFont(new Font("Segoe UI", Font.BOLD, 13));
        topSummary.add(lblTotalSales);
        topSummary.add(lblRevenue);
        summaryContainer.add(topSummary);

        JPanel summaries = new JPanel(new GridLayout(1, 2, 8, 8));
        summaries.setBackground(Color.WHITE);
        setupSummaryTextArea(taBrandSummary, "Revenue by Brand", summaries);
        setupSummaryTextArea(taTypeSummary, "Revenue by Type", summaries);
        summaryContainer.add(summaries);

        JPanel chartHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        chartHeader.setBackground(Color.WHITE);
        chartHeader.add(new JLabel("Chart Mode:"));
        chartHeader.add(cbChartMode);

        barChartPanel.setBorder(BorderFactory.createTitledBorder("Visual Breakdown"));

        summaryContainer.add(Box.createVerticalStrut(8));
        summaryContainer.add(chartHeader);
        summaryContainer.add(barChartPanel);

        add(summaryContainer, BorderLayout.SOUTH);
    }

    private void setupSummaryTextArea(JTextArea area, String title, JPanel parent) {
        area.setEditable(false);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        area.setBackground(new Color(248, 248, 248));
        area.setBorder(BorderFactory.createTitledBorder(title));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        parent.add(area);
    }

    // 🆕 ALERTS SECTION
    private void initAlertsPanel() {
        JPanel alertsContainer = new JPanel(new BorderLayout());
        alertsContainer.setBackground(Color.WHITE);
        alertsContainer.setBorder(BorderFactory.createTitledBorder("⚠️ Stock & Expiration Alerts"));
        alertsContainer.setPreferredSize(new Dimension(320, 0));

        taAlerts.setEditable(false);
        taAlerts.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        taAlerts.setBackground(new Color(255, 250, 240)); // Light beige
        taAlerts.setForeground(Color.DARK_GRAY);
        taAlerts.setLineWrap(true);
        taAlerts.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(taAlerts);
        alertsContainer.add(scroll, BorderLayout.CENTER);

        // Place alerts at EAST so it doesn't replace the SOUTH summary (which contains the bar chart)
        add(alertsContainer, BorderLayout.EAST);
    }

    // 🟢 NEW: Update alerts with inventory data
    public void updateAlerts(List<InventoryBatch> batches) {
        StringBuilder sb = new StringBuilder();
        LocalDate today = LocalDate.now();

        for (InventoryBatch b : batches) {
            LocalDate exp = b.getExpirationDate();
            int qty = b.getQuantity();

            if (exp != null) {
                long daysLeft = ChronoUnit.DAYS.between(today, exp);
                if (daysLeft <= 7 && daysLeft > 0) {
                    sb.append("🟡 ").append(b.getName()).append(" (").append(b.getBrand()).append(")")
                            .append(" — Expiring in ").append(daysLeft).append(" days\n");
                } else if (daysLeft <= 0) {
                    sb.append("🔴 ").append(b.getName()).append(" (").append(b.getBrand()).append(")")
                            .append(" — EXPIRED!\n");
                }
            }

            if (qty == 0) {
                sb.append("⚫ ").append(b.getName()).append(" (").append(b.getBrand()).append(")")
                        .append(" — OUT OF STOCK\n");
            } else if (qty <= 5) {
                sb.append("🟠 ").append(b.getName()).append(" (").append(b.getBrand()).append(")")
                        .append(" — Low stock: ").append(qty).append(" left\n");
            }
        }

        if (sb.length() == 0) {
            sb.append("✅ All products are healthy and in stock.");
        }

        taAlerts.setText(sb.toString());
    }

    // SALES SUMMARY METHODS (unchanged)
    public void refreshSales(Collection<Sale> sales) {
        tableModel.setRowCount(0);
        double totalRevenue = 0;
        int totalSales = 0;
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        currentSales = sales instanceof List ? (List<Sale>) sales : List.copyOf(sales);

        for (Sale s : currentSales) {
            totalSales++;
            totalRevenue += s.getTotal();

            StringBuilder itemSummary = new StringBuilder();
            for (SaleItem it : s.getItems()) {
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

    // GETTERS
    public void populateBrandFilter(Collection<String> brands) {
        cbFilterBrand.removeAllItems();
        cbFilterBrand.addItem("All Brands");
        for (String b : brands) cbFilterBrand.addItem(b);
    }

    public void updateBreakdown(String brandText, String typeText) {
        taBrandSummary.setText(brandText);
        taTypeSummary.setText(typeText);
    }

    public JButton getBtnApplyFilter() { return btnApplyFilter; }
    public JButton getBtnClearFilter() { return btnClearFilter; }
    public JComboBox<String> getCbFilterBrand() { return cbFilterBrand; }
    public BarChartPanel getBarChartPanel() { return barChartPanel; }
    public JComboBox<String> getCbChartMode() { return cbChartMode; }

    // Date selector getters used by MonitoringController
    public String getFromDay() {
        Object v = cbFromDay.getSelectedItem();
        return v == null ? "" : v.toString();
    }
    public String getFromMonth() {
        Object v = cbFromMonth.getSelectedItem();
        return v == null ? "" : v.toString();
    }
    public String getFromYear() {
        Object v = cbFromYear.getSelectedItem();
        return v == null ? "" : v.toString();
    }
    public String getToDay() {
        Object v = cbToDay.getSelectedItem();
        return v == null ? "" : v.toString();
    }
    public String getToMonth() {
        Object v = cbToMonth.getSelectedItem();
        return v == null ? "" : v.toString();
    }
    public String getToYear() {
        Object v = cbToYear.getSelectedItem();
        return v == null ? "" : v.toString();
    }

    private void showSaleDetailsDialog(Sale sale) {
        if (sale == null) return;

        StringBuilder details = new StringBuilder();
        details.append("Sale ID: ").append(sale.getId()).append("\n");
        details.append("Date: ").append(sale.getDate()).append("\n");
        details.append("Items:\n");

        for (SaleItem item : sale.getItems()) {
            details.append(" - ").append(item.getName())
                    .append(" (x").append(item.getQty())
                    .append("): ₱").append(String.format("%.2f", item.getSubtotal()))
                    .append("\n");
        }

        details.append("\nTotal: ₱").append(String.format("%.2f", sale.getTotal()));

        JOptionPane.showMessageDialog(
                this,
                new JTextArea(details.toString()),
                "Sale Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
    // 🆕 For updating alerts externally
    public JTextArea getTaAlerts() { return taAlerts; }
}
