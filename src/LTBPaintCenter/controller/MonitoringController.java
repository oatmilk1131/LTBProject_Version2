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

        Date dateFrom = parseDateFromSelectors(
                view.getFromDay(), view.getFromMonth(), view.getFromYear()
        );
        Date dateTo = parseDateFromSelectors(
                view.getToDay(), view.getToMonth(), view.getToYear()
        );

        if (dateFrom != null && dateTo != null && dateFrom.after(dateTo)) {
            JOptionPane.showMessageDialog(view, "Invalid date range: 'From' cannot be after 'To'.");
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

    private Date parseDateFromSelectors(String day, String month, String year) {
        if (day.isBlank() || month.isBlank() || year.isBlank()) return null;

        try {
            int d = Integer.parseInt(day);
            int y = Integer.parseInt(year);
            int m = switch (month) {
                case "Jan" -> 0;
                case "Feb" -> 1;
                case "Mar" -> 2;
                case "Apr" -> 3;
                case "May" -> 4;
                case "Jun" -> 5;
                case "Jul" -> 6;
                case "Aug" -> 7;
                case "Sep" -> 8;
                case "Oct" -> 9;
                case "Nov" -> 10;
                case "Dec" -> 11;
                default -> 0;
            };

            Calendar cal = Calendar.getInstance();
            cal.set(y, m, d, 0, 0, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();
        } catch (Exception e) {
            return null;
        }
    }

    private void clearFilters() {
        // Reset brands
        if (view.getCbFilterBrand().getItemCount() > 0) {
            view.getCbFilterBrand().setSelectedIndex(0);
        }

        // Reset "From"
        if (view.getCbFromDay() != null)   view.getCbFromDay().setSelectedIndex(0);
        if (view.getCbFromMonth() != null) view.getCbFromMonth().setSelectedIndex(0);
        if (view.getCbFromYear() != null)  view.getCbFromYear().setSelectedIndex(0);

        // Reset "To"
        if (view.getCbToDay() != null)     view.getCbToDay().setSelectedIndex(0);
        if (view.getCbToMonth() != null)   view.getCbToMonth().setSelectedIndex(0);
        if (view.getCbToYear() != null)    view.getCbToYear().setSelectedIndex(0);

        // Refresh the table
        refresh();
    }
}