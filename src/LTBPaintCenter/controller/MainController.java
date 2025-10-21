package LTBPaintCenter.controller;

import LTBPaintCenter.model.*;
import LTBPaintCenter.view.MainFrame;

import javax.swing.*;
import java.util.List;


    //Main Controller, manages application flow between POS, Inventory and Monitoring
public class MainController {
    private final Inventory inventory;
    private final Report report;
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
        initializeControllers();
        initializeFrame();

        posController.getView().setCheckoutHandler(this::handleCheckout);
        frame.showPanel("POS");
        frame.setVisible(true);
    }

    private void initializeControllers() {
        posController = new POSController(inventory, report);
        inventoryController = new InventoryController(inventory);
        monitoringController = new MonitoringController(report, inventory);
    }

    private void initializeFrame() {
        frame = new MainFrame(posController, inventoryController, monitoringController);
        frame.addPanel(posController.getView(), "POS");
        frame.addPanel(inventoryController.getView(), "Inventory");
        frame.addPanel(monitoringController.getView(), "Monitoring");
    }

    private boolean handleCheckout(List<SaleItem> cart) {
        if (cart == null || cart.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Cart is empty");
            return false;
        }

        try {
            String saleId = "S" + (report.getSales().size() + 1);
            Sale sale = new Sale(saleId);

            for (SaleItem item : cart) {
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
        inventory.addProduct(new Product("P001", "Boysen - Red Acrylic", 450.0, 30, "Boysen", "Red", "Acrylic"));
        inventory.addProduct(new Product("P002", "Boysen - White Latex", 420.0, 25, "Boysen", "White", "Latex"));
        inventory.addProduct(new Product("P003", "Boysen - Green Enamel", 470.0, 15, "Boysen", "Green", "Enamel"));
        inventory.addProduct(new Product("P004", "Davies - Blue Oil", 460.0, 20, "Davies", "Blue", "Oil"));
        inventory.addProduct(new Product("P005", "Davies - Yellow Latex", 440.0, 20, "Davies", "Yellow", "Latex"));
        inventory.addProduct(new Product("P006", "Nation - Black Primer", 400.0, 18, "Nation", "Black", "Primer"));
        inventory.addProduct(new Product("P007", "Nation - Gray Enamel", 430.0, 22, "Nation", "Gray", "Enamel"));
    }
}