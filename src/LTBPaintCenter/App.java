package LTBPaintCenter;

import javax.swing.SwingUtilities;

import LTBPaintCenter.controller.InventoryController;
import LTBPaintCenter.controller.MainController;


public class App {
    public static void main(String[] args) {
        LTBPaintCenter.model.DatabaseSetup.initialize();
        SwingUtilities.invokeLater(() -> new MainController());
    }
}