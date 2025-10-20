package LTBPaintCenter.controller;

import LTBPaintCenter.model.Inventory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;

import LTBPaintCenter.model.*;
import LTBPaintCenter.view.*;

public class MonitoringController {
    private Report report;
    private MonitoringPanel view;
    private Inventory inventory;

    public MonitoringController(Report rep, Inventory inv) {
        this.report = rep; this.inventory = inv; this.view = new MonitoringPanel();
        refresh();
    }

    public JPanel getView() { return view; }

    public void refresh() {
        DefaultTableModel m = view.model;
        int rows = m.getRowCount(); for (int i = rows-1; i>=0; --i) m.removeRow(i);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Sale s : report.getSales()) {
            StringBuilder items = new StringBuilder();
            for (SaleItem it : s.getItems()) {
                items.append(it.getName()).append(" x").append(it.getQty()).append(", ");
            }
            if (items.length() > 2) items.setLength(items.length()-2);
            m.addRow(new Object[]{s.getId(), fmt.format(s.getDate()), items.toString(), String.format("%.2f", s.getTotal())});
        }
        view.lblSummary.setText(String.format("Sales: %d | Revenue: ₱%.2f", report.getTotalSalesCount(), report.getTotalRevenue()));
    }
}
