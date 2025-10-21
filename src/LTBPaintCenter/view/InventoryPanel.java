package LTBPaintCenter.view;

import LTBPaintCenter.model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Collection;
import java.util.Vector;

public class InventoryPanel extends JPanel {
    private final JTextField tfId = new JTextField(8);
    private final JTextField tfName = new JTextField(12);
    private final JTextField tfPrice = new JTextField(6);
    private final JTextField tfQty = new JTextField(4);
    private final JComboBox<String> cbBrand = new JComboBox<>();
    private final JComboBox<String> cbColor = new JComboBox<>();
    private final JComboBox<String> cbType = new JComboBox<>();
    private final JTextField tfSearch = new JTextField(10);

    private final JButton btnAddUpdate = new JButton("Add / Update");
    private final JButton btnDelete = new JButton("Delete");
    private final JButton btnClear = new JButton("Clear Fields");

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Name", "Brand", "Color", "Type", "Price", "Qty"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public InventoryPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        initTopBar();
        initTableArea();
    }

    // --------------------------------------
    // Top Panel (Form + Filter Section)
    // --------------------------------------
    private void initTopBar() {
        JPanel topPanel = new JPanel(new BorderLayout(6, 6));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createTitledBorder("Product Details"));

        // ===== Row 1: Basic Info =====
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row1.setBackground(Color.WHITE);

        row1.add(new JLabel("ID:"));
        row1.add(tfId);
        row1.add(new JLabel("Name:"));
        row1.add(tfName);
        row1.add(new JLabel("Price:"));
        row1.add(tfPrice);
        row1.add(new JLabel("Qty:"));
        row1.add(tfQty);

        // ===== Row 2: Categorization =====
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row2.setBackground(Color.WHITE);

        row2.add(new JLabel("Brand:"));
        cbBrand.setPreferredSize(new Dimension(130, 25));
        row2.add(cbBrand);

        row2.add(new JLabel("Color:"));
        cbColor.setPreferredSize(new Dimension(130, 25));
        row2.add(cbColor);

        row2.add(new JLabel("Type:"));
        cbType.setPreferredSize(new Dimension(130, 25));
        row2.add(cbType);

        // Wrap both rows in a form section
        JPanel formSection = new JPanel(new GridLayout(2, 1));
        formSection.setBackground(Color.WHITE);
        formSection.add(row1);
        formSection.add(row2);
        topPanel.add(formSection, BorderLayout.CENTER);

        // ===== Buttons =====
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        btnPanel.setBackground(Color.WHITE);

        styleButton(btnAddUpdate, new Color(0, 120, 215), Color.WHITE);
        styleButton(btnDelete, new Color(220, 53, 69), Color.WHITE);
        styleButton(btnClear, new Color(108, 117, 125), Color.WHITE);

        btnPanel.add(btnAddUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);
        btnClear.addActionListener(e -> clearFormFields()); // fix for clear button

        topPanel.add(btnPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
    }

    private void styleButton(JButton b, Color bg, Color fg) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setPreferredSize(new Dimension(120, 28));
    }

    // --------------------------------------
    // Table Area
    // --------------------------------------
    private void initTableArea() {
        JPanel midPanel = new JPanel(new BorderLayout(6, 6));
        midPanel.setBackground(Color.WHITE);
        midPanel.setBorder(BorderFactory.createTitledBorder("Inventory List"));

        // --- Search bar ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        searchPanel.setBackground(Color.WHITE);

        JLabel lblSearch = new JLabel("Search:");
        JButton btnSearch = new JButton("Find");
        styleButton(btnSearch, new Color(0, 120, 215), Color.WHITE);

        searchPanel.add(lblSearch);
        searchPanel.add(tfSearch);
        searchPanel.add(btnSearch);

        midPanel.add(searchPanel, BorderLayout.NORTH);

        // --- Table ---
        table.setRowHeight(26);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(230, 230, 230));
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        // Single-click row to load data into form (auto combo selection)
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    // Convert to model index if table sorting is enabled
                    int modelRow = table.convertRowIndexToModel(row);

                    // Load basic info
                    tfId.setText(model.getValueAt(modelRow, 0).toString());
                    tfName.setText(model.getValueAt(modelRow, 1).toString());
                    tfPrice.setText(model.getValueAt(modelRow, 5).toString());
                    tfQty.setText(model.getValueAt(modelRow, 6).toString());

                    // Auto-select combo boxes safely
                    String brandVal = model.getValueAt(modelRow, 2).toString();
                    String colorVal = model.getValueAt(modelRow, 3).toString();
                    String typeVal  = model.getValueAt(modelRow, 4).toString();

                    selectComboBoxValue(cbBrand, brandVal);
                    selectComboBoxValue(cbColor, colorVal);
                    selectComboBoxValue(cbType, typeVal);
                }
            }
        });

        midPanel.add(scroll, BorderLayout.CENTER);

        add(midPanel, BorderLayout.CENTER);
    }
    // --------------------------------------
    // Public API
    // --------------------------------------
    public void refreshInventory(Collection<Product> products) {
        model.setRowCount(0);
        for (Product p : products) {
            model.addRow(new Object[]{
                    p.getId(),
                    p.getName(),
                    p.getBrand(),
                    p.getColor(),
                    p.getType(),
                    String.format("%.2f", p.getPrice()),
                    p.getQuantity()
            });
        }

        // repopulate dropdowns
        updateCombos(products);
    }

    private void updateCombos(Collection<Product> products) {
        cbBrand.removeAllItems();
        cbColor.removeAllItems();
        cbType.removeAllItems();

        cbBrand.addItem("Select Brand");
        cbColor.addItem("Select Color");
        cbType.addItem("Select Type");

        java.util.Set<String> brands = new java.util.TreeSet<>();
        java.util.Set<String> colors = new java.util.TreeSet<>();
        java.util.Set<String> types = new java.util.TreeSet<>();

        for (Product p : products) {
            if (!p.getBrand().isEmpty()) brands.add(p.getBrand());
            if (!p.getColor().isEmpty()) colors.add(p.getColor());
            if (!p.getType().isEmpty()) types.add(p.getType());
        }

        for (String s : brands) cbBrand.addItem(s);
        for (String s : colors) cbColor.addItem(s);
        for (String s : types) cbType.addItem(s);
    }

    private void clearFormFields() {
        tfId.setText("");
        tfName.setText("");
        tfPrice.setText("");
        tfQty.setText("");
        tfSearch.setText("");

        // Reset dropdowns
        if (cbBrand.getItemCount() > 0) cbBrand.setSelectedIndex(0);
        if (cbColor.getItemCount() > 0) cbColor.setSelectedIndex(0);
        if (cbType.getItemCount() > 0) cbType.setSelectedIndex(0);

        // Clear table selection
        table.clearSelection();
    }

    private void selectComboBoxValue(JComboBox<String> comboBox, String value) {
        if (value == null || value.isBlank()) return;
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            String item = comboBox.getItemAt(i);
            if (item.equalsIgnoreCase(value)) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    // Getters for controller
    public JTextField getTfId() { return tfId; }
    public JTextField getTfName() { return tfName; }
    public JTextField getTfPrice() { return tfPrice; }
    public JTextField getTfQty() { return tfQty; }
    public JComboBox<String> getCbBrand() { return cbBrand; }
    public JComboBox<String> getCbColor() { return cbColor; }
    public JComboBox<String> getCbType() { return cbType; }
    public JTextField getTfSearch() { return tfSearch; }

    public JButton getBtnAddUpdate() { return btnAddUpdate; }
    public JButton getBtnDelete() { return btnDelete; }
    public JButton getBtnClear() { return btnClear; }

    public JTable getTable() { return table; }
}
