package LTBPaintCenter.view;

import LTBPaintCenter.model.Product;
import LTBPaintCenter.model.SaleItem;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * POSPanel - product grid (4 columns) + cart (table) on the right.
 *
 * Responsibilities:
 * - Render products in a 4-column grid (image placeholder + name + price + stock)
 * - Clicking a product opens a quantity dialog (- [n] +) and adds to cart
 * - Cart displayed in a table: ID | Name | Price | Qty | Subtotal
 * - Remove selected, edit qty (double-click), clear cart, checkout (delegated)
 *
 * Checkout is delegated to a CheckoutHandler provided by the controller:
 *   boolean handleCheckout(List<SaleItem> cart)
 * The handler should perform the sale (update inventory/report) and return true on success.
 */
public class POSPanel extends JPanel {
    // UI
    private final JPanel productGrid = new JPanel(new GridLayout(0, 4, 12, 12));
    private final JScrollPane productScroll = new JScrollPane(productGrid, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    private final DefaultTableModel cartTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Price", "Qty", "Subtotal"}, 0) {
        // Make cells non-editable directly (we edit via dialog)
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable cartTable = new JTable(cartTableModel);
    private final JLabel lblTotal = new JLabel("Total: ₱0.00");
    private final JButton btnCheckout = new JButton("Checkout");
    private final JButton btnClear = new JButton("Clear Cart");
    private final JButton btnRemove = new JButton("Remove Selected");

    // Internal state
    private final LinkedHashMap<String, SaleItem> cart = new LinkedHashMap<>(); // productId -> SaleItem
    private final Map<String, Product> productMap = new LinkedHashMap<>(); // latest products by id (for stock/name/price lookups)

    // Checkout handler (controller provides)
    public interface CheckoutHandler {
        /** Should perform sale, update inventory/report. Return true if sale succeeded */
        boolean handleCheckout(List<SaleItem> cartSnapshot);
    }
    private CheckoutHandler checkoutHandler = null;

    public POSPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        initProductArea();
        initCartArea();
    }

    // ---------------------------
    // Public API
    // ---------------------------

    /**
     * Refresh product thumbnails from the given collection.
     * Must be called when inventory changes or on first load.
     */
    public void refreshProducts(Collection<Product> products) {
        // update product map for stock/name lookups
        productMap.clear();
        for (Product p : products) productMap.put(p.getId(), p);

        // recreate grid
        productGrid.removeAll();
        for (Product p : products) {
            productGrid.add(createProductCard(p));
        }
        productGrid.revalidate();
        productGrid.repaint();
    }

    /** Controller injects handler to execute checkout logic (update inventory, record sale) */
    public void setCheckoutHandler(CheckoutHandler handler) {
        this.checkoutHandler = handler;
    }

    /** Return a snapshot of the cart (useful for controller) */
    public List<SaleItem> getCartSnapshot() {
        return new ArrayList<>(cart.values());
    }

    /** Clear cart (UI + internal) */
    public void clearCart() {
        cart.clear();
        refreshCartTable();
    }

    // ---------------------------
    // Private UI init
    // ---------------------------

    private void initProductArea() {
        // give product area a white/light background to look Windows-like
        productGrid.setBackground(Color.WHITE);
        productScroll.getVerticalScrollBar().setUnitIncrement(12);
        add(productScroll, BorderLayout.CENTER);
    }

