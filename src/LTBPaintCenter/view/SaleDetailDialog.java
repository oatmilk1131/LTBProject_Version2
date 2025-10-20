package LTBPaintCenter.view;

import LTBPaintCenter.model.SaleItem;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SaleDetailDialog extends JDialog {
    public SaleDetailDialog(Frame owner, List<SaleItem> items, String saleId) {
        super(owner, "Sale Details - " + saleId, true);
        setLayout(new BorderLayout(8,8));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (SaleItem it : items) {
            listModel.addElement(it.getName() + " x" + it.getQty() + " - ₱" + String.format("%.2f", it.getSubtotal()));
        }

        JList<String> itemList = new JList<>(listModel);
        add(new JScrollPane(itemList), BorderLayout.CENTER);

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnClose);
        add(bottom, BorderLayout.SOUTH);

        setSize(300, 400);
        setLocationRelativeTo(owner);
    }
}
