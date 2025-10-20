package LTBPaintCenter;

import javax.swing.SwingUtilities;
import LTBPaintCenter.controller.MainController;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainController());
    }
}