    private void initCartArea() {
        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.setPreferredSize(new Dimension(320, 0));

        // Cart table
        cartTable.setFillsViewportHeight(true);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cartTable.getTableHeader().setReorderingAllowed(false);
        cartTable.setRowHeight(26);

        JScrollPane cartScroll = new JScrollPane(cartTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        right.add(cartScroll, BorderLayout.CENTER);

        // Top: label
        JLabel lblCart = new JLabel("Cart");
        lblCart.setFont(lblCart.getFont().deriveFont(Font.BOLD, 14f));
        JPanel top = new JPanel(new BorderLayout());
        top.add(lblCart, BorderLayout.WEST);
        top.add(lblTotal, BorderLayout.EAST);
        right.add(top, BorderLayout.NORTH);

        // Bottom: buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        btnRow.add(btnRemove);
        btnRow.add(btnClear);
        btnRow.add(btnCheckout);
        right.add(btnRow, BorderLayout.SOUTH);

        // Actions
        btnClear.addActionListener(e -> {
            if (cart.isEmpty()) return;
            int confirm = JOptionPane.showConfirmDialog(this, "Clear cart?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) clearCart();
        });

        btnRemove.addActionListener(e -> removeSelectedCartItem());

        btnCheckout.addActionListener(e -> {
            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cart is empty");
                return;
            }
            if (checkoutHandler == null) {
                JOptionPane.showMessageDialog(this, "No checkout handler connected");
                return;
            }
            // give controller a snapshot to process sale
            List<SaleItem> snapshot = getCartSnapshot();
            boolean ok = checkoutHandler.handleCheckout(snapshot);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Sale recorded.");
                clearCart();
            } else {
                // controller will have shown appropriate errors
            }
        });

        // Double-click to edit qty
        cartTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) editSelectedCartQty();
            }
        });

        add(right, BorderLayout.EAST);
    }

    // ---------------------------
    // Product card creation & add-to-cart flow
    // ---------------------------

    private JPanel createProductCard(Product p) {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        card.setBackground(Color.WHITE);

        // Image placeholder
        JLabel img = new JLabel();
        img.setHorizontalAlignment(SwingConstants.CENTER);
        img.setPreferredSize(new Dimension(120, 80));
        img.setText("<html><center>Image</center></html>");
        img.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        card.add(img, BorderLayout.NORTH);

        // Middle: name + price + stock
        JPanel center = new JPanel(new GridLayout(3, 1));
        center.setBackground(Color.WHITE);
        JLabel lblName = new JLabel("<html><center>" + escapeHtml(p.getName()) + "</center></html>", SwingConstants.CENTER);
        lblName.setFont(lblName.getFont().deriveFont(12f));
        JLabel lblPrice = new JLabel(String.format("₱%.2f", p.getPrice()), SwingConstants.CENTER);
        JLabel lblStock = new JLabel("Stock: " + p.getQuantity(), SwingConstants.CENTER);
        center.add(lblName);
        center.add(lblPrice);
        center.add(lblStock);
        card.add(center, BorderLayout.CENTER);

        // Click handler: open quantity dialog
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                openQuantityDialogAndAdd(p);
            }
        });

        return card;
    }

    private void openQuantityDialogAndAdd(Product p) {
        int available = p.getQuantity();
        // also subtract qty already in cart for this product
        SaleItem inCart = cart.get(p.getId());
        if (inCart != null) available = available - inCart.getQty();
        if (available <= 0) {
            JOptionPane.showMessageDialog(this, "Product out of stock");
            return;
        }

        QuantityDialog qd = new QuantityDialog(p.getName(), 1, available, p.getPrice());
        Integer result = qd.showDialog();
        if (result != null && result > 0) {
            addToCart(p, result);
        }
    }

    private void addToCart(Product p, int qty) {
        if (qty <= 0) return;
        SaleItem existing = cart.get(p.getId());
        if (existing != null) {
            existing.addQuantity(qty);
        } else {
            cart.put(p.getId(), new SaleItem(p.getId(), p.getName(), p.getPrice(), qty));
        }
        refreshCartTable();
    }

    // ---------------------------
    // Cart table + helpers
    // ---------------------------

    private void refreshCartTable() {
        // rebuild table
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
        updateTotalLabel();
    }

    private void updateTotalLabel() {
        double total = cart.values().stream().mapToDouble(SaleItem::getSubtotal).sum();
        lblTotal.setText(String.format("Total: ₱%.2f", total));
    }

    private void removeSelectedCartItem() {
        int r = cartTable.getSelectedRow();
        if (r < 0) return;
        String id = (String) cartTableModel.getValueAt(r, 0);
        if (id != null) {
            cart.remove(id);
            refreshCartTable();
        }
    }

    private void editSelectedCartQty() {
        int r = cartTable.getSelectedRow();
        if (r < 0) return;
        String id = (String) cartTableModel.getValueAt(r, 0);
        SaleItem itm = cart.get(id);
        if (itm == null) return;

        Product prod = productMap.get(id);
        int max = (prod != null) ? prod.getQuantity() : itm.getQty(); // fallback
        // allow adjusting based on product stock + current cart: compute available as product.quantity - (cartQty - itm.qty)
        int cartQtyOther = cart.values().stream().mapToInt(SaleItem::getQty).sum() - itm.getQty();
        int available = max - cartQtyOther;
        if (available < 1) available = itm.getQty(); // cannot increase

        QuantityDialog qd = new QuantityDialog(itm.getName(), itm.getQty(), available, itm.getPrice());
        Integer res = qd.showDialog();
        if (res != null && res > 0) {
            itm.setQty(res);
            // if qty becomes 0 (shouldn't happen due to clamp) remove
            if (itm.getQty() <= 0) cart.remove(id);
            refreshCartTable();
        }
    }

    // ---------------------------
    // Small utilities
    // ---------------------------

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ---------------------------
    // QuantityDialog inner class ( - [n] + plus confirm/cancel )
    // ---------------------------
    private static class QuantityDialog extends JDialog {
        private Integer selected = null;
        private final JSpinner spinner;
        private final JButton btnAdd = new JButton("Add");
        private final JButton btnCancel = new JButton("Cancel");

        public QuantityDialog(String productName, int initial, int max, double pricePerUnit) {
            setModal(true);
            setTitle("Add: " + productName);
            setSize(320, 140);
            setLocationRelativeTo(null);
            setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6,6,6,6);
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
            JLabel lbl = new JLabel(String.format("%s — ₱%.2f each (max %d)", productName, pricePerUnit, max));
            add(lbl, gbc);

            gbc.gridy = 1; gbc.gridwidth = 1;
            JButton btnMinus = new JButton("-");
            spinner = new JSpinner(new SpinnerNumberModel(initial, 1, max, 1));
            JButton btnPlus = new JButton("+");

            btnMinus.addActionListener(e -> {
                int v = (Integer) spinner.getValue();
                if (v > 1) spinner.setValue(v-1);
            });

            btnPlus.addActionListener(e -> {
                int v = (Integer) spinner.getValue();
                if (v < max) spinner.setValue(v+1);
            });

            gbc.gridx = 0;
            add(btnMinus, gbc);
            gbc.gridx = 1;
            add(spinner, gbc);
            gbc.gridx = 2;
            add(btnPlus, gbc);

            // buttons row
            JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            row.add(btnCancel);
            row.add(btnAdd);
            gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 3;
            add(row, gbc);

            btnCancel.addActionListener(e -> { selected = null; setVisible(false); });
            btnAdd.addActionListener(e -> {
                selected = (Integer) spinner.getValue();
                setVisible(false);
            });
        }

        public Integer showDialog() {
            setVisible(true);
            return selected;
        }
    }

}
