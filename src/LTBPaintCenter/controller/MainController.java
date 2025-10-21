package LTBPaintCenter.controller;

import LTBPaintCenter.model.Global;
import LTBPaintCenter.model.Inventory;
import LTBPaintCenter.model.Report;
import LTBPaintCenter.model.SaleItem;
import LTBPaintCenter.view.MainFrame;

import javax.swing.*;
import java.util.List;

public class MainController {
    private Inventory inventory;
    private Report report;
    private MainFrame frame;

    private POSController posController;
    private InventoryController inventoryController;
    private MonitoringController monitoringController;

    public MainController() {
        inventory = new Inventory();
        report = new Report();
        Global.inventory = inventory;
        Global.report = report;

        seedData();

        // Controllers
        posController = new POSController(inventory, report);
        inventoryController = new InventoryController(inventory);
        monitoringController = new MonitoringController(report);

        // Frame
        frame = new MainFrame(posController, inventoryController, monitoringController);
        frame.setVisible(true);

        // Add panels
        frame.addPanel(posController.getView(), "POS");
        frame.addPanel(inventoryController.getView(), "Inventory");
        frame.addPanel(monitoringController.getView(), "Monitoring");

        // ✅ Connect checkout handler to POS panel
        posController.getView().setCheckoutHandler(this::handleCheckout);

        // Show default view
        frame.showPanel("POS");
        frame.setVisible(true);
    }

    private boolean handleCheckout(List<SaleItem> cart) {
        if (cart == null || cart.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Cart is empty");
            return false;
        }

        try {
            String saleId = "S" + (report.getSales().size() + 1);
            var sale = new LTBPaintCenter.model.Sale(saleId);
            for (var item : cart) {
                sale.addItem(item);
                inventory.updateQuantity(item.getProductId(), -item.getQty());
            }
            report.recordSale(sale);
            monitoringController.refresh();
            inventoryController.refreshInventory();
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Checkout failed: " + e.getMessage());
            return false;
        }
    }

    private void seedData() {
        inventory.addProduct(new LTBPaintCenter.model.Product("P001", "Coke 330ml", 25.0, 50));
        inventory.addProduct(new LTBPaintCenter.model.Product("P002", "Bread Loaf", 40.0, 20));
        inventory.addProduct(new LTBPaintCenter.model.Product("P003", "Chips", 30.0, 35));
    }
}
