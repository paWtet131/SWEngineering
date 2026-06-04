package com.salestracker.ui;

import com.salestracker.db.ShopDao;
import com.salestracker.model.Shop;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

/**
 * Компонент выбора магазинов для фильтрации отчётов (ФР-09).
 * Можно выбрать один, несколько магазинов или ничего —
 * пустой выбор означает «все магазины».
 */
public class ShopFilter extends JPanel {

    private final ShopDao shopDao = new ShopDao();
    private final DefaultListModel<Shop> model = new DefaultListModel<>();
    private final JList<Shop> list = new JList<>(model);

    public ShopFilter() {
        super(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Магазины (не выбрано = все)"));
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setVisibleRowCount(4);
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(220, 90));
        add(scroll, BorderLayout.CENTER);
        reload();
    }

    /** Перечитать список магазинов из БД (после изменений в настройках). */
    public void reload() {
        model.clear();
        for (Shop shop : shopDao.findAll()) {
            model.addElement(shop);
        }
    }

    /** Идентификаторы выбранных магазинов; пустой список — все магазины. */
    public List<Integer> getSelectedShopIds() {
        List<Integer> ids = new ArrayList<>();
        for (Shop shop : list.getSelectedValuesList()) {
            ids.add(shop.getId());
        }
        return ids;
    }
}
