package LTBPaintCenter.controller;

import LTBPaintCenter.model.*;
import LTBPaintCenter.view.MainFrame;

import javax.swing.*;
import java.sql.*;
import java.util.List;

// Main Controller, manages application flow between POS, Inventory and Monitoring
public class MainController {
    private final Inventory inventory;
    private final Report report;
    private MainFrame frame;
    private InventoryDatabase db;

    private POSController posController;
    private InventoryController inventoryController;
    private MonitoringController monitoringController;

    public MainController() {
        inventory = new Inventory();
        report = new Report();
        Global.inventory = inventory;
        Global.report = report;

        // connect MySQL and load data
        db = new InventoryDatabase();
        db.updateExpiredProducts();

        if (isInventoryEmpty()) {
            db.insertSeedData();
        }
        loadInventoryFromDB();

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

    // ---------------------------------------------
    // REPLACEMENT for seedData()
    // ---------------------------------------------
    private void loadInventoryFromDB() {
        List<Product> products = db.loadActiveProducts();
        for (Product p : products) {
            inventory.addProduct(p);
        }
    }

    private boolean isInventoryEmpty() {
        boolean empty = true;
        String sql = "SELECT COUNT(*) FROM inventory";
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ltb_paint", "root", "admin123");
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next() && rs.getInt(1) > 0) {
                empty = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return empty;
    }
}
