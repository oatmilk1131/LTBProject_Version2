package LTBPaintCenter.controller;

import LTBPaintCenter.model.Inventory;
import LTBPaintCenter.model.Product;
import LTBPaintCenter.model.Report;
import LTBPaintCenter.view.InventoryPanel;
import LTBPaintCenter.view.MainFrame;

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

        // Controllers
        posController = new POSController(inventory, report);
        inventoryController = new InventoryController(inventory);
        monitoringController = new MonitoringController(report);

        // Main frame
        frame = new MainFrame();
        frame.getPOSPanel().setAddToCartListener(posController::promptAddToCart);
        frame.getPOSPanel().setCheckoutListener(e -> posController.checkout());
        frame.getPOSPanel().setClearCartListener(e -> posController.clearCart());

        // Inject inventoryController so POS can refresh if needed
        posController.refreshPOS();

        // Add panels to frame
        // Panels are already added inside MainFrame constructor using sidebar
        // Show default panel
        frame.showPanel("POS");

        // Show frame
        frame.setVisible(true);
    }

    private void seedData() {
        inventory.addProduct(new Product("P001", "Coke 330ml", 25.0, 50));
        inventory.addProduct(new Product("P002", "Bread Loaf", 40.0, 20));
        inventory.addProduct(new Product("P003", "Chips", 30.0, 35));
    }
}
