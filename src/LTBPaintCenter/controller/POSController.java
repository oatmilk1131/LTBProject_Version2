package LTBPaintCenter.controller;

import LTBPaintCenter.model.*;
import LTBPaintCenter.view.*;
import javax.swing.*;
import java.util.*;

public class POSController {
    private Inventory inventory;
    private Report report;
    private POSPanel view;
    private List<SaleItem> cart = new ArrayList<>();
    private InventoryController inventoryController;

    public POSController(Inventory inv, Report rep) {
        this.inventory = inv;
        this.report = rep;
        this.view = new POSPanel();
        attachListeners();
    }

    public void setInventoryController(InventoryController ic) { this.inventoryController = ic; }
    public JPanel getView() { return view; }

    private void attachListeners() {
        view.btnAdd.addActionListener(e -> addOrUpdateCart());
        view.btnClear.addActionListener(e -> clearCart());
        view.btnCheckout.addActionListener(e -> checkout());
        view.table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int r = view.table.getSelectedRow();
                    if (r >= 0) removeFromCart(r);
                }
            }
        });
    }

    private void addOrUpdateCart() {
        String id = view.tfProductId.getText().trim();
        int qty;
        try { qty = Integer.parseInt(view.tfQty.getText().trim()); }
        catch(Exception ex) { JOptionPane.showMessageDialog(view,"Invalid quantity"); return; }

        Product p = inventory.getProduct(id);
        if(p==null) { JOptionPane.showMessageDialog(view,"Product not found"); return; }

        int existingQtyInCart = cart.stream().filter(it->it.getProductId().equals(id))
                .mapToInt(SaleItem::getQty).sum();

        if(qty <= 0 || qty + existingQtyInCart > p.getQuantity()) {
            JOptionPane.showMessageDialog(view,"Quantity invalid or exceeds stock"); return;
        }

        Optional<SaleItem> existing = cart.stream().filter(it->it.getProductId().equals(id)).findFirst();
        if(existing.isPresent()) {
            SaleItem it = existing.get();
            cart.remove(it);
            cart.add(new SaleItem(id,p.getName(),p.getPrice(), it.getQty()+qty));
        } else {
            cart.add(new SaleItem(id,p.getName(),p.getPrice(), qty));
        }
        refreshCartTable();
        view.tfProductId.setText(""); view.tfQty.setText("");
    }

    private void removeFromCart(int row) {
        if(row >=0 && row < cart.size()) {
            cart.remove(row);
            refreshCartTable();
        }
    }

    private void refreshCartTable() {
        var m = view.tableModel;
        for(int i=m.getRowCount()-1;i>=0;i--) m.removeRow(i);
        for(SaleItem it : cart) {
            m.addRow(new Object[]{
                    it.getProductId(), it.getName(),
                    String.format("%.2f",it.getPrice()),
                    it.getQty(), String.format("%.2f",it.getSubtotal())
            });
        }
        updateTotalLabel();
    }

    private void updateTotalLabel() {
        double total = cart.stream().mapToDouble(SaleItem::getSubtotal).sum();
        view.lblTotal.setText(String.format("Total: ₱%.2f", total));
    }

    private void clearCart() { cart.clear(); refreshCartTable(); }

    private void checkout() {
        if(cart.isEmpty()) { JOptionPane.showMessageDialog(view,"Cart is empty"); return; }

        double total = cart.stream().mapToDouble(SaleItem::getSubtotal).sum();
        int confirm = JOptionPane.showConfirmDialog(view,
                String.format("Total: ₱%.2f\nConfirm checkout?", total),
                "Confirm Checkout", JOptionPane.YES_NO_OPTION);
        if(confirm != JOptionPane.YES_OPTION) return;

        // Unique sale ID generation
        String saleId = "S" + (report.getSales().size() + 1 + new Random().nextInt(1000));

        Sale sale = new Sale(saleId);
        for(SaleItem it : cart) {
            sale.addItem(it);
            inventory.updateQuantity(it.getProductId(), -it.getQty());
        }
        report.recordSale(sale);
        clearCart();

        if(inventoryController!=null) inventoryController.refreshTable();
    }
}
