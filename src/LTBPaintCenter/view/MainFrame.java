package LTBPaintCenter.view;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel content = new JPanel(cardLayout);
    private JMenuBar menuBar = new JMenuBar();

    public MainFrame() {
        setTitle("Vibe POS - Inventory - Monitoring");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        setJMenuBar(menuBar);
        add(content);
    }

    public void addView(JPanel panel, String name) { content.add(panel, name); }
    public void showView(String name) { cardLayout.show(content, name); }
    public JMenuBar getAppMenuBar() { return menuBar; }
}
