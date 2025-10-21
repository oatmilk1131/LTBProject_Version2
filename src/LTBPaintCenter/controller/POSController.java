package LTBPaintCenter.controller;

import LTBPaintCenter.model.Inventory;
import LTBPaintCenter.model.Product;
import LTBPaintCenter.model.Report;
import LTBPaintCenter.model.Sale;
import LTBPaintCenter.model.SaleItem;
import LTBPaintCenter.view.POSPanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class POSController {
    private Inventory inventory;
    private Report report;
    private POSPanel view;
    private List<SaleItem> cart;

    public POSController(Inventory inv, Report rep) {
        this.inventory = inv;
        this.report = rep;
        this.view = new POSPanel();
        this.cart = new ArrayList<>();
        attachListeners();
        refreshPOS(); // ensures products are visible on load
    }

    public POSPanel getView() { return view; }

    private void attachListeners() {
        view.setCheckoutListener(e -> checkout());
        view.setClearCartListener(e -> clearCart());
        view.setAddToCartListener(this::promptAddToCart);
    }

    // Called when switching to POS panel
    public void refreshPOS() {
        view.refreshProducts(inventory.getAll(), cart);
        view.updateSubtotal(cart);
    }

    void promptAddToCart(Product selected) {
        if (selected == null) return;

        int maxQty = selected.getQuantity();
        if (maxQty == 0) {
            JOptionPane.showMessageDialog(view, "Out of stock");
            return;
        }

        String qtyStr = JOptionPane.showInputDialog(view,
                "Enter quantity (max " + maxQty + "):", "1");
        if (qtyStr == null) return;

        int qty;
        try {
            qty = Integer.parseInt(qtyStr);
        } catch (Exception ex) { return; }

        if (qty <= 0 || qty > maxQty) {
            JOptionPane.showMessageDialog(view, "Invalid quantity");
            return;
        }

        // Merge with existing cart item if present
        SaleItem existing = cart.stream()
                .filter(it -> it.getProductId().equals(selected.getId()))
                .findFirst().orElse(null);

        if (existing != null) {
            if (existing.getQty() + qty > selected.getQuantity()) {
                JOptionPane.showMessageDialog(view, "Quantity exceeds stock");
                return;
            }
            existing.addQuantity(qty);
        } else {
            cart.add(new SaleItem(selected.getId(), selected.getName(),
                    selected.getPrice(), qty));
        }

        refreshPOS();
    }

    void clearCart() {
        cart.clear();
        refreshPOS();
    }

    void checkout() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Cart is empty");
            return;
        }

        String saleId = "S" + (report.getSales().size() + 1);
        Sale sale = new Sale(saleId);
        for (SaleItem it : cart) {
            sale.addItem(it);
            inventory.updateQuantity(it.getProductId(), -it.getQty());
        }
        report.recordSale(sale);
        JOptionPane.showMessageDialog(view,
                "Sale recorded. Total: ₱" + String.format("%.2f", sale.getTotal()));
        clearCart();
    }
}
