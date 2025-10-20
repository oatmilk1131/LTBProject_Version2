package LTBPaintCenter.controller;

import LTBPaintCenter.model.*;
import LTBPaintCenter.view.*;

import javax.swing.*;

public class MainController {
    private Inventory inventory;
    private Report report;
    private MainFrame mainFrame;

    private POSController posController;
    private InventoryController inventoryController;
    private MonitoringController monitoringController;

    public MainController() {
        inventory = new Inventory();
        report = new Report();
        seedData();

        mainFrame = new MainFrame();

        // controllers
        posController = new POSController(inventory, report, mainFrame.getPOSPanel());
        inventoryController = new InventoryController(inventory, mainFrame.getInventoryPanel());
        monitoringController = new MonitoringController(report, mainFrame.getMonitoringPanel());

        // initial panel
        mainFrame.showPanel("POS");

        mainFrame.setVisible(true);
    }

    private void seedData() {
        inventory.addProduct(new Product("P001", "Coke 330ml", 25.0, 50));
        inventory.addProduct(new Product("P002", "Bread Loaf", 40.0, 20));
        inventory.addProduct(new Product("P003", "Chips", 30.0, 35));
    }
}
