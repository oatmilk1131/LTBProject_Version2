package LTBPaintCenter.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MonitoringPanel extends JPanel {
    public DefaultTableModel model = new DefaultTableModel(new String[]{"Sale ID","Date","Total"}, 0);
    public JTable table = new JTable(model);
    public JLabel lblSummary = new JLabel("Sales: 0 | Revenue: ₱0.00");

    public MonitoringPanel() {
        setLayout(new BorderLayout(8,8));
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(lblSummary);
        add(bottom, BorderLayout.SOUTH);
    }
}
