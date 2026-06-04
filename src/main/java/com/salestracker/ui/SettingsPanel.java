package com.salestracker.ui;

import com.salestracker.db.ShopDao;
import com.salestracker.model.Marketplace;
import com.salestracker.model.Shop;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Экран «Настройки»: привязка магазинов Wildberries и Ozon по API-ключам,
 * просмотр и удаление привязок (ФР-01 … ФР-05).
 */
public class SettingsPanel extends JPanel {

    private final ShopDao shopDao = new ShopDao();
    private final DefaultListModel<Shop> shopListModel = new DefaultListModel<>();
    private final JList<Shop> shopList = new JList<>(shopListModel);

    // Поля для Wildberries.
    private final JTextField wbName = new JTextField(18);
    private final JTextField wbKey = new JTextField(18);

    // Поля для Ozon.
    private final JTextField ozonName = new JTextField(18);
    private final JTextField ozonClientId = new JTextField(18);
    private final JTextField ozonKey = new JTextField(18);

    // Вызывается после изменения списка магазинов, чтобы обновить фильтры на других экранах.
    private final Runnable onShopsChanged;

    public SettingsPanel(Runnable onShopsChanged) {
        super(new BorderLayout(10, 10));
        this.onShopsChanged = onShopsChanged;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildShopListPanel(), BorderLayout.WEST);
        add(buildFormsPanel(), BorderLayout.CENTER);

        reloadShops();
    }

    /** Левая часть — список привязанных магазинов и кнопка удаления. */
    private JPanel buildShopListPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Привязанные магазины"));

        JScrollPane scroll = new JScrollPane(shopList);
        scroll.setPreferredSize(new java.awt.Dimension(260, 200));
        panel.add(scroll, BorderLayout.CENTER);

        JButton deleteButton = new JButton("Удалить выбранный магазин");
        deleteButton.addActionListener(e -> deleteSelected());
        panel.add(deleteButton, BorderLayout.SOUTH);

        return panel;
    }

    /** Правая часть — формы добавления магазинов. */
    private JPanel buildFormsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // --- Wildberries ---
        addSectionTitle(panel, c, row++, "Wildberries");
        addField(panel, c, row++, "Название магазина:", wbName);
        addField(panel, c, row++, "API-ключ:", wbKey);

        JButton wbButton = new JButton("Сохранить ключ Wildberries");
        wbButton.addActionListener(e -> saveWb());
        addButton(panel, c, row++, wbButton);

        // --- Ozon ---
        addSectionTitle(panel, c, row++, "Ozon");
        addField(panel, c, row++, "Название магазина:", ozonName);
        addField(panel, c, row++, "Client-Id:", ozonClientId);
        addField(panel, c, row++, "Api-Key:", ozonKey);

        JButton ozonButton = new JButton("Сохранить ключ Ozon");
        ozonButton.addActionListener(e -> saveOzon());
        addButton(panel, c, row++, ozonButton);

        return panel;
    }

    private void addSectionTitle(JPanel panel, GridBagConstraints c, int row, String text) {
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 2;
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        panel.add(label, c);
        c.gridwidth = 1;
    }

    private void addField(JPanel panel, GridBagConstraints c, int row, String label, JTextField field) {
        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        panel.add(field, c);
    }

    private void addButton(JPanel panel, GridBagConstraints c, int row, JButton button) {
        c.gridx = 1;
        c.gridy = row;
        panel.add(button, c);
    }

    /** ФР-01, ФР-02: сохранить ключ Wildberries. */
    private void saveWb() {
        String name = wbName.getText().trim();
        String key = wbKey.getText().trim();
        if (name.isEmpty() || key.isEmpty()) {
            UiUtil.showError(this, "Заполните название магазина и API-ключ Wildberries.");
            return;
        }
        Shop shop = new Shop();
        shop.setMarketplace(Marketplace.WB);
        shop.setName(name);
        shop.setApiKey(key);
        shop.setClientId(null);
        shopDao.insert(shop);

        wbName.setText("");
        wbKey.setText("");
        reloadShops();
        UiUtil.showInfo(this, "Магазин Wildberries сохранён.");
    }

    /** ФР-03, ФР-04: сохранить ключ Ozon. */
    private void saveOzon() {
        String name = ozonName.getText().trim();
        String clientId = ozonClientId.getText().trim();
        String key = ozonKey.getText().trim();
        if (name.isEmpty() || clientId.isEmpty() || key.isEmpty()) {
            UiUtil.showError(this, "Заполните название, Client-Id и Api-Key Ozon.");
            return;
        }
        Shop shop = new Shop();
        shop.setMarketplace(Marketplace.OZON);
        shop.setName(name);
        shop.setApiKey(key);
        shop.setClientId(clientId);
        shopDao.insert(shop);

        ozonName.setText("");
        ozonClientId.setText("");
        ozonKey.setText("");
        reloadShops();
        UiUtil.showInfo(this, "Магазин Ozon сохранён.");
    }

    /** ФР-05: удалить выбранный магазин вместе с его данными. */
    private void deleteSelected() {
        Shop shop = shopList.getSelectedValue();
        if (shop == null) {
            UiUtil.showError(this, "Выберите магазин в списке.");
            return;
        }
        shopDao.delete(shop.getId());
        reloadShops();
        UiUtil.showInfo(this, "Магазин удалён.");
    }

    private void reloadShops() {
        shopListModel.clear();
        for (Shop shop : shopDao.findAll()) {
            shopListModel.addElement(shop);
        }
        if (onShopsChanged != null) {
            onShopsChanged.run();
        }
    }
}
