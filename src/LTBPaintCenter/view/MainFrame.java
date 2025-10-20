package LTBPaintCenter.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MainFrame extends JFrame {
    private JPanel content = new JPanel();
    private JPanel mainPanel;
    private JPanel sidebarPanel;
    private JButton btnPOS, btnInventory, btnMonitoring;
    private JLabel profileIcon;
    private boolean isAdmin = false;
    private CardLayout cardLayout;
    private POSPanel posPanel;
    private InventoryPanel inventoryPanel;
    private MonitoringPanel monitoringPanel;

    public MainFrame() {
        setTitle("Product Management System for LTB Paint Center");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Sidebar panel
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new GridLayout(6, 1, 10, 10));
        sidebarPanel.setBackground(new Color(40, 44, 52));
        sidebarPanel.setPreferredSize(new Dimension(180, getHeight()));

        btnPOS = new JButton("POS");
        btnInventory = new JButton("Inventory");
        btnMonitoring = new JButton("Monitoring");

        styleButton(btnPOS);
        styleButton(btnInventory);
        styleButton(btnMonitoring);

        // Profile icon for admin toggle
        profileIcon = new JLabel(/*new ImageIcon(getClass().getResource("/LTBPaintCenter/assets/user_icon.png"))*/);
        profileIcon.setHorizontalAlignment(SwingConstants.CENTER);
        profileIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profileIcon.setToolTipText("Switch to Admin (Password Protected)");

        profileIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                handleAdminToggle();
            }
        });

        sidebarPanel.add(profileIcon);
        sidebarPanel.add(btnPOS);
        sidebarPanel.add(btnInventory);
        sidebarPanel.add(btnMonitoring);
        sidebarPanel.add(new JLabel()); // spacing

        add(sidebarPanel, BorderLayout.WEST);

        // Main content area
        mainPanel = new JPanel();
        cardLayout = new CardLayout();
        mainPanel.setLayout(cardLayout);

        posPanel = new POSPanel();
        inventoryPanel = new InventoryPanel();
        monitoringPanel = new MonitoringPanel();

        mainPanel.add(posPanel, "POS");
        mainPanel.add(inventoryPanel, "Inventory");
        mainPanel.add(monitoringPanel, "Monitoring");

        add(mainPanel, BorderLayout.CENTER);

        // Event listeners
        btnPOS.addActionListener(e -> showPanel("POS"));
        btnInventory.addActionListener(e -> showPanel("Inventory"));
        btnMonitoring.addActionListener(e -> showPanel("Monitoring"));

        updateAccess();
    }

    private void styleButton(JButton btn) {
        btn.setBackground(new Color(70, 73, 83));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorderPainted(false);
    }

    public void addView(JPanel panel, String name) {
        content.add(panel, name);
    }

    public void showPanel(String name) {
        if (name.equals("Inventory")) inventoryPanel.refreshInventory();
        if (name.equals("POS")) posPanel.refreshPOS();
        cardLayout.show(mainPanel, name);
    }

    private void handleAdminToggle() {
        if (!isAdmin) {
            // Prompt for password
            JPasswordField passwordField = new JPasswordField();
            int option = JOptionPane.showConfirmDialog(
                    this,
                    passwordField,
                    "Enter Admin Password:",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (option == JOptionPane.OK_OPTION) {
                String password = new String(passwordField.getPassword());
                if (password.equals("admin123")) {
                    isAdmin = true;
                    JOptionPane.showMessageDialog(this, "Admin mode activated!");
                } else {
                    JOptionPane.showMessageDialog(this, "Incorrect password.", "Access Denied", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            // Return to cashier mode
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Switch back to Cashier mode?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                isAdmin = false;
                JOptionPane.showMessageDialog(this, "Returned to Cashier mode.");
            }
        }
        updateAccess();
    }

    private void updateAccess() {
        btnInventory.setEnabled(isAdmin);
        btnInventory.setToolTipText(isAdmin ? "Inventory Management" : "Admin access required");
        btnInventory.setBackground(isAdmin ? new Color(60, 90, 160) : new Color(70, 73, 83));

        if (isAdmin) {
            profileIcon.setToolTipText("Currently in Admin Mode (Click to switch)");
        } else {
            profileIcon.setToolTipText("Switch to Admin (Password Protected)");
        }
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public POSPanel getPOSPanel() {
        return posPanel;
    }

    public InventoryPanel getInventoryPanel() {
        return inventoryPanel;
    }

    public MonitoringPanel getMonitoringPanel() {
        return monitoringPanel;
    }
}
