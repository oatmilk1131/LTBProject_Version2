package LTBPaintCenter.controller;

import LTBPaintCenter.model.Report;
import LTBPaintCenter.model.Sale;
import LTBPaintCenter.model.SaleItem;
import LTBPaintCenter.view.MonitoringPanel;

import java.text.SimpleDateFormat;

public class MonitoringController {
    private final Report report;
    private final MonitoringPanel view;

    public MonitoringController(Report report) {
        this.report = report;
        this.view = new MonitoringPanel();
        refresh();
    }

    public void refresh() {
        view.clearTable();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Sale s : report.getSales()) {
            StringBuilder items = new StringBuilder();
            for (SaleItem it : s.getItems()) {
                items.append(it.getName()).append(" x").append(it.getQty()).append(", ");
            }
            if (items.length() > 2) items.setLength(items.length() - 2);
            view.addRow(new Object[]{s.getId(), fmt.format(s.getDate()), items.toString(), String.format("₱%.2f", s.getTotal())});
        }
        view.updateSummary(report.getTotalSalesCount(), report.getTotalRevenue());
    }

    public MonitoringPanel getView() { return view; }
}
