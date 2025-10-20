package LTBPaintCenter.view;

import LTBPaintCenter.model.Product;
import LTBPaintCenter.model.SaleItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;

public class POSPanel extends JPanel {
    private JPanel productGrid;
    private JLabel lblSubtotal;

    private ActionListener checkoutListener;
    private ActionListener clearCartListener;
    private ProductClickListener addToCartListener;

    public POSPanel(){
        setLayout(new BorderLayout(8,8));

        // Product grid
        productGrid = new JPanel(new GridLayout(0,4,10,10));
        add(new JScrollPane(productGrid), BorderLayout.CENTER);

        // Subtotal panel
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblSubtotal = new JLabel("Total: ₱0.00");
        rightPanel.add(lblSubtotal);
        add(rightPanel, BorderLayout.EAST);
    }

    public void refreshProducts(Collection<Product> products, List<SaleItem> cart){
        productGrid.removeAll();
        for(Product p: products){
            JButton btn = new JButton("<html>"+p.getName()+"<br>₱"+String.format("%.2f",p.getPrice())+"</html>");
            btn.setPreferredSize(new Dimension(120,120));
            btn.addActionListener(e -> {
                if(addToCartListener != null) addToCartListener.onClick(p);
            });
            productGrid.add(btn);
        }
        revalidate();
        repaint();
    }

    public void updateSubtotal(List<SaleItem> cart){
        double total = cart.stream().mapToDouble(SaleItem::getSubtotal).sum();
        lblSubtotal.setText("Total: ₱"+String.format("%.2f",total));
    }

    // Listener setters
    public void setCheckoutListener(ActionListener l){ checkoutListener = l; }
    public void setClearCartListener(ActionListener l){ clearCartListener = l; }
    public void setAddToCartListener(ProductClickListener l){ addToCartListener = l; }

    // Custom functional interface for product clicks
    @FunctionalInterface
    public interface ProductClickListener{
        void onClick(Product product);
    }
}
