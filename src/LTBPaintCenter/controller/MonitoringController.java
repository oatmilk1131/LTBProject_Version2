package LTBPaintCenter.controller;

import LTBPaintCenter.model.*;
import LTBPaintCenter.view.MonitoringPanel;

import javax.swing.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MonitoringController {
    private final Report report;
    private final Inventory inventory;
    private final MonitoringPanel view;
    private final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

    public MonitoringController(Report report, Inventory inventory) {
        this.report = report;
        this.inventory = inventory;
        this.view = new MonitoringPanel();

        attachListeners();
        populateBrandFilter();
        refresh();
    }

    public JPanel getView() { return view; }

    private void attachListeners() {
        view.getBtnApplyFilter().addActionListener(e -> applyFilters());
        view.getBtnClearFilter().addActionListener(e -> clearFilters());
    }

    private void populateBrandFilter() {
        Set<String> brands = new TreeSet<>();
        for (Product p : inventory.getAll()) {
            if (p.getBrand() != null && !p.getBrand().isBlank()) {
                brands.add(p.getBrand());
            }
        }
        view.populateBrandFilter(brands);
    }

    public void refresh() {
        view.refreshSales(report.getSales());
        populateBrandFilter();
    }

    // ---------------------------------------------
    // FILTER LOGIC
    // ---------------------------------------------
    private void applyFilters() {
        String selectedBrand = Objects.requireNonNull(view.getCbFilterBrand().getSelectedItem()).toString();
        String dateFromStr = view.getTfDateFrom().getText().trim();
        String dateToStr = view.getTfDateTo().getText().trim();

        Date dateFrom = null, dateTo = null;
        try {
            if (!dateFromStr.isEmpty()) dateFrom = fmt.parse(dateFromStr);
            if (!dateToStr.isEmpty()) {
                dateTo = fmt.parse(dateToStr);
                // include entire last day
                Calendar cal = Calendar.getInstance();
                cal.setTime(dateTo);
                cal.add(Calendar.DAY_OF_MONTH, 1);
                cal.add(Calendar.SECOND, -1);
                dateTo = cal.getTime();
            }
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(view, "Invalid date format. Use yyyy-MM-dd");
            return;
        }

        List<Sale> filtered = new ArrayList<>();
        for (Sale s : report.getSales()) {
            boolean withinDate = true;
            if (dateFrom != null && s.getDate().before(dateFrom)) withinDate = false;
            if (dateTo != null && s.getDate().after(dateTo)) withinDate = false;

            boolean matchesBrand = true;
            if (!selectedBrand.equals("All Brands")) {
                matchesBrand = s.getItems().stream()
                        .anyMatch(it -> {
                            Product p = inventory.getProduct(it.getProductId());
                            return p != null && selectedBrand.equalsIgnoreCase(p.getBrand());
                        });
            }

            if (withinDate && matchesBrand) filtered.add(s);
        }
        view.refreshSales(filtered);
    }

    private void clearFilters() {
        // Reset filter inputs
        view.getCbFilterBrand().setSelectedIndex(0);
        view.getTfDateFrom().setText("");
        view.getTfDateTo().setText("");

        // Refresh the table with all sales
        refresh();
    }
}