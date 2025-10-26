package LTBPaintCenter.view;

import LTBPaintCenter.controller.InventoryController;
import LTBPaintCenter.model.InventoryBatch;
import LTBPaintCenter.model.Global;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InventoryPanel extends JPanel {
    private final InventoryController controller;
    private final DefaultTableModel tableModel;
    private final JTable table;

    // user-typed product code
    private final JTextField txtCode = new JTextField();
    private final JTextField txtName = new JTextField();
    private final JComboBox<String> cbBrand = new JComboBox<>();
    private final JComboBox<String> cbColor = new JComboBox<>();
    private final JComboBox<String> cbType = new JComboBox<>();
    private final JTextField txtPrice = new JTextField();
    private final JTextField txtQty = new JTextField();
    private final JSpinner spDateImported = new JSpinner(new SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.DAY_OF_MONTH));
    private final JSpinner spExpiration = new JSpinner(new SpinnerDateModel());
    private final JCheckBox chkNoExpiration = new JCheckBox("No Expiration");

    // Filters & sorting
    private final JTextField txtSearch = new JTextField();
    private final JComboBox<String> cbFilterBrand = new JComboBox<>();
    private final JComboBox<String> cbFilterColor = new JComboBox<>();
    private final JComboBox<String> cbSort = new JComboBox<>(new String[]{"A–Z (Name)", "Price Low–High", "Price High–Low"});
    private javax.swing.table.TableRowSorter<DefaultTableModel> rowSorter;

    private final JButton btnAdd = new JButton("Add Batch");
    private final JButton btnUpdate = new JButton("Update");
    private final JButton btnDelete = new JButton("Delete");
    private final JButton btnRefresh = new JButton("Refresh");

    public InventoryPanel(InventoryController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);

        // ─────────────────────────────
        // Table setup
        // ─────────────────────────────
        String[] columns = {
                "ID", "Product ID", "Name", "Brand", "Color", "Type", "Price", "Qty",
                "Date Imported", "Expiration Date", "Status"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Hide numeric ID column from view while keeping it in the model for operations
        table.removeColumn(table.getColumnModel().getColumn(0));

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Populate form fields when selecting a row
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) return;
            int row = table.convertRowIndexToModel(viewRow);
            try {
                txtCode.setText(String.valueOf(tableModel.getValueAt(row, 1)));
                txtName.setText(String.valueOf(tableModel.getValueAt(row, 2)));
                String brand = String.valueOf(tableModel.getValueAt(row, 3));
                String color = String.valueOf(tableModel.getValueAt(row, 4));
                String type = String.valueOf(tableModel.getValueAt(row, 5));
                cbBrand.setSelectedItem(brand);
                cbColor.setSelectedItem(color);
                cbType.setSelectedItem(type);
                txtPrice.setText(String.valueOf(tableModel.getValueAt(row, 6)));
                txtQty.setText(String.valueOf(tableModel.getValueAt(row, 7)));

                java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String impStr = String.valueOf(tableModel.getValueAt(row, 8));
                if (impStr != null && !impStr.isBlank()) {
                    java.time.LocalDate ld = java.time.LocalDate.parse(impStr, df);
                    java.util.Date d = java.util.Date.from(ld.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
                    spDateImported.setValue(d);
                }

                String expStr = String.valueOf(tableModel.getValueAt(row, 9));
                if (expStr != null && !expStr.isBlank()) {
                    chkNoExpiration.setSelected(false);
                    spExpiration.setEnabled(true);
                    java.time.LocalDate ld = java.time.LocalDate.parse(expStr, df);
                    java.util.Date d = java.util.Date.from(ld.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
                    spExpiration.setValue(d);
                } else {
                    chkNoExpiration.setSelected(true);
                    spExpiration.setEnabled(false);
                }
            } catch (Exception ignored) {}
        });

        // ─────────────────────────────
        // Form panel
        // ─────────────────────────────
        JPanel formPanel = new JPanel(new GridLayout(3, 6, 8, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder("Product Batch Details"));
        formPanel.setBackground(Color.WHITE);

        cbBrand.setEditable(true);
        cbColor.setEditable(true);
        cbType.setEditable(true);
        ((JSpinner.DefaultEditor) spDateImported.getEditor()).getTextField().setEditable(false);
        spDateImported.setEditor(new JSpinner.DateEditor(spDateImported, "yyyy-MM-dd"));
        spExpiration.setEditor(new JSpinner.DateEditor(spExpiration, "yyyy-MM-dd"));

        // Row 1: Product ID, Name, Brand
        formPanel.add(new JLabel("Product ID:"));
        formPanel.add(txtCode);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(txtName);
        formPanel.add(new JLabel("Brand:"));
        formPanel.add(cbBrand);

        // Row 2: Color, Type, Price
        formPanel.add(new JLabel("Color:"));
        formPanel.add(cbColor);
        formPanel.add(new JLabel("Type:"));
        formPanel.add(cbType);
        formPanel.add(new JLabel("Price:"));
        formPanel.add(txtPrice);

        // Row 3: Quantity, Date Imported, Expiration
        formPanel.add(new JLabel("Quantity:"));
        formPanel.add(txtQty);
        formPanel.add(new JLabel("Date Imported:"));
        formPanel.add(spDateImported);
        formPanel.add(new JLabel("Expiration Date:"));
        JPanel expPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        expPanel.setBackground(Color.WHITE);
        expPanel.add(spExpiration);
        expPanel.add(chkNoExpiration);
        formPanel.add(expPanel);

        chkNoExpiration.addActionListener(e -> spExpiration.setEnabled(!chkNoExpiration.isSelected()));

        // Top container: form + filter/sort bar
        JPanel northContainer = new JPanel(new BorderLayout(8, 8));
        northContainer.setBackground(Color.WHITE);
        northContainer.add(formPanel, BorderLayout.NORTH);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterBar.setBackground(Color.WHITE);
        filterBar.setBorder(BorderFactory.createTitledBorder("Search, Filter, and Sort"));

        filterBar.add(new JLabel("Search:"));
        txtSearch.setColumns(16);
        filterBar.add(txtSearch);

        filterBar.add(new JLabel("Brand:"));
        cbFilterBrand.addItem("All Brands");
        filterBar.add(cbFilterBrand);

        filterBar.add(new JLabel("Color:"));
        cbFilterColor.addItem("All Colors");
        filterBar.add(cbFilterColor);

        filterBar.add(new JLabel("Sort:"));
        filterBar.add(cbSort);

        northContainer.add(filterBar, BorderLayout.SOUTH);
        add(northContainer, BorderLayout.NORTH);

        // ─────────────────────────────
        // Buttons
        // ─────────────────────────────
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnRefresh);
        add(buttonPanel, BorderLayout.SOUTH);

        // ─────────────────────────────
        // Button actions
        // ─────────────────────────────
        btnAdd.addActionListener(e -> handleAdd());
        btnUpdate.addActionListener(e -> handleUpdate());
        btnDelete.addActionListener(e -> handleDelete());
        btnRefresh.addActionListener(e -> refreshTable());

        // Initialize sorting/filtering and row highlighting
        setupFilters();
        refreshTable();
    }

    // ─────────────────────────────
    // Event handlers
    // ─────────────────────────────
    private void handleAdd() {
        try {
            String name = txtName.getText().trim();
            String brand = (cbBrand.getEditor().getItem() != null) ? cbBrand.getEditor().getItem().toString().trim() : "";
            String color = (cbColor.getEditor().getItem() != null) ? cbColor.getEditor().getItem().toString().trim() : "";
            String type = (cbType.getEditor().getItem() != null) ? cbType.getEditor().getItem().toString().trim() : "";
            double price = Double.parseDouble(txtPrice.getText().trim());
            int qty = Integer.parseInt(txtQty.getText().trim());
            java.util.Date impDate = (java.util.Date) spDateImported.getValue();
            LocalDate imported = impDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            LocalDate expiration = null;
            if (!chkNoExpiration.isSelected()) {
                java.util.Date expDate = (java.util.Date) spExpiration.getValue();
                expiration = expDate == null ? null : expDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            }

            String code = txtCode.getText().trim();
            boolean added = controller.addBatch(code, name, brand, color, type, price, qty, imported, expiration);
            if (added) {
                if (brand != null && !brand.isBlank()) cbBrand.addItem(brand);
                if (color != null && !color.isBlank()) cbColor.addItem(color);
                if (type != null && !type.isBlank()) cbType.addItem(type);
                JOptionPane.showMessageDialog(this, "Batch added successfully!");
                refreshTable();
                // Notify other modules
                if (Global.posController != null) Global.posController.refreshPOS();
                if (Global.monitoringController != null) Global.monitoringController.refresh();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add batch.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a row to update.");
            return;
        }
        int row = table.convertRowIndexToModel(viewRow);

        try {
            int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());

            String name = txtName.getText().trim();
            String brand = (cbBrand.getEditor().getItem() != null) ? cbBrand.getEditor().getItem().toString().trim() : "";
            String color = (cbColor.getEditor().getItem() != null) ? cbColor.getEditor().getItem().toString().trim() : "";
            String type = (cbType.getEditor().getItem() != null) ? cbType.getEditor().getItem().toString().trim() : "";

            double price = Double.parseDouble(txtPrice.getText().trim());
            int qty = Integer.parseInt(txtQty.getText().trim());

            java.util.Date impDate = (java.util.Date) spDateImported.getValue();
            LocalDate imported = impDate == null ? null : impDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

            LocalDate expiration = null;
            if (!chkNoExpiration.isSelected()) {
                java.util.Date expDate = (java.util.Date) spExpiration.getValue();
                expiration = expDate == null ? null : expDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            }

            // status will be recomputed in controller.updateBatch
            String productCode = txtCode.getText().trim();
            InventoryBatch batch = new InventoryBatch(id, productCode, name, brand, color, type, price, qty, imported, expiration, "");
            boolean updated = controller.updateBatch(batch);
            if (updated) {
                JOptionPane.showMessageDialog(this, "Batch updated!");
                refreshTable();
                if (Global.posController != null) Global.posController.refreshPOS();
                if (Global.monitoringController != null) Global.monitoringController.refresh();
            } else {
                JOptionPane.showMessageDialog(this, "Update failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a batch to delete.");
            return;
        }
        int row = table.convertRowIndexToModel(viewRow);

        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            controller.deleteBatch(id);
            refreshTable();
            if (Global.posController != null) Global.posController.refreshPOS();
            if (Global.monitoringController != null) Global.monitoringController.refresh();
        }
    }

    // ─────────────────────────────
    // Refresh table
    // ─────────────────────────────
    public void refreshTable() {
        tableModel.setRowCount(0);
        List<InventoryBatch> batches = controller.getAllBatches();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Populate combos
        java.util.Set<String> brands = new java.util.TreeSet<>();
        java.util.Set<String> colors = new java.util.TreeSet<>();
        java.util.Set<String> types = new java.util.TreeSet<>();

        for (InventoryBatch b : batches) {
            brands.add(b.getBrand() == null ? "" : b.getBrand());
            colors.add(b.getColor() == null ? "" : b.getColor());
            types.add(b.getType() == null ? "" : b.getType());

            String status = computeDisplayStatus(b);
            tableModel.addRow(new Object[]{
                    b.getId(), b.getProductCode(), b.getName(), b.getBrand(), b.getColor(), b.getType(),
                    String.format("%.2f", b.getPrice()), b.getQuantity(),
                    b.getDateImported() != null ? b.getDateImported().format(df) : "",
                    b.getExpirationDate() != null ? b.getExpirationDate().format(df) : "",
                    status
            });
        }

        // Update filter combos
        cbFilterBrand.removeAllItems();
        cbFilterBrand.addItem("All Brands");
        for (String s : brands) if (s != null && !s.isBlank()) cbFilterBrand.addItem(s);

        cbFilterColor.removeAllItems();
        cbFilterColor.addItem("All Colors");
        for (String s : colors) if (s != null && !s.isBlank()) cbFilterColor.addItem(s);

        // Update form combos (keep existing items but ensure base set)
        cbBrand.removeAllItems(); for (String s : brands) if (s != null && !s.isBlank()) cbBrand.addItem(s);
        cbColor.removeAllItems(); for (String s : colors) if (s != null && !s.isBlank()) cbColor.addItem(s);
        cbType.removeAllItems(); for (String s : types) if (s != null && !s.isBlank()) cbType.addItem(s);

        // Re-apply filters if sorter exists
        if (rowSorter != null) applyFilters();
    }

    private String computeDisplayStatus(InventoryBatch b) {
        java.time.LocalDate today = java.time.LocalDate.now();
        boolean expired = false;
        boolean expSoon = false;

        if (b.getExpirationDate() != null) {
            java.time.LocalDate exp = b.getExpirationDate();
            if (!exp.isAfter(today)) {
                expired = true; // exp <= today → expired
            } else if (!exp.isAfter(today.plusDays(7))) {
                expSoon = true; // within next 7 days
            }
        }

        boolean out = b.getQuantity() <= 0;
        boolean low = !out && b.getQuantity() <= 5;

        StringBuilder sb = new StringBuilder();
        if (expired) sb.append("Expired");
        else if (expSoon) sb.append("Expiring Soon");

        if (out) {
            if (sb.length() > 0) sb.append("; ");
            sb.append("Out of Stock");
        } else if (low) {
            if (sb.length() > 0) sb.append("; ");
            sb.append("Low Stock");
        }

        return sb.toString();
    }

    private void setupFilters() {
        rowSorter = new javax.swing.table.TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        // Numeric comparator for price (column 6 after adding Code column)
        rowSorter.setComparator(6, (o1, o2) -> {
            try {
                double d1 = Double.parseDouble(o1.toString());
                double d2 = Double.parseDouble(o2.toString());
                return Double.compare(d1, d2);
            } catch (Exception e) { return 0; }
        });

        // Search and filter listeners
        javax.swing.event.DocumentListener dl = new javax.swing.event.DocumentListener() {
            private void changed() { applyFilters(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { changed(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { changed(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { changed(); }
        };
        txtSearch.getDocument().addDocumentListener(dl);
        cbFilterBrand.addActionListener(e -> applyFilters());
        cbFilterColor.addActionListener(e -> applyFilters());
        cbSort.addActionListener(e -> applySort());

        // Row renderer to highlight status when not selected; preserve selection highlight when selected
        javax.swing.table.TableCellRenderer defaultRenderer = table.getDefaultRenderer(Object.class);
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                java.awt.Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                if (isSelected) {
                    c.setBackground(tbl.getSelectionBackground());
                    c.setForeground(tbl.getSelectionForeground());
                    return c;
                }
                c.setForeground(Color.BLACK);
                c.setBackground(Color.WHITE);
                int modelRow = tbl.convertRowIndexToModel(row);
                String status = String.valueOf(tableModel.getValueAt(modelRow, 10));
                if (status != null && !status.isBlank()) {
                    Color bg = new Color(255, 235, 205); // default: light peach
                    if (status.toLowerCase().contains("expired")) bg = new Color(255, 205, 210); // light red
                    else if (status.toLowerCase().contains("expiring")) bg = new Color(255, 224, 178); // light orange
                    else if (status.toLowerCase().contains("low stock")) bg = new Color(255, 249, 196); // light yellow
                    else if (status.toLowerCase().contains("out of stock")) bg = new Color(224, 224, 224); // light gray
                    c.setBackground(bg);
                }
                return c;
            }
        });

        applySort();
        applyFilters();
    }

    private void applySort() {
        String sort = (String) cbSort.getSelectedItem();
        java.util.List<javax.swing.RowSorter.SortKey> keys = new java.util.ArrayList<>();
        if ("A–Z (Name)".equals(sort)) {
            keys.add(new javax.swing.RowSorter.SortKey(2, javax.swing.SortOrder.ASCENDING));
        } else if ("Price Low–High".equals(sort)) {
            keys.add(new javax.swing.RowSorter.SortKey(6, javax.swing.SortOrder.ASCENDING));
        } else if ("Price High–Low".equals(sort)) {
            keys.add(new javax.swing.RowSorter.SortKey(6, javax.swing.SortOrder.DESCENDING));
        }
        rowSorter.setSortKeys(keys);
    }

    private void applyFilters() {
        String search = txtSearch.getText().trim().toLowerCase();
        String brandSel = (String) cbFilterBrand.getSelectedItem();
        String colorSel = (String) cbFilterColor.getSelectedItem();
        boolean filterBrand = brandSel != null && !brandSel.equals("All Brands");
        boolean filterColor = colorSel != null && !colorSel.equals("All Colors");

        javax.swing.RowFilter<DefaultTableModel, Integer> rf = new javax.swing.RowFilter<>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String code = String.valueOf(entry.getValue(1)).toLowerCase();
                String name = String.valueOf(entry.getValue(2)).toLowerCase();
                String brand = String.valueOf(entry.getValue(3));
                String color = String.valueOf(entry.getValue(4));
                String type = String.valueOf(entry.getValue(5)).toLowerCase();
                boolean matchesSearch = search.isEmpty() || code.contains(search) || name.contains(search) || type.contains(search)
                        || brand.toLowerCase().contains(search) || color.toLowerCase().contains(search);
                boolean matchesBrand = !filterBrand || brand.equals(brandSel);
                boolean matchesColor = !filterColor || color.equals(colorSel);
                return matchesSearch && matchesBrand && matchesColor;
            }
        };
        rowSorter.setRowFilter(rf);
    }
}
