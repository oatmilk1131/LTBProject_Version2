package LTBPaintCenter.view;

import LTBPaintCenter.model.Product;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public class POSPanel extends JPanel {
    private JPanel productGrid;
    private JLabel lblTotal;
    private JButton btnCheckout, btnClear;

    // Listener hooks
    private ProductClickListener addToCartListener;
    private Runnable checkoutListener;
    private Runnable clearCartListener;

    public POSPanel() {
        setLayout(new BorderLayout(10, 10));

        // Top panel for buttons and subtotal
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        JPanel buttonPanel = new JPanel();
        btnCheckout = new JButton("Checkout");
        btnClear = new JButton("Clear Cart");
        buttonPanel.add(btnClear);
        buttonPanel.add(btnCheckout);
        topPanel.add(buttonPanel, BorderLayout.WEST);

        lblTotal = new JLabel("Total: ₱0.00");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        topPanel.add(lblTotal, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Product grid panel
        productGrid = new JPanel(new GridLayout(0, 4, 10, 10));
        JScrollPane scroll = new JScrollPane(productGrid);
        add(scroll, BorderLayout.CENTER);
    }

    // Refresh the product grid
    public void refreshProducts(Collection<Product> products, java.util.List<?> cart) {
        productGrid.removeAll();
        for (Product p : products) {
            JPanel card = createProductCard(p);
            productGrid.add(card);
        }
        productGrid.revalidate();
        productGrid.repaint();

        // Update subtotal dynamically
        double total = cart.stream().mapToDouble(i -> {
            if (i instanceof Product) return ((Product) i).getPrice();
            return 0;
        }).sum();
        lblTotal.setText(String.format("Total: ₱%.2f", total));
    }

    private JPanel createProductCard(Product p) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        card.setBackground(Color.WHITE);

        JLabel name = new JLabel("<html><center>" + p.getName() + "</center></html>", SwingConstants.CENTER);
        JLabel price = new JLabel(String.format("₱%.2f", p.getPrice()), SwingConstants.CENTER);
        JLabel stock = new JLabel("Stock: " + p.getQuantity(), SwingConstants.CENTER);

        // Placeholder for image
        JLabel img = new JLabel("Image", SwingConstants.CENTER);
        img.setPreferredSize(new Dimension(100, 80));
        img.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        card.add(img, BorderLayout.NORTH);
        card.add(name, BorderLayout.CENTER);
        card.add(price, BorderLayout.SOUTH);
        card.add(stock, BorderLayout.EAST);

        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (addToCartListener != null) addToCartListener.productClicked(p);
            }
        });

        return card;
    }

    // Listener setters
    public void setAddToCartListener(ProductClickListener listener) {
        this.addToCartListener = listener;
    }

    public void setCheckoutListener(Runnable listener) {
        this.checkoutListener = listener;
        btnCheckout.addActionListener(e -> {
            if (checkoutListener != null) checkoutListener.run();
        });
    }

    public void setClearCartListener(Runnable listener) {
        this.clearCartListener = listener;
        btnClear.addActionListener(e -> {
            if (clearCartListener != null) clearCartListener.run();
        });
    }

    @FunctionalInterface
    public interface ProductClickListener {
        void productClicked(Product product);
    }

    public void setTotal(double total) {
        lblTotal.setText(String.format("Total: ₱%.2f", total));
    }
}
