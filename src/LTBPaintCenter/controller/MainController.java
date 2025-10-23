package LTBPaintCenter.controller;

import LTBPaintCenter.model.*;
import LTBPaintCenter.view.MainFrame;

import javax.swing.*;
import java.sql.SQLException;
import java.util.List;

// Main Controller — central hub connecting POS, Inventory, and Monitoring.
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

        initializeControllers();
        initializeFrame();

        seedData();

        loadProductsFromDatabase();

        posController.getView().setCheckoutHandler(this::handleCheckout);

        frame.showPanel("POS");
        frame.setVisible(true);
    }

    private void initializeControllers() {
        posController = new POSController(inventory, report);
        inventoryController = new InventoryController(inventory);
        monitoringController = new MonitoringController(report, inventory);

        Global.inventoryController = inventoryController;
        Global.posController = posController;
        Global.monitoringController = monitoringController;

        System.out.println("InventoryController view hash: " + inventoryController.getView().hashCode());
    }

    private void initializeFrame() {
        frame = new MainFrame(posController, inventoryController, monitoringController);
        frame.addPanel(posController.getView(), "POS");
        frame.addPanel(inventoryController.getView(), "Inventory");
        frame.addPanel(monitoringController.getView(), "Monitoring");
    }

    private void loadProductsFromDatabase() {
        inventory.clear();
        List<Product> dbProducts = ProductDAO.getAllProducts();
        for (Product p : dbProducts) {
            inventory.addProduct(p);
        }

        inventoryController.refreshInventory();
        posController.getView().refreshProducts(dbProducts);
        monitoringController.refresh();
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

                Product p = inventory.getProduct(item.getProductId());
                if (p != null) {
                    int newQty = Math.max(0, p.getQuantity() - item.getQty());
                    p.setQuantity(newQty);
                    ProductDAO.updateProduct(p);
                }
            }

            report.recordSale(sale);
            monitoringController.refresh();
            inventoryController.refreshInventory();

            loadProductsFromDatabase();

            JOptionPane.showMessageDialog(frame, "Sale recorded successfully!");
            return true;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Checkout failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static void seedData() {
        DatabaseSetup.initialize();

        List<Product> existing = ProductDAO.getAllProducts();
        if (!existing.isEmpty()) {
            System.out.println("Products already exist — skipping seed.");
            return;
        }

        System.out.println("Seeding default products into database...");

        try {
            Product p1 = new Product(0, "LTB Acrylic Paint Red", 149.99, 50, "LTB", "Red", "Acrylic");
            Product p2 = new Product(0, "LTB Enamel Paint Blue", 129.99, 30, "LTB", "Blue", "Enamel");
            Product p3 = new Product(0, "LTB Latex Paint White", 99.99, 40, "LTB", "White", "Latex");
            Product p4 = new Product(0, "LTB Primer Gray", 89.99, 25, "LTB", "Gray", "Primer");
            Product p5 = new Product(0, "LTB Wood Stain Walnut", 129.50, 20, "LTB", "Brown", "Wood");

            ProductDAO.addProduct(p1);
            ProductDAO.addProduct(p2);
            ProductDAO.addProduct(p3);
            ProductDAO.addProduct(p4);
            ProductDAO.addProduct(p5);

            System.out.println("Seed data inserted.");
        } catch (SQLException e) {
            System.err.println("Seeding failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}