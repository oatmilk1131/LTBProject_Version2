package LTBPaintCenter.controller;

import LTBPaintCenter.model.*;
import LTBPaintCenter.view.*;
import javax.swing.*;

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
        seedData();

        posController = new POSController(inventory, report);
        inventoryController = new InventoryController(inventory);
        monitoringController = new MonitoringController(report, inventory);

        frame = new MainFrame();
        frame.addView(posController.getView(), "POS");
        frame.addView(inventoryController.getView(), "Inventory");
        frame.addView(monitoringController.getView(), "Monitoring");

        JMenuBar mb = frame.getAppMenuBar();
        JMenu menu = new JMenu("Navigate");
        JMenuItem miPOS = new JMenuItem("POS");
        JMenuItem miInv = new JMenuItem("Inventory");
        JMenuItem miMon = new JMenuItem("Monitoring");
        menu.add(miPOS); menu.add(miInv); menu.add(miMon);
        mb.add(menu);

        miPOS.addActionListener(e -> frame.showView("POS"));
        miInv.addActionListener(e -> frame.showView("Inventory"));
        miMon.addActionListener(e -> {
            monitoringController.refresh();
            frame.showView("Monitoring");
        });

        frame.showView("POS");
        frame.setVisible(true);
    }

    private void seedData() {
        inventory.addProduct(new Product("P001","Coke 330ml",25.0,50));
        inventory.addProduct(new Product("P002","Bread Loaf",40.0,20));
        inventory.addProduct(new Product("P003","Chips",30.0,35));
    }
}
