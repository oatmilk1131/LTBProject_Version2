package LTBPaintCenter.controller;

import LTBPaintCenter.model.Inventory;
import LTBPaintCenter.model.Product;
import LTBPaintCenter.model.ProductDAO;
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
                JOptionPane.showMessageDialog(view, "Invalid price or quantity.");
                return;
            }

            // parse id: allow display format like "P001" or just "1"
            Integer parsedId = parseIdText(idText);

            // run DB operations off the EDT
            new javax.swing.SwingWorker<Void, Void>() {
                private Exception thrown;

                @Override
                protected Void doInBackground() {
                    try {
                        if (parsedId == null) {
                            // add new
                            Product newProduct = new Product(0, name, price, qty, brand, color, type);
                            int newId = ProductDAO.addProduct(newProduct); // returns generated id
                            newProduct.setId(newId);
                        } else {
                            // update existing
                            Product updated = new Product(parsedId, name, price, qty, brand, color, type);
                            ProductDAO.updateProduct(updated);
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

                    // refresh UI from DB
                    view.refreshInventory(ProductDAO.getAllProducts());

                    // set id field to newly created ID display if it was an insert
                    if (parsedId == null) {
                        // pick the last product inserted (safe because we set generated ID to model in DAO)
                        List<Product> all = ProductDAO.getAllProducts();
                        if (!all.isEmpty()) {
                            Product last = all.get(all.size() - 1);
                            view.getTfId().setText(last.getDisplayId());
                        }
                    } else {
                        view.getTfId().setText(String.format("P%03d", parsedId));
                    }

                    clearFields();
                    JOptionPane.showMessageDialog(view, "Product saved!");
                }
            }.execute();
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
                            String.valueOf(p.getId()).contains(query)
                                    ||
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

        if (inventory.getProduct(Integer.parseInt(id)) == null) {
            JOptionPane.showMessageDialog(view, "Product not found");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(view, "Delete this product?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            inventory.removeProduct(Integer.parseInt(id));
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
        private Integer parseIdText(String idText) {
            if (idText == null || idText.isBlank()) return null;
            String digits = idText.replaceAll("\\D+", "");
            if (digits.isEmpty()) return null;
            try {
                return Integer.parseInt(digits);
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