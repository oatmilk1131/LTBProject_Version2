package LTBPaintCenter.controller;

import LTBPaintCenter.model.Inventory;
import LTBPaintCenter.model.Product;
import LTBPaintCenter.model.ProductDAO;
import LTBPaintCenter.view.InventoryPanel;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;


    //InventoryController, handles all inventory management logic: add, update, delete, and table refresh.
public class InventoryController {
    private final Inventory inventory;
    private final InventoryPanel view;

    public InventoryController(Inventory inventory) {
        this.inventory = inventory;
        this.view = new InventoryPanel();
        attachListeners();
        refreshInventory();
    }

        private void attachListeners() {
            view.getBtnAddUpdate().addActionListener(e -> addOrUpdate());
            view.getBtnDelete().addActionListener(e -> delete());
            view.getBtnClear().addActionListener(e -> clearForm());

            //Search field + Find button
            view.getTfSearch().addActionListener(e -> performSearch());
            view.getBtnSearch().addActionListener(e -> performSearch());

            view.getTable().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int r = view.getTable().getSelectedRow();
                    if (r >= 0) {
                        view.getTfId().setText(view.getTable().getValueAt(r, 0).toString());
                        view.getTfName().setText(view.getTable().getValueAt(r, 1).toString());
                        view.getTfPrice().setText(view.getTable().getValueAt(r, 5).toString());
                        view.getTfQty().setText(view.getTable().getValueAt(r, 6).toString());
                    }
                }
            });
        }

        private void addOrUpdate() {
            String idText = view.getTfId().getText().trim();
            String name = view.getTfName().getText().trim();
            String brand = Objects.requireNonNull(view.getCbBrand().getSelectedItem()).toString();
            String color = Objects.requireNonNull(view.getCbColor().getSelectedItem()).toString();
            String type = Objects.requireNonNull(view.getCbType().getSelectedItem()).toString();

            double price;
            int qty;

            try {
                price = Double.parseDouble(view.getTfPrice().getText().trim());
                qty = Integer.parseInt(view.getTfQty().getText().trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(view, "Invalid price or quantity value.");
                return;
            }

            try {
                if (idText.isEmpty()) {
                    // --- ADD NEW PRODUCT ---
                    Product newProduct = new Product("0", name, price, qty, brand, color, type);
                    ProductDAO.addProduct(newProduct);

                    JOptionPane.showMessageDialog(view, "✅ New product added successfully!");
                } else {
                    // --- UPDATE EXISTING PRODUCT ---
                    int id = Integer.parseInt(idText);
                    Product updated = new Product(idText, name, price, qty, brand, color, type);
                    ProductDAO.updateProduct(updated);

                    JOptionPane.showMessageDialog(view, "✅ Product updated successfully!");
                }

                // --- REFRESH TABLE AFTER OPERATION ---
                view.refreshInventory(ProductDAO.getAllProducts());

                // --- Clear input fields if desired ---
                //clearFields();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(view, "❌ Database error: " + e.getMessage());
                e.printStackTrace();
            }
        }


        private void performSearch() {
            String query = view.getTfSearch().getText().trim().toLowerCase();

            // If search box is empty, show all
            if (query.isEmpty()) {
                refreshInventory();
                return;
            }

            var allProducts = inventory.getAll();
            var filtered = allProducts.stream()
                    .filter(p ->
                            p.getId().toLowerCase().contains(query) ||
                                    p.getName().toLowerCase().contains(query) ||
                                    p.getBrand().toLowerCase().contains(query) ||
                                    p.getColor().toLowerCase().contains(query) ||
                                    p.getType().toLowerCase().contains(query))
                    .toList();

            view.refreshInventory(filtered);
        }

        private void delete() {
        String id = view.getTfId().getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Select a product first");
            return;
        }

        if (inventory.getProduct(id) == null) {
            JOptionPane.showMessageDialog(view, "Product not found");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(view, "Delete this product?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            inventory.removeProduct(id);
            refreshInventory();
            JOptionPane.showMessageDialog(view, "Deleted.");
        }
    }

    public void refreshInventory() {
        view.refreshInventory(ProductDAO.getAllProducts());
    }

    public InventoryPanel getView() {
        return view;
    }

        private void clearForm() {
            view.getTfId().setText("");
            view.getTfName().setText("");
            view.getTfPrice().setText("");
            view.getTfQty().setText("");
            view.getTfSearch().setText("");
            view.getTable().clearSelection();
        }
    }