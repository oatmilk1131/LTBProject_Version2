package LTBPaintCenter.controller;

import LTBPaintCenter.model.*;
import LTBPaintCenter.view.InventoryPanel;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
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

            SwingUtilities.invokeLater(this::populateCombosFromDB);
        }

        private void attachListeners() {
            view.getBtnAddUpdate().addActionListener(e -> addOrUpdate());
            view.getBtnDelete().addActionListener(e -> delete());
            view.getBtnClear().addActionListener(e -> clearForm());

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

        private void populateCombosFromDB() {
            try {
                var brands = ProductDAO.getDistinct("brand");
                var colors = ProductDAO.getDistinct("color");
                var types = ProductDAO.getDistinct("type");
                // debug System.out.printf("Loaded combo values from DB: %d brands, %d colors, %d types.%n",
                //    brands.size(), colors.size(), types.size())

                view.populateCombos(brands, colors, types);
                view.revalidate();
                view.repaint();
            } catch (Exception e) {
                System.err.println("Failed to populate combos: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void addOrUpdate() {
            if (view.getTfId().getText().trim().startsWith("P")) {
                view.getTfId().setText("");
            }

            String idText = view.getTfId().getText().trim();
            String name = view.getTfName().getText().trim();

            String brand = view.getCbBrand().isEditable()
                    ? view.getCbBrand().getEditor().getItem().toString().trim()
                    : Objects.requireNonNull(view.getCbBrand().getSelectedItem()).toString();

            String color = view.getCbColor().isEditable()
                    ? view.getCbColor().getEditor().getItem().toString().trim()
                    : Objects.requireNonNull(view.getCbColor().getSelectedItem()).toString();

            String type = view.getCbType().isEditable()
                    ? view.getCbType().getEditor().getItem().toString().trim()
                    : Objects.requireNonNull(view.getCbType().getSelectedItem()).toString();

            double price;
            int qty;

            try {
                price = Double.parseDouble(view.getTfPrice().getText().trim());
                qty = Integer.parseInt(view.getTfQty().getText().trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(view, "Invalid price or quantity.");
                return;
            }

            Integer parsedId = parseIdText(idText);

            new javax.swing.SwingWorker<Void, Void>() {
                private Exception thrown;

                @Override
                protected Void doInBackground() {
                    try {
                        LookupDAO.addIfNotExists("brands", brand);
                        LookupDAO.addIfNotExists("colors", color);
                        LookupDAO.addIfNotExists("types", type);

                        Product newOrUpdatedProduct;
                        if (parsedId == null) {
                            Product newProduct = new Product(0, name, price, qty, brand, color, type);
                            int newId = ProductDAO.addProduct(newProduct);
                            newProduct.setId(newId);
                            newOrUpdatedProduct = newProduct;
                        } else {
                            Product updated = new Product(parsedId, name, price, qty, brand, color, type);
                            ProductDAO.updateProduct(updated);
                            newOrUpdatedProduct = updated;
                        }
                    } catch (Exception e) {
                        this.thrown = e;
                    }
                    return null;
                }

                @Override
                protected void done() {
                    if (thrown != null) {
                        JOptionPane.showMessageDialog(view, "Database error: " + thrown.getMessage());
                        thrown.printStackTrace();
                        return;
                    }

                    try { Thread.sleep(150); } catch (InterruptedException ignored) {}

                    List<Product> updatedList = ProductDAO.getAllProducts();
                    inventory.clear();
                    for (Product p : updatedList) inventory.addProduct(p);

                    view.refreshInventory(updatedList);

                    view.populateCombos(
                            ProductDAO.getDistinct("brand"),
                            ProductDAO.getDistinct("color"),
                            ProductDAO.getDistinct("type")
                    );

                    view.getTable().revalidate();
                    view.getTable().repaint();
                    view.getTfId().setText("");
                    JOptionPane.showMessageDialog(view, "Product saved successfully!");
                }
            }.execute();
        }

        private void performSearch() {
            String query = view.getTfSearch().getText().trim().toLowerCase();

            if (query.isEmpty()) {
                refreshInventory();
                return;
            }

            List<Product> allProducts = ProductDAO.getAllProducts();

            var filtered = allProducts.stream()
                    .filter(p ->
                            String.valueOf(p.getId()).contains(query)
                                    || p.getName().toLowerCase().contains(query)
                                    || p.getBrand().toLowerCase().contains(query)
                                    || p.getColor().toLowerCase().contains(query)
                                    || p.getType().toLowerCase().contains(query))
                    .toList();

            view.refreshInventory(filtered);
        }

        private void delete() {
            String idText = view.getTfId().getText().trim();
            if (idText.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Select a product first");
                return;
            }

            Integer parsedId = parseIdText(idText);
            if (parsedId == null) {
                JOptionPane.showMessageDialog(view, "Invalid product ID format.");
                return;
            }

            Product target = inventory.getProduct(parsedId);
            if (target == null) {
                JOptionPane.showMessageDialog(view, "Product not found");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(view, "Delete this product?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    ProductDAO.deleteProduct(parsedId);

                    inventory.removeProduct(parsedId);

                    refreshInventory();
                    JOptionPane.showMessageDialog(view, "Product deleted successfully.");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(view, "Failed to delete product: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        public void refreshInventory() {
            List<Product> all = ProductDAO.getAllProducts();
            view.refreshInventory(all);

            SwingUtilities.invokeLater(() -> {
                view.populateCombos(
                        ProductDAO.getDistinct("brand"),
                        ProductDAO.getDistinct("color"),
                        ProductDAO.getDistinct("type")
                );
                view.revalidate();
                view.repaint();
            });
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
        private Integer parseIdText(String idText) {
            if (idText == null) return null;
            idText = idText.trim();

            if (idText.isEmpty() || idText.equalsIgnoreCase("new")) return null;

            if (idText.toUpperCase().startsWith("P")) {
                idText = idText.substring(1);
            }

            try {
                int val = Integer.parseInt(idText);
                if (ProductDAO.getById(val) == null) {
                    return null;
                }
                return val;
            } catch (NumberFormatException e) {
                return null;
            }
        }

        private void clearFields() {
            view.getTfId().setText("");
            view.getTfName().setText("");
            view.getTfPrice().setText("");
            view.getTfQty().setText("");
            view.getCbBrand().setSelectedIndex(0);
            view.getCbColor().setSelectedIndex(0);
            view.getCbType().setSelectedIndex(0);
        }

    }