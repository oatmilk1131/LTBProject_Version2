package LTBPaintCenter.controller;

import LTBPaintCenter.model.*;
import LTBPaintCenter.view.*;

import java.text.SimpleDateFormat;

public class MonitoringController {
    private Report report;
    private MonitoringPanel view;

    public MonitoringController(Report report, MonitoringPanel panel) {
        this.report = report;
        this.view = panel;

        refreshMonitoring();
    }

    public void refreshMonitoring() {
        view.clearTable();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (Sale s : report.getSales()) {
            StringBuilder items = new StringBuilder();
            for (SaleItem it : s.getItems()) {
                items.append(it.getName()).append(" x").append(it.getQty()).append(", ");
            }
            if (items.length() > 2) items.setLength(items.length() - 2);

            view.addRow(new Object[]{s.getId(), fmt.format(s.getDate()), items.toString(), s.getTotal()});
        }

        view.updateSummary(report.getTotalSalesCount(), report.getTotalRevenue());
    }
}
