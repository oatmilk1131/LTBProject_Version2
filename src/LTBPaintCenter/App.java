package LTBPaintCenter;

import javax.swing.SwingUtilities;
import LTBPaintCenter.controller.MainController;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainController());
    }
}
