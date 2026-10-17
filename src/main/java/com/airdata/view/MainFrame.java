package com.airdata.view;

import com.airdata.controller.AppController;
import com.airdata.controller.IDatasetListener;
import com.airdata.model.AirlineDataset;
import com.airdata.model.DatasetSummary;
import com.airdata.model.Flight;
import com.airdata.util.ThemeConstants;
import com.airdata.util.ValidationException;
import com.airdata.view.components.FilterPanel;
import com.airdata.view.components.ModernButton;
import com.airdata.view.components.VectorIcons;
import com.airdata.view.panels.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Main Application Window.
 * Features:
 * - Modern dark sidebar navigation with crystal-clear vector icons (no missing glyph boxes)
 * - Top dynamic filter & search toolbar
 * - CardLayout view container
 * - Responsive layout management
 */
public class MainFrame extends JFrame implements IDatasetListener {

    private final AppController controller;
    private final CardLayout cardLayout;
    private final JPanel cardsContainer;
    private final FilterPanel filterPanel;

    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private String currentCard = "DASHBOARD";

    public MainFrame(AppController controller) {
        super("Airline Data Visualization Software - [OOP Java Project]");
        this.controller = controller;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);

        // Root Layout
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(ThemeConstants.BG_MAIN);

        // 1. Sidebar Navigation (West)
        JPanel sidebar = createSidebar();
        rootPanel.add(sidebar, BorderLayout.WEST);

        // 2. Right Content Area (Center)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(ThemeConstants.BG_MAIN);

        // Top Filter Bar
        filterPanel = new FilterPanel(controller);
        rightPanel.add(filterPanel, BorderLayout.NORTH);

        // Cards Container
        cardLayout = new CardLayout();
        cardsContainer = new JPanel(cardLayout);
        cardsContainer.setBackground(ThemeConstants.BG_MAIN);

        // Register Panels
        cardsContainer.add(new DashboardPanel(controller), "DASHBOARD");
        cardsContainer.add(new DatasetTablePanel(controller), "DATASET");
        cardsContainer.add(new VisualizationPanel(controller), "VISUALIZATION");
        cardsContainer.add(new AnalyticsPanel(controller), "ANALYTICS");
        cardsContainer.add(new AboutPanel(), "ABOUT");

        rightPanel.add(cardsContainer, BorderLayout.CENTER);
        rootPanel.add(rightPanel, BorderLayout.CENTER);

        setContentPane(rootPanel);
        switchView("DASHBOARD");

