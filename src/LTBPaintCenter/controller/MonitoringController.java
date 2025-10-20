package LTBPaintCenter.controller;

import LTBPaintCenter.model.*;
import LTBPaintCenter.view.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.text.SimpleDateFormat;

public class MonitoringController {
    private Report report;
    private MonitoringPanel view;

    public MonitoringController(Report rep) {
        this.report = rep;
        this.view = new MonitoringPanel();
        refresh();
        attachListeners();
    }

    public JPanel getView() { return view; }

    private void attachListeners() {
        view.table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = view.table.getSelectedRow();
                    if (row >=0 ) {
                        String saleId = view.model.getValueAt(row,0).toString();
                        showSaleDetails(saleId);
                    }
                }
            }
        });
    }

    private void showSaleDetails(String saleId) {
        Sale sale = report.getSales().stream().filter(s->s.getId().equals(saleId)).findFirst().orElse(null);
        if (sale == null) return;
        new SaleDetailDialog(null, sale.getItems(), saleId).setVisible(true);
    }

    public void refresh() {
        DefaultTableModel m = view.model;
        int rows = m.getRowCount(); for(int i=rows-1;i>=0;i--) m.removeRow(i);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for(Sale s : report.getSales()) {
            m.addRow(new Object[]{s.getId(), fmt.format(s.getDate()), String.format("%.2f", s.getTotal())});
        }
        view.lblSummary.setText(String.format("Sales: %d | Revenue: ₱%.2f", report.getTotalSalesCount(), report.getTotalRevenue()));
    }
}
