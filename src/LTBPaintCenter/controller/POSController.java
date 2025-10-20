package LTBPaintCenter.controller;

import LTBPaintCenter.model.Inventory;
import LTBPaintCenter.model.Product;
import LTBPaintCenter.model.Sale;
import LTBPaintCenter.model.SaleItem;
import LTBPaintCenter.model.Report;
import LTBPaintCenter.view.POSPanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class POSController {
    private Inventory inventory;
    private Report report;
    private POSPanel view;
    private List<SaleItem> cart = new ArrayList<>();

    public POSController(Inventory inventory, Report report) {
        this.inventory = inventory;
        this.report = report;
        this.view = new POSPanel();
        attachListeners();
    }

    public POSPanel getView() {
        return view;
    }

    private void attachListeners() {
        view.setAddToCartListener(this::promptAddToCart);
        view.setCheckoutListener(this::checkout);
        view.setClearCartListener(this::clearCart);
    }

    public void refreshPOS() {
        view.refreshProducts(inventory.getAll(), cart);
    }

    private void promptAddToCart(Product product) {
        // Quantity dialog with - x + style
        SpinnerNumberModel model = new SpinnerNumberModel(1, 1, product.getQuantity(), 1);
        JSpinner spinner = new JSpinner(model);
        int option = JOptionPane.showConfirmDialog(
                view,
                spinner,
                "Enter quantity for " + product.getName(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option == JOptionPane.OK_OPTION) {
            int qty = (int) spinner.getValue();
            if (qty <= 0 || qty > product.getQuantity()) {
                JOptionPane.showMessageDialog(view, "Invalid quantity.");
                return;
            }

            // Check if already in cart
            SaleItem existing = cart.stream()
                    .filter(i -> i.getProductId().equals(product.getId()))
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                if (existing.getQty() + qty > product.getQuantity()) {
                    JOptionPane.showMessageDialog(view, "Cannot exceed stock quantity.");
                    return;
                }
                existing.addQuantity(qty);
            } else {
                cart.add(new SaleItem(product.getId(), product.getName(), product.getPrice(), qty));
            }
            refreshPOS();
        }
    }

    private void checkout() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Cart is empty.");
            return;
        }

        String saleId = "S" + (report.getSales().size() + 1);
        Sale sale = new Sale(saleId);

        for (SaleItem it : cart) {
            sale.addItem(it);
            inventory.updateQuantity(it.getProductId(), -it.getQty());
        }

        report.recordSale(sale);
        JOptionPane.showMessageDialog(view, String.format("Sale recorded. Total: ₱%.2f", sale.getTotal()));
        clearCart();
    }

    private void clearCart() {
        cart.clear();
        refreshPOS();
    }
}
