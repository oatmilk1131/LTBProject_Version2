package LTBPaintCenter.view;

import LTBPaintCenter.model.Product;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog for selecting quantity with stock limit enforcement.
 */
public class QuantityDialog extends JDialog {
    private int quantity = 1;
    private boolean confirmed = false;

    private JTextField tfQty = new JTextField("1", 3);
    private JButton btnMinus = new JButton("-");
    private JButton btnPlus = new JButton("+");
    private JButton btnYes = new JButton("Yes");
    private JButton btnNo = new JButton("No");

    private final int maxStock;

    public QuantityDialog(Product product, int maxStock) {
        this.maxStock = maxStock;

        setModal(true);
        setTitle("Add to Cart: " + product.getName());
        setSize(280, 150);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);

        // Label
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        JLabel lblInfo = new JLabel("Select quantity (Stock: " + maxStock + ")");
        add(lblInfo, gbc);

        // Quantity controls: - [text] +
        gbc.gridy = 1; gbc.gridwidth = 1;
        add(btnMinus, gbc);
        gbc.gridx = 1;
        tfQty.setHorizontalAlignment(JTextField.CENTER);
        add(tfQty, gbc);
        gbc.gridx = 2;
        add(btnPlus, gbc);

        // Yes / No buttons
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(btnYes);
        btnPanel.add(btnNo);
        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 3;
        add(btnPanel, gbc);

        // Actions
        btnMinus.addActionListener(e -> adjustQty(-1));
        btnPlus.addActionListener(e -> adjustQty(1));
        tfQty.addActionListener(e -> parseQty());
        tfQty.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent e) { parseQty(); }
        });

        btnYes.addActionListener(e -> {
            parseQty();
            confirmed = true;
            setVisible(false);
        });

        btnNo.addActionListener(e -> setVisible(false));
    }

    private void adjustQty(int delta) {
        parseQty();
        quantity += delta;
        clampQty();
        tfQty.setText(String.valueOf(quantity));
    }

    private void parseQty() {
        try {
            quantity = Integer.parseInt(tfQty.getText().trim());
        } catch (NumberFormatException e) {
            quantity = 1;
        }
        clampQty();
        tfQty.setText(String.valueOf(quantity));
    }

    private void clampQty() {
        if (quantity < 1) quantity = 1;
        if (quantity > maxStock) quantity = maxStock;
    }

    /**
     * Show the dialog and return selected quantity or null if cancelled
     */
    public Integer showDialog() {
        setVisible(true);
        return confirmed ? quantity : null;
    }
}