        // Register after all child panels and filter panel are ready
        this.controller.addListener(this);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(ThemeConstants.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(15, 23, 42)));

        // Sidebar Header / Brand
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 20));
        brandPanel.setOpaque(false);

        JLabel planeIcon = new JLabel(VectorIcons.createIcon(VectorIcons.IconType.AIRPLANE, 28, new Color(56, 189, 248)));
        JPanel titleWrapper = new JPanel();
        titleWrapper.setLayout(new BoxLayout(titleWrapper, BoxLayout.Y_AXIS));
        titleWrapper.setOpaque(false);

        JLabel logoLabel = new JLabel("AirData Suite");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logoLabel.setForeground(Color.WHITE);

        JLabel subLogoLabel = new JLabel("OOP Java Analytics");
        subLogoLabel.setFont(ThemeConstants.FONT_SMALL);
        subLogoLabel.setForeground(new Color(148, 163, 184));

        titleWrapper.add(logoLabel);
        titleWrapper.add(Box.createVerticalStrut(2));
        titleWrapper.add(subLogoLabel);

        brandPanel.add(planeIcon);
        brandPanel.add(titleWrapper);
        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Nav Buttons Menu
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        addNavButton(menuPanel, "Dashboard", "DASHBOARD", VectorIcons.IconType.DASHBOARD);
        addNavButton(menuPanel, "Dataset Records", "DATASET", VectorIcons.IconType.DATASET);
        addNavButton(menuPanel, "Visualizations", "VISUALIZATION", VectorIcons.IconType.VISUALIZATION);
        addNavButton(menuPanel, "Analytics & KPIs", "ANALYTICS", VectorIcons.IconType.ANALYTICS);
        addNavButton(menuPanel, "About & OOP Info", "ABOUT", VectorIcons.IconType.ABOUT);

        sidebar.add(menuPanel, BorderLayout.CENTER);

        // Sidebar Footer
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 20, 16));

        ModernButton quickImportBtn = new ModernButton("Open CSV File", ModernButton.ButtonStyle.PRIMARY);
        quickImportBtn.setIcon(VectorIcons.createIcon(VectorIcons.IconType.IMPORT, 16, Color.WHITE));
        quickImportBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        quickImportBtn.addActionListener(e -> onQuickImport());

        JLabel versionLabel = new JLabel("Java 17 • Modern Swing • OOP");
        versionLabel.setFont(ThemeConstants.FONT_SMALL);
        versionLabel.setForeground(new Color(100, 116, 139));
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        footerPanel.add(quickImportBtn);
        footerPanel.add(Box.createVerticalStrut(12));
        footerPanel.add(versionLabel);

        sidebar.add(footerPanel, BorderLayout.SOUTH);
        return sidebar;
    }

    private void addNavButton(JPanel container, String title, String cardKey, VectorIcons.IconType iconType) {
        JButton btn = new JButton(title);
        btn.setFont(ThemeConstants.FONT_BODY_BOLD);
        btn.setForeground(ThemeConstants.TEXT_SIDEBAR);
        btn.setBackground(ThemeConstants.BG_SIDEBAR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(12);
        btn.setMargin(new Insets(10, 16, 10, 16));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Icon normalIcon = VectorIcons.createIcon(iconType, 18, ThemeConstants.TEXT_SIDEBAR);
        btn.setIcon(normalIcon);

        btn.addActionListener(e -> switchView(cardKey));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!cardKey.equals(currentCard)) {
                    btn.setBackground(ThemeConstants.BG_SIDEBAR_HOVER);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!cardKey.equals(currentCard)) {
                    btn.setBackground(ThemeConstants.BG_SIDEBAR);
                }
            }
        });

        navButtons.put(cardKey, btn);
        container.add(btn);
        container.add(Box.createVerticalStrut(6));
    }

    public void switchView(String cardKey) {
        this.currentCard = cardKey;
        cardLayout.show(cardsContainer, cardKey);

        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton b = entry.getValue();
            if (entry.getKey().equals(cardKey)) {
                b.setBackground(ThemeConstants.BG_SIDEBAR_SELECTED);
                b.setForeground(Color.WHITE);
                // Update icon to white when selected
                updateButtonIcon(entry.getKey(), b, Color.WHITE);
            } else {
                b.setBackground(ThemeConstants.BG_SIDEBAR);
                b.setForeground(ThemeConstants.TEXT_SIDEBAR);
                // Update icon to sidebar muted color when unselected
                updateButtonIcon(entry.getKey(), b, ThemeConstants.TEXT_SIDEBAR);
            }
        }
    }

    private void updateButtonIcon(String key, JButton btn, Color color) {
        VectorIcons.IconType type;
        switch (key) {
            case "DASHBOARD": type = VectorIcons.IconType.DASHBOARD; break;
            case "DATASET": type = VectorIcons.IconType.DATASET; break;
            case "VISUALIZATION": type = VectorIcons.IconType.VISUALIZATION; break;
            case "ANALYTICS": type = VectorIcons.IconType.ANALYTICS; break;
            case "ABOUT": type = VectorIcons.IconType.ABOUT; break;
            default: type = VectorIcons.IconType.DASHBOARD;
        }
        btn.setIcon(VectorIcons.createIcon(type, 18, color));
    }

    private void onQuickImport() {
        JFileChooser chooser = new JFileChooser(new File("data"));
        chooser.setDialogTitle("Select Airline CSV Dataset");
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                controller.loadDataset(file);
                JOptionPane.showMessageDialog(this,
                        "Successfully loaded " + controller.getDataset().size() + " flights from " + file.getName(),
                        "Import Successful", JOptionPane.INFORMATION_MESSAGE);
                switchView("DASHBOARD");
            } catch (ValidationException e) {
                JOptionPane.showMessageDialog(this, "Import Failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void onDatasetChanged(AirlineDataset fullDataset, List<Flight> currentViewFlights, DatasetSummary currentSummary) {
        if (filterPanel != null && fullDataset != null) {
            filterPanel.updateFilterOptions(
                    fullDataset.getUniqueAirlines(),
                    fullDataset.getUniqueOrigins(),
                    fullDataset.getUniqueDestinations()
            );
            filterPanel.syncWithCriteria(controller.getActiveCriteria());
        }
    }
}
