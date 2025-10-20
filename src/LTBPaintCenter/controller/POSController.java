package LTBPaintCenter.controller;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import LTBPaintCenter.model.*;
import LTBPaintCenter.view.*;

public class POSController {
    private Inventory inventory;
    private Report report;
    private POSPanel view;
    private List<SaleItem> cart = new ArrayList<>();

    public POSController(Inventory inv, Report rep) {
        this.inventory = inv; this.report = rep; this.view = new POSPanel();
        attachListeners();
    }

    public JPanel getView() { return view; }

    private void attachListeners() {
        view.btnAdd.addActionListener(e -> addToCart());
        view.btnClear.addActionListener(e -> clearCart());
        view.btnCheckout.addActionListener(e -> checkout());
    }

    private void addToCart() {
        String id = view.tfProductId.getText().trim();
        int qty;
        try { qty = Integer.parseInt(view.tfQty.getText().trim()); }
        catch (Exception ex) { JOptionPane.showMessageDialog(view, "Invalid qty"); return; }
        Product p = inventory.getProduct(id);
        if (p == null) { JOptionPane.showMessageDialog(view, "Product not found"); return; }
        if (qty <= 0 || qty > p.getQuantity()) { JOptionPane.showMessageDialog(view, "Quantity invalid or out of stock"); return; }

        // add to cart (no merge logic for simplicity)
        SaleItem it = new SaleItem(p.getId(), p.getName(), p.getPrice(), qty);
        cart.add(it);
        view.tableModel.addRow(new Object[]{it.getProductId(), it.getName(), String.format("%.2f", it.getPrice()), it.getQty(), String.format("%.2f", it.getSubtotal())});
        updateTotalLabel();
        view.tfProductId.setText(""); view.tfQty.setText("");
    }

    private void updateTotalLabel() {
        double total = cart.stream().mapToDouble(SaleItem::getSubtotal).sum();
        view.lblTotal.setText(String.format("Total: ₱%.2f", total));
    }

    private void clearCart() {
        cart.clear();
        int rows = view.tableModel.getRowCount();
        for (int i = rows -1; i >=0; --i) view.tableModel.removeRow(i);
        updateTotalLabel();
    }

    private void checkout() {
        if (cart.isEmpty()) { JOptionPane.showMessageDialog(view, "Cart is empty"); return; }
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
}