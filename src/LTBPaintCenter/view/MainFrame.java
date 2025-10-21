package LTBPaintCenter.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public class MainFrame extends JFrame {
    private final JPanel mainPanel = new JPanel();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel sidebar = new JPanel(new GridLayout(6, 1, 10, 10));
    private final JButton btnPOS = new JButton("POS");
    private final JButton btnInventory = new JButton("Inventory");
    private final JButton btnMonitoring = new JButton("Monitoring");
    private final JLabel profileIcon = new JLabel("👤", SwingConstants.CENTER);

    private boolean isAdmin = false;
    private final Map<String, JPanel> panelMap = new HashMap<>();

    public MainFrame() {
        setTitle("Product Management System for LTB Paint Center");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Sidebar styling
        sidebar.setBackground(new Color(240, 240, 240));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        styleSidebarButton(btnPOS);
        styleSidebarButton(btnInventory);
        styleSidebarButton(btnMonitoring);

        profileIcon.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        profileIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        profileIcon.setToolTipText("Switch to Admin (Password Protected)");
        profileIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                handleAdminToggle();
            }
        });

        sidebar.add(profileIcon);
        sidebar.add(btnPOS);
        sidebar.add(btnInventory);
        sidebar.add(btnMonitoring);
        sidebar.add(new JLabel());
        sidebar.add(new JLabel()); // spacing

        add(sidebar, BorderLayout.WEST);

        // Main panel (CardLayout)
        mainPanel.setLayout(cardLayout);
        add(mainPanel, BorderLayout.CENTER);

        // Sidebar actions
        btnPOS.addActionListener(e -> showPanel("POS"));
        btnInventory.addActionListener(e -> showPanel("Inventory"));
        btnMonitoring.addActionListener(e -> showPanel("Monitoring"));

        updateAccess();
    }

    // -------------------------------
    // Public API
    // -------------------------------

    public void addPanel(JPanel panel, String name) {
        panelMap.put(name, panel);
        mainPanel.add(panel, name);
    }

    /** Switch panel and refresh depending on type */
    public void showPanel(String name) {
        JPanel panel = panelMap.get(name);
        if (panel == null) return;

        if (name.equals("POS") && panel instanceof POSPanel p) {
            // Ask panel to refresh products if method exists
            p.refreshProducts(LTBPaintCenter.model.Global.inventory.getAll());
        }
        if (name.equals("Inventory") && panel instanceof InventoryPanel inv) {
            inv.refreshInventory(LTBPaintCenter.model.Global.inventory.getAll());
        }
        if (name.equals("Monitoring") && panel instanceof MonitoringPanel mon) {
            mon.repaint();
        }

        cardLayout.show(mainPanel, name);
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void updateAccess() {
        btnInventory.setEnabled(isAdmin);
        btnMonitoring.setEnabled(isAdmin);

        btnInventory.setToolTipText(isAdmin ? "Inventory Management" : "Admin access required");
        btnMonitoring.setToolTipText(isAdmin ? "Sales Monitoring" : "Admin access required");

        if (isAdmin)
            profileIcon.setToolTipText("Currently in Admin Mode (Click to switch)");
        else
            profileIcon.setToolTipText("Switch to Admin (Password Protected)");
    }

    // -------------------------------
    // Private logic
    // -------------------------------

    private void styleSidebarButton(JButton b) {
        b.setFocusPainted(false);
        b.setBackground(new Color(220, 220, 220));
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMargin(new Insets(8, 16, 8, 16));

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(new Color(200, 200, 200));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(new Color(220, 220, 220));
            }
        });
    }

    private void handleAdminToggle() {
        if (!isAdmin) {
            JPasswordField pf = new JPasswordField();
            int option = JOptionPane.showConfirmDialog(this, pf, "Enter Admin Password:", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                String pass = new String(pf.getPassword());
                if (pass.equals("admin123")) {
                    isAdmin = true;
                    JOptionPane.showMessageDialog(this, "Admin mode activated!");
                } else {
                    JOptionPane.showMessageDialog(this, "Incorrect password.", "Access Denied", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            int confirm = JOptionPane.showConfirmDialog(this, "Switch back to Cashier mode?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                isAdmin = false;
                JOptionPane.showMessageDialog(this, "Returned to Cashier mode.");
            }
        }
        updateAccess();
    }
}
