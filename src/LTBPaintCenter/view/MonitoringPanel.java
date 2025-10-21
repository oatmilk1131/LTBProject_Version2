package LTBPaintCenter.view;

import LTBPaintCenter.model.Sale;
import LTBPaintCenter.model.SaleItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

/**
 * Monitoring panel for viewing sales records, applying filters,
 * and visualizing revenue breakdown by brand or type.
 */
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

    public MonitoringPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        initTopFilters();
        initTable();
        initSummaryBar();
    }

    // FILTER BAR
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

    // SALES TABLE
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
                if (!isSelected) c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Recorded Sales"));
        add(scroll, BorderLayout.CENTER);
    }

    // SUMMARY SECTION
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

        setupSummaryTextArea(taBrandSummary, "Revenue by Brand");
        setupSummaryTextArea(taTypeSummary, "Revenue by Type");

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

    private void setupSummaryTextArea(JTextArea area, String title) {
        area.setEditable(false);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        area.setBackground(new Color(248, 248, 248));
        area.setBorder(BorderFactory.createTitledBorder(title));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        add(area);
    }

    // PUBLIC
    public void refreshSales(Collection<Sale> sales) {
        tableModel.setRowCount(0);
        double totalRevenue = 0;
        int totalSales = 0;
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (Sale s : sales) {
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

    public void populateBrandFilter(Collection<String> brands) {
        cbFilterBrand.removeAllItems();
        cbFilterBrand.addItem("All Brands");
        for (String b : brands) cbFilterBrand.addItem(b);
    }

    public void updateBreakdown(String brandText, String typeText) {
        taBrandSummary.setText(brandText);
        taTypeSummary.setText(typeText);
    }

    // GETTERS
    public JButton getBtnApplyFilter() { return btnApplyFilter; }
    public JButton getBtnClearFilter() { return btnClearFilter; }
    public JComboBox<String> getCbFilterBrand() { return cbFilterBrand; }
    public String getFromDay()   { return (String) cbFromDay.getSelectedItem(); }
    public String getFromMonth() { return (String) cbFromMonth.getSelectedItem(); }
    public String getFromYear()  { return (String) cbFromYear.getSelectedItem(); }
    public String getToDay()     { return (String) cbToDay.getSelectedItem(); }
    public String getToMonth()   { return (String) cbToMonth.getSelectedItem(); }
    public String getToYear()    { return (String) cbToYear.getSelectedItem(); }
    public JComboBox<String> getCbFromDay()   { return cbFromDay; }
    public JComboBox<String> getCbFromMonth() { return cbFromMonth; }
    public JComboBox<String> getCbFromYear()  { return cbFromYear; }
    public JComboBox<String> getCbToDay()     { return cbToDay; }
    public JComboBox<String> getCbToMonth()   { return cbToMonth; }
    public JComboBox<String> getCbToYear()    { return cbToYear; }
    public BarChartPanel getBarChartPanel()   { return barChartPanel; }
    public JComboBox<String> getCbChartMode() { return cbChartMode; }
}
