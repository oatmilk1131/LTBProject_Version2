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
    private final JPanel productGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
    private final JScrollPane productScroll = new JScrollPane(productGrid);
    private final DefaultTableModel cartTableModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Price", "Qty", "Subtotal"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable cartTable = new JTable(cartTableModel);
    private final JLabel lblTotal = new JLabel("Total: ₱0.00");

    private final JButton btnCheckout = new JButton("Checkout");
    private final JButton btnClear = new JButton("Clear Cart");
    private final JButton btnRemove = new JButton("Remove Selected");

    private final LinkedHashMap<String, SaleItem> cart = new LinkedHashMap<>();
    private final Map<String, Product> productMap = new LinkedHashMap<>();

    public interface CheckoutHandler {
        boolean handleCheckout(List<SaleItem> cartSnapshot);
    }

    private CheckoutHandler checkoutHandler = null;

    public POSPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setBackground(Color.WHITE);

        initProductArea();
        initCartArea();
    }

    // ---------------------------------
    // UI Setup
    // ---------------------------------
    private void initProductArea() {
        productGrid.setBackground(Color.WHITE);
        productScroll.setBorder(BorderFactory.createTitledBorder("Available Products"));
        productScroll.getVerticalScrollBar().setUnitIncrement(12);
        add(productScroll, BorderLayout.CENTER);
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

        // Bottom buttons panel
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

        // Button actions
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
    }

    // ---------------------------------
    // Public API
    // ---------------------------------
    public void refreshProducts(Collection<Product> products) {
        productGrid.removeAll();
        productMap.clear();

        for (Product p : products) {
            productMap.put(p.getId(), p);
            productGrid.add(createProductCard(p));
        }

        productGrid.revalidate();
        productGrid.repaint();
    }

    public void setCheckoutHandler(CheckoutHandler handler) { this.checkoutHandler = handler; }

    public List<SaleItem> getCartSnapshot() { return new ArrayList<>(cart.values()); }

    public void clearCart() {
        cart.clear();
        refreshCartTable();
    }

    // ---------------------------------
    // Product Cards
    // ---------------------------------
    private JPanel createProductCard(Product p) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
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

        JPanel bottom = new JPanel(new GridLayout(2, 1));
        bottom.setBackground(Color.WHITE);
        bottom.add(lblName);
        bottom.add(lblPrice);
        card.add(bottom, BorderLayout.SOUTH);

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { openQuantityDialogAndAdd(p); }

            @Override
            public void mouseEntered(MouseEvent e) { card.setBackground(new Color(245, 245, 245)); }

            @Override
            public void mouseExited(MouseEvent e) { card.setBackground(Color.WHITE); }
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

    // ---------------------------------
    // Cart Logic
    // ---------------------------------
    private void addToCart(Product p, int qty) {
        SaleItem existing = cart.get(p.getId());
        if (existing != null) {
            existing.addQuantity(qty);
        } else {
            cart.put(p.getId(), new SaleItem(p.getId(), p.getName(), p.getPrice(), qty));
        }
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

    private void updateTotal() {
        double total = cart.values().stream().mapToDouble(SaleItem::getSubtotal).sum();
        lblTotal.setText(String.format("Total: ₱%.2f", total));
    }

    // ---------------------------------
    // Quantity Dialog (Cleaned)
    // ---------------------------------
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
