package LTBPaintCenter.controller;

import LTBPaintCenter.model.*;
import LTBPaintCenter.view.POSPanel;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

// POSController, manages checkout flow and updates product display.
public class POSController {
    private final Inventory inventory;
    private final Report report;
    private final POSPanel view;

    public POSController(Inventory inventory, Report report) {
        this.inventory = inventory;
        this.report = report;
        this.view = new POSPanel();

        attachHandlers();
        refreshPOS();
    }

    private void attachHandlers() {
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
            double subtotal = 0;
            for (SaleItem item : cart) {
                Product product = inventory.getProduct(item.getProductId());
                if (product == null) throw new Exception("Product not found: " + item.getName());
                if (item.getQty() > product.getQuantity())
                    throw new Exception("Not enough stock for " + product.getName());

                sale.addItem(item);
                subtotal += item.getSubtotal();
            }

            double vat = subtotal * 0.12;
            double total = subtotal + vat;

            // Step 1: Confirmation dialog before checkout
            int confirm = JOptionPane.showConfirmDialog(
                    view,
                    String.format("Subtotal: ₱%.2f\nVAT (12%%): ₱%.2f\nTotal: ₱%.2f\n\nConfirm purchase?", subtotal, vat, total),
                    "Confirm Checkout",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) return false;

            // Step 2: Finalize sale
            for (SaleItem item : cart) {
                inventory.updateQuantity(item.getProductId(), -item.getQty());
            }
            sale.setTotal(total);
            report.recordSale(sale);

            // Step 3: Show receipt
            showReceiptWindow(sale, subtotal, vat, total);

            refreshPOS();
            return true;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Checkout failed: " + e.getMessage());
            return false;
        }
    }

    private void showReceiptWindow(Sale sale, double subtotal, double vat, double total) {
        JTextArea receiptArea = new JTextArea();
        receiptArea.setEditable(false);

        StringBuilder sb = new StringBuilder();
        sb.append("       LTB PAINT CENTER\n");
        sb.append("    Official Sales Receipt\n");
        sb.append("------------------------------------\n");
        sb.append("Sale ID: ").append(sale.getId()).append("\n");
        sb.append("Date: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
        sb.append("------------------------------------\n");

        for (SaleItem item : sale.getItems()) {
            sb.append(String.format("%-20s x%-2d ₱%.2f\n", item.getName(), item.getQty(), item.getSubtotal()));
        }

        sb.append("------------------------------------\n");
        sb.append(String.format("Subtotal:       ₱%.2f\n", subtotal));
        sb.append(String.format("VAT (12%%):      ₱%.2f\n", vat));
        sb.append(String.format("TOTAL:          ₱%.2f\n", total));
        sb.append("------------------------------------\n");
        sb.append("Thank you for your purchase!\n");

        receiptArea.setText(sb.toString());
        JOptionPane.showMessageDialog(view, new JScrollPane(receiptArea), "Receipt", JOptionPane.PLAIN_MESSAGE);
    }

    public void refreshPOS() {
        view.refreshProducts(inventory.getAll());
    }

    public POSPanel getView() {
        return view;
    }
}
