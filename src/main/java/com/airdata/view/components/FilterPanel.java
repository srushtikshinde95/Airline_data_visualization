package com.airdata.view.components;

import com.airdata.controller.AppController;
import com.airdata.model.FlightStatus;
import com.airdata.service.IDataFilter;
import com.airdata.util.ThemeConstants;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Set;

public class FilterPanel extends JPanel {

    private final AppController controller;
    private final JComboBox<String> airlineCombo;
    private final JComboBox<String> originCombo;
    private final JComboBox<String> destCombo;
    private final JComboBox<String> statusCombo;
    private final SearchField searchField;
    private final ModernButton applyButton;
    private final ModernButton resetButton;

    private boolean isMutingEvents = false;

    public FilterPanel(AppController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(10, 0));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftPanel.setOpaque(false);

        JLabel filterIcon = new JLabel(VectorIcons.createIcon(VectorIcons.IconType.FILTER, 16, ThemeConstants.ACCENT));
        leftPanel.add(filterIcon);

        airlineCombo = createStyledCombo(140);
        originCombo = createStyledCombo(85);
        destCombo = createStyledCombo(85);

        statusCombo = createStyledCombo(100);
        statusCombo.addItem("ALL");
        statusCombo.addItem("ON_TIME");
        statusCombo.addItem("DELAYED");
        statusCombo.addItem("CANCELLED");

        // Auto-apply listeners for immediate real-time feedback
        airlineCombo.addActionListener(e -> { if (!isMutingEvents) onApply(); });
        originCombo.addActionListener(e -> { if (!isMutingEvents) onApply(); });
        destCombo.addActionListener(e -> { if (!isMutingEvents) onApply(); });
        statusCombo.addActionListener(e -> { if (!isMutingEvents) onApply(); });

        leftPanel.add(createLabeledField("Airline:", airlineCombo));
        leftPanel.add(createLabeledField("Origin:", originCombo));
        leftPanel.add(createLabeledField("Destination:", destCombo));
        leftPanel.add(createLabeledField("Status:", statusCombo));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);

        searchField = new SearchField("Search flights...");
        searchField.setPreferredSize(new Dimension(170, 32));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { if (!isMutingEvents) onApply(); }
            public void removeUpdate(DocumentEvent e) { if (!isMutingEvents) onApply(); }
            public void changedUpdate(DocumentEvent e) { if (!isMutingEvents) onApply(); }
        });
        searchField.addActionListener(e -> onApply());

        applyButton = new ModernButton("Apply", ModernButton.ButtonStyle.PRIMARY);
        applyButton.setIcon(VectorIcons.createIcon(VectorIcons.IconType.SEARCH, 14, Color.WHITE));
        applyButton.setPreferredSize(new Dimension(95, 32));
        applyButton.addActionListener(e -> onApply());

        resetButton = new ModernButton("Reset", ModernButton.ButtonStyle.SECONDARY);
        resetButton.setIcon(VectorIcons.createIcon(VectorIcons.IconType.RESET, 14, ThemeConstants.TEXT_DARK));
        resetButton.setPreferredSize(new Dimension(90, 32));
        resetButton.addActionListener(e -> onReset());

        rightPanel.add(searchField);
        rightPanel.add(applyButton);
        rightPanel.add(resetButton);

        add(leftPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    private JComboBox<String> createStyledCombo(int width) {
        JComboBox<String> combo = new JComboBox<>(new String[]{"ALL"});
        combo.setPreferredSize(new Dimension(width, 32));
        combo.setFont(ThemeConstants.FONT_BODY);
        combo.setBackground(Color.WHITE);
        combo.setFocusable(false);
        return combo;
    }

    private JPanel createLabeledField(String labelText, JComponent component) {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        wrapper.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(ThemeConstants.FONT_SMALL.deriveFont(Font.BOLD));
        label.setForeground(ThemeConstants.TEXT_MUTED);

        wrapper.add(label);
        wrapper.add(component);
        return wrapper;
    }

    public void updateFilterOptions(Set<String> airlines, Set<String> origins, Set<String> destinations) {
        isMutingEvents = true;
        try {
            updateComboBox(airlineCombo, airlines);
            updateComboBox(originCombo, origins);
            updateComboBox(destCombo, destinations);
        } finally {
            isMutingEvents = false;
        }
    }

    private void updateComboBox(JComboBox<String> combo, Set<String> items) {
        String currentSelected = (String) combo.getSelectedItem();
        combo.removeAllItems();
        combo.addItem("ALL");
        if (items != null) {
            for (String item : items) {
                combo.addItem(item);
            }
        }
        if (currentSelected != null && containsItem(combo, currentSelected)) {
            combo.setSelectedItem(currentSelected);
        } else {
            combo.setSelectedItem("ALL");
        }
    }

    private boolean containsItem(JComboBox<String> combo, String item) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (item.equals(combo.getItemAt(i))) return true;
        }
        return false;
    }

    public void onApply() {
        IDataFilter.FilterCriteria criteria = new IDataFilter.FilterCriteria();
        criteria.setAirline((String) airlineCombo.getSelectedItem());
        criteria.setOrigin((String) originCombo.getSelectedItem());
        criteria.setDestination((String) destCombo.getSelectedItem());

        String statusStr = (String) statusCombo.getSelectedItem();
        if (statusStr != null && !statusStr.equals("ALL")) {
            criteria.setStatus(FlightStatus.valueOf(statusStr));
        }

        criteria.setSearchQuery(searchField.getText());
        controller.applyFilter(criteria);
    }

    public void syncWithCriteria(IDataFilter.FilterCriteria criteria) {
        if (criteria == null || criteria.isDefault()) {
            isMutingEvents = true;
            try {
                airlineCombo.setSelectedItem("ALL");
                originCombo.setSelectedItem("ALL");
                destCombo.setSelectedItem("ALL");
                statusCombo.setSelectedItem("ALL");
                searchField.setText("");
            } finally {
                isMutingEvents = false;
            }
        }
    }

    public void onReset() {
        isMutingEvents = true;
        try {
            airlineCombo.setSelectedItem("ALL");
            originCombo.setSelectedItem("ALL");
            destCombo.setSelectedItem("ALL");
            statusCombo.setSelectedItem("ALL");
            searchField.setText("");
        } finally {
            isMutingEvents = false;
        }
        controller.resetFilters();
    }
}
