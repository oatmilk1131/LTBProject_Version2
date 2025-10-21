package LTBPaintCenter.controller;

import LTBPaintCenter.model.*;
import LTBPaintCenter.view.POSPanel;

import javax.swing.*;
import java.util.List;

public class POSController {
    private Inventory inventory;
    private Report report;
    private POSPanel view;

    public POSController(Inventory inventory, Report report) {
        this.inventory = inventory;
        this.report = report;
        this.view = new POSPanel();

        attachHandlers();
        refreshPOS();
    }

    private void attachHandlers() {
        // Checkout logic provided as handler to POSPanel
        view.setCheckoutHandler(this::handleCheckout);
    }

    private boolean handleCheckout(List<SaleItem> cart) {
        if (cart == null || cart.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Cart is empty");
            return false;
        }

        String saleId = "S" + (report.getSales().size() + 1);
        Sale sale = new Sale(saleId);

        try {
            for (SaleItem item : cart) {
                Product product = inventory.getProduct(item.getProductId());
                if (product == null) throw new Exception("Product not found: " + item.getName());
                if (item.getQty() > product.getQuantity())
                    throw new Exception("Not enough stock for " + product.getName());

                sale.addItem(item);
                inventory.updateQuantity(item.getProductId(), -item.getQty());
            }
            report.recordSale(sale);
            JOptionPane.showMessageDialog(view, String.format("Sale recorded! Total: ₱%.2f", sale.getTotal()));
            refreshPOS();
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Checkout failed: " + e.getMessage());
            return false;
        }
    }

    public void refreshPOS() {
        view.refreshProducts(inventory.getAll());
    }

    public POSPanel getView() {
        return view;
    }
}
