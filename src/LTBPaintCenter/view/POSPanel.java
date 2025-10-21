package LTBPaintCenter.view;

import LTBPaintCenter.model.Product;
import LTBPaintCenter.model.SaleItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class POSPanel extends JPanel {
    // Product area uses FlowLayout to keep cards square and not stretch vertically
    private final JPanel productGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
    private final JScrollPane productScroll = new JScrollPane(productGrid);

    // Cart table
    private final DefaultTableModel cartTableModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Price", "Qty", "Subtotal"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable cartTable = new JTable(cartTableModel);
    private final JLabel lblTotal = new JLabel("Total: ₱0.00");

    // Cart buttons
    private final JButton btnCheckout = new JButton("Checkout");
    private final JButton btnClear = new JButton("Clear Cart");
    private final JButton btnRemove = new JButton("Remove Selected");

    // Internal cart state
    private final LinkedHashMap<String, SaleItem> cart = new LinkedHashMap<>();
    private final Map<String, Product> productMap = new LinkedHashMap<>();

    // Filters (ensure these are instance fields and initialized before refresh)
    private final JComboBox<String> cbBrand = new JComboBox<>();
    private final JComboBox<String> cbColor = new JComboBox<>();
    private final JComboBox<String> cbType = new JComboBox<>();
    private boolean suppressFilterEvents = false;

    // Checkout handler (controller sets)
    public interface CheckoutHandler { boolean handleCheckout(List<SaleItem> cartSnapshot); }
    private CheckoutHandler checkoutHandler = null;

    public POSPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setBackground(Color.WHITE);

        initFilterBar();   // create and add filters BEFORE any product refresh
        initProductArea();
        initCartArea();

        // Wire filter listeners (safe — they check suppressFilterEvents)
        cbBrand.addActionListener(e -> { if (!suppressFilterEvents) updateProductGrid(productMap.values()); });
        cbColor.addActionListener(e -> { if (!suppressFilterEvents) updateProductGrid(productMap.values()); });
        cbType.addActionListener(e -> { if (!suppressFilterEvents) updateProductGrid(productMap.values()); });
    }

    // ---------------------------
    // Initialization
    // ---------------------------
    private void initFilterBar() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filters"));

        cbBrand.addItem("All Brands");
        cbColor.addItem("All Colors");
        cbType.addItem("All Types");

        filterPanel.add(new JLabel("Brand:"));
        filterPanel.add(cbBrand);
        filterPanel.add(Box.createHorizontalStrut(8));
        filterPanel.add(new JLabel("Color:"));
        filterPanel.add(cbColor);
        filterPanel.add(Box.createHorizontalStrut(8));
        filterPanel.add(new JLabel("Type:"));
        filterPanel.add(cbType);

        add(filterPanel, BorderLayout.NORTH);
    }

    private final JPanel gridContainer = new JPanel(); // wrapper for scroll content

    private void initProductArea() {
        gridContainer.setLayout(new BorderLayout());
        gridContainer.setBackground(Color.WHITE);

        // productGrid is embedded inside a wrapper panel for proper sizing
        productGrid.setBackground(Color.WHITE);
        productScroll.setViewportView(productGrid);
        productScroll.setBorder(BorderFactory.createTitledBorder("Available Products"));
        productScroll.getVerticalScrollBar().setUnitIncrement(16);
        productScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        gridContainer.add(productScroll, BorderLayout.CENTER);
        add(gridContainer, BorderLayout.CENTER);

        // Responsive resizing: recalculates number of columns on window resize
        productScroll.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                layoutGridToFitWidth();
            }
        });
    }

    private void layoutGridToFitWidth() {
        // Determine width available for cards
        int availableWidth = productScroll.getViewport().getWidth();
        int cardWidth = 150 + 12; // 150px card + 12px gap
        int columns = Math.max(1, availableWidth / cardWidth);

        // 4 is the ideal number of columns
        if (columns > 4) columns = 4;

        // Update layout dynamically
        int rows = (int) Math.ceil((double) productMap.size() / columns);
        GridLayout layout = new GridLayout(rows, columns, 12, 12);
        productGrid.setLayout(layout);
        productGrid.revalidate();
        productGrid.repaint();
    }


    private void initCartArea() {
        JPanel rightPanel = new JPanel(new BorderLayout(8, 8));
        rightPanel.setPreferredSize(new Dimension(350, 0));
        rightPanel.setBackground(Color.WHITE);

        JLabel lblCart = new JLabel("🛒 Cart");
        lblCart.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.add(lblCart, BorderLayout.WEST);
        top.add(lblTotal, BorderLayout.EAST);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scroll = new JScrollPane(cartTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        cartTable.setRowHeight(26);
        cartTable.getTableHeader().setReorderingAllowed(false);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttons.setBackground(Color.WHITE);
        buttons.add(btnRemove);
        buttons.add(btnClear);
        buttons.add(btnCheckout);

        btnCheckout.setBackground(new Color(0, 120, 215));
        btnCheckout.setForeground(Color.WHITE);
        btnCheckout.setFocusPainted(false);

        rightPanel.add(top, BorderLayout.NORTH);
        rightPanel.add(scroll, BorderLayout.CENTER);
        rightPanel.add(buttons, BorderLayout.SOUTH);

        add(rightPanel, BorderLayout.EAST);

        // Cart button actions
        btnClear.addActionListener(e -> {
            if (!cart.isEmpty()) {
                int confirm = JOptionPane.showConfirmDialog(this, "Clear entire cart?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) clearCart();
            }
        });

        btnRemove.addActionListener(e -> removeSelectedCartItem());
        btnCheckout.addActionListener(e -> {
            if (checkoutHandler == null) {
                JOptionPane.showMessageDialog(this, "Checkout handler not set!");
                return;
            }
            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cart is empty!");
                return;
            }
            boolean ok = checkoutHandler.handleCheckout(getCartSnapshot());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Sale recorded!");
                clearCart();
            }
        });

        // Double click to edit qty (optional)
        cartTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) editSelectedCartQty();
            }
        });
    }

    // ---------------------------
    // Public API
    // ---------------------------
    public void setCheckoutHandler(CheckoutHandler handler) { this.checkoutHandler = handler; }
    public List<SaleItem> getCartSnapshot() { return new ArrayList<>(cart.values()); }
    public void clearCart() { cart.clear(); refreshCartTable(); }

    /**
     * Refreshes the product grid and repopulates filter dropdowns.
     * Always safe to call (filters are created in constructor).
     */
    public void refreshProducts(Collection<Product> products) {
        if (products == null) products = Collections.emptyList();

        // update product map
        productMap.clear();
        for (Product p : products) productMap.put(p.getId(), p);

        // populate filter sets
        Set<String> brands = new TreeSet<>();
        Set<String> colors = new TreeSet<>();
        Set<String> types = new TreeSet<>();
        for (Product p : products) {
            if (p.getBrand() != null && !p.getBrand().isBlank()) brands.add(p.getBrand());
            if (p.getColor() != null && !p.getColor().isBlank()) colors.add(p.getColor());
            if (p.getType()  != null && !p.getType().isBlank())  types.add(p.getType());
        }

        // populate combo boxes safely
        suppressFilterEvents = true;
        try {
            cbBrand.removeAllItems(); cbBrand.addItem("All Brands");
            for (String s : brands) cbBrand.addItem(s);

            cbColor.removeAllItems(); cbColor.addItem("All Colors");
            for (String s : colors) cbColor.addItem(s);

            cbType.removeAllItems(); cbType.addItem("All Types");
            for (String s : types) cbType.addItem(s);
        } finally {
            suppressFilterEvents = false;
        }

        // finally update the grid (applies currently selected filter values)
        updateProductGrid(productMap.values());
        //
        layoutGridToFitWidth();
    }

    // update product grid using filter selections
    private void updateProductGrid(Collection<Product> products) {
        productGrid.removeAll();

        String selectedBrand = cbBrand.getSelectedItem() != null ? cbBrand.getSelectedItem().toString() : "All Brands";
        String selectedColor = cbColor.getSelectedItem() != null ? cbColor.getSelectedItem().toString() : "All Colors";
        String selectedType  = cbType.getSelectedItem() != null ? cbType.getSelectedItem().toString() : "All Types";

        for (Product p : products) {
            boolean brandOk = selectedBrand.equals("All Brands") || selectedBrand.equals(p.getBrand());
            boolean colorOk = selectedColor.equals("All Colors") || selectedColor.equals(p.getColor());
            boolean typeOk  = selectedType.equals("All Types")  || selectedType.equals(p.getType());

            if (brandOk && colorOk && typeOk) {
                productGrid.add(createProductCard(p));
            }
        }

        productGrid.revalidate();
        productGrid.repaint();
    }

    // ---------------------------
    // Product card & quantity dialog
    // ---------------------------
    private JPanel createProductCard(Product p) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(150, 150));
        card.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        card.setBackground(Color.WHITE);

        JLabel icon = new JLabel("🖌️", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        card.add(icon, BorderLayout.CENTER);

        JLabel lblName = new JLabel("<html><center>" + p.getName() + "</center></html>", SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JLabel lblPrice = new JLabel(String.format("₱%.2f", p.getPrice()), SwingConstants.CENTER);
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JPanel bottom = new JPanel(new GridLayout(2,1));
        bottom.setBackground(Color.WHITE);
        bottom.add(lblName);
        bottom.add(lblPrice);
        card.add(bottom, BorderLayout.SOUTH);

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { openQuantityDialogAndAdd(p); }
            @Override public void mouseEntered(MouseEvent e) { card.setBackground(new Color(245, 245, 245)); }
            @Override public void mouseExited(MouseEvent e) { card.setBackground(Color.WHITE); }
        });

        return card;
    }

    private void openQuantityDialogAndAdd(Product p) {
        int stock = p.getQuantity();
        int alreadyInCart = cart.containsKey(p.getId()) ? cart.get(p.getId()).getQty() : 0;
        int available = stock - alreadyInCart;
        if (available <= 0) {
            JOptionPane.showMessageDialog(this, "Out of stock!");
            return;
        }

        QuantityDialog qd = new QuantityDialog(p.getName(), available);
        Integer qty = qd.showDialog();
        if (qty != null && qty > 0) addToCart(p, qty);
    }

    // ---------------------------
    // Cart operations
    // ---------------------------
    private void addToCart(Product p, int qty) {
        SaleItem existing = cart.get(p.getId());
        if (existing != null) existing.addQuantity(qty);
        else cart.put(p.getId(), new SaleItem(p.getId(), p.getName(), p.getPrice(), qty));
        refreshCartTable();
    }

    private void refreshCartTable() {
        cartTableModel.setRowCount(0);
        for (SaleItem it : cart.values()) {
            cartTableModel.addRow(new Object[]{
                    it.getProductId(),
                    it.getName(),
                    String.format("%.2f", it.getPrice()),
                    it.getQty(),
                    String.format("%.2f", it.getSubtotal())
            });
        }
        updateTotal();
    }

    private void removeSelectedCartItem() {
        int row = cartTable.getSelectedRow();
        if (row < 0) return;
        String id = cartTableModel.getValueAt(row, 0).toString();
        cart.remove(id);
        refreshCartTable();
    }

    private void editSelectedCartQty() {
        int r = cartTable.getSelectedRow();
        if (r < 0) return;
        String id = cartTableModel.getValueAt(r, 0).toString();
        SaleItem itm = cart.get(id);
        if (itm == null) return;

        Product prod = productMap.get(id);
        int max = (prod != null) ? prod.getQuantity() : itm.getQty();
        int cartQtyOther = cart.values().stream().mapToInt(SaleItem::getQty).sum() - itm.getQty();
        int available = max - cartQtyOther;
        if (available < 1) available = itm.getQty();

        QuantityDialog qd = new QuantityDialog(itm.getName(), available);
        Integer res = qd.showDialog();
        if (res != null && res > 0) {
            itm.setQty(res);
            if (itm.getQty() <= 0) cart.remove(id);
            refreshCartTable();
        }
    }

    private void updateTotal() {
        double total = cart.values().stream().mapToDouble(SaleItem::getSubtotal).sum();
        lblTotal.setText(String.format("Total: ₱%.2f", total));
    }

    // ---------------------------
    // Dialog
    // ---------------------------
    private static class QuantityDialog extends JDialog {
        private Integer result = null;
        private final JTextField txtQty;

        public QuantityDialog(String name, int max) {
            setModal(true);
            setTitle("Select Quantity");
            setSize(300, 180);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout(8, 8));
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            JLabel lblTitle = new JLabel("<html><center>" + name + "<br>(Max: " + max + ")</center></html>", SwingConstants.CENTER);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
            add(lblTitle, BorderLayout.NORTH);

            JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            JButton btnMinus = new JButton("-");
            JButton btnPlus = new JButton("+");
            txtQty = new JTextField("1", 4);
            txtQty.setHorizontalAlignment(JTextField.CENTER);
            txtQty.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            center.add(btnMinus);
            center.add(txtQty);
            center.add(btnPlus);
            add(center, BorderLayout.CENTER);

            btnMinus.addActionListener(e -> adjust(-1));
            btnPlus.addActionListener(e -> adjust(1));

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnCancel = new JButton("Cancel");
            JButton btnAdd = new JButton("Add");
            bottom.add(btnCancel);
            bottom.add(btnAdd);
            add(bottom, BorderLayout.SOUTH);

            btnCancel.addActionListener(e -> { result = null; dispose(); });

            btnAdd.addActionListener(e -> {
                try {
                    int val = Integer.parseInt(txtQty.getText().trim());
                    if (val < 1 || val > max) throw new Exception();
                    result = val;
                    dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Enter a valid number (1–" + max + ")");
                }
            });
        }

        private void adjust(int delta) {
            try {
                int val = Integer.parseInt(txtQty.getText().trim());
                val += delta;
                if (val < 1) val = 1;
                txtQty.setText(String.valueOf(val));
            } catch (Exception ignored) {
                txtQty.setText("1");
            }
        }

        public Integer showDialog() {
            setVisible(true);
            return result;
        }
    }
}
