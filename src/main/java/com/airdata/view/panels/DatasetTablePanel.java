package com.airdata.view.panels;

import com.airdata.controller.AppController;
import com.airdata.controller.IDatasetListener;
import com.airdata.model.AirlineDataset;
import com.airdata.model.DatasetSummary;
import com.airdata.model.Flight;
import com.airdata.util.DateUtils;
import com.airdata.util.FormatUtils;
import com.airdata.util.ThemeConstants;
import com.airdata.util.ValidationException;
import com.airdata.view.components.ModernButton;
import com.airdata.view.components.SearchField;
import com.airdata.view.components.StatusBadgeRenderer;
import com.airdata.view.components.VectorIcons;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DatasetTablePanel extends JPanel implements IDatasetListener {

    private final AppController controller;
    private final FlightTableModel tableModel;
    private final JTable table;
    private final JLabel statusCountLabel;
    private final SearchField quickSearchField;

    public DatasetTablePanel(AppController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(0, 12));
        setBackground(ThemeConstants.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));

        JPanel topBar = new JPanel(new BorderLayout(12, 12));
        topBar.setOpaque(false);

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftActions.setOpaque(false);

        ModernButton importBtn = new ModernButton("Import CSV", ModernButton.ButtonStyle.PRIMARY);
        importBtn.setIcon(VectorIcons.createIcon(VectorIcons.IconType.IMPORT, 14, Color.WHITE));
        importBtn.addActionListener(e -> onImportCsv());

        ModernButton exportBtn = new ModernButton("Export CSV", ModernButton.ButtonStyle.SECONDARY);
        exportBtn.setIcon(VectorIcons.createIcon(VectorIcons.IconType.EXPORT, 14, ThemeConstants.TEXT_DARK));
        exportBtn.addActionListener(e -> onExportCsv());

        ModernButton addBtn = new ModernButton("Add Flight", ModernButton.ButtonStyle.SUCCESS);
        addBtn.setIcon(VectorIcons.createIcon(VectorIcons.IconType.ADD, 14, Color.WHITE));
        addBtn.addActionListener(e -> onAddFlight());

        ModernButton editBtn = new ModernButton("Edit", ModernButton.ButtonStyle.SECONDARY);
        editBtn.setIcon(VectorIcons.createIcon(VectorIcons.IconType.EDIT, 14, ThemeConstants.TEXT_DARK));
        editBtn.addActionListener(e -> onEditFlight());

        ModernButton deleteBtn = new ModernButton("Delete", ModernButton.ButtonStyle.DANGER);
        deleteBtn.setIcon(VectorIcons.createIcon(VectorIcons.IconType.DELETE, 14, Color.WHITE));
        deleteBtn.addActionListener(e -> onDeleteFlight());

        leftActions.add(importBtn);
        leftActions.add(exportBtn);
        leftActions.add(addBtn);
        leftActions.add(editBtn);
        leftActions.add(deleteBtn);

        JPanel rightSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightSearch.setOpaque(false);

        quickSearchField = new SearchField("Filter table records...");
        quickSearchField.setPreferredSize(new Dimension(200, 34));

        quickSearchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { onSearch(); }
            public void removeUpdate(DocumentEvent e) { onSearch(); }
            public void changedUpdate(DocumentEvent e) { onSearch(); }
        });

        rightSearch.add(quickSearchField);

        topBar.add(leftActions, BorderLayout.WEST);
        topBar.add(rightSearch, BorderLayout.EAST);

        tableModel = new FlightTableModel();
        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                } else {
                    c.setBackground(new Color(224, 242, 254));
                }
                return c;
            }
        };

        table.setRowHeight(34);
        table.setFont(ThemeConstants.FONT_BODY);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(ThemeConstants.BORDER_COLOR);
        table.setShowGrid(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionBackground(new Color(224, 242, 254));
        table.setSelectionForeground(ThemeConstants.TEXT_DARK);

        JTableHeader header = table.getTableHeader();
        header.setFont(ThemeConstants.FONT_BODY_BOLD);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(ThemeConstants.TEXT_DARK);
        header.setPreferredSize(new Dimension(0, 38));

        table.getColumnModel().getColumn(13).setCellRenderer(new StatusBadgeRenderer());

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);

        TableRowSorter<FlightTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        int[] colWidths = {90, 160, 95, 75, 75, 100, 85, 85, 85, 85, 95, 95, 110, 105, 190};
        for (int i = 0; i < colWidths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeConstants.BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));

        statusCountLabel = new JLabel("Showing 0 flights");
        statusCountLabel.setFont(ThemeConstants.FONT_BODY);
        statusCountLabel.setForeground(ThemeConstants.TEXT_MUTED);
        bottomBar.add(statusCountLabel, BorderLayout.WEST);

        add(topBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

        this.controller.addListener(this);
    }

    private void onSearch() {
        String query = quickSearchField.getText();
        var criteria = controller.getActiveCriteria();
        criteria.setSearchQuery(query);
        controller.applyFilter(criteria);
    }

    private void onImportCsv() {
        JFileChooser chooser = new JFileChooser(new File("data"));
        chooser.setDialogTitle("Select Airline CSV Dataset");
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                controller.loadDataset(file);
                JOptionPane.showMessageDialog(this,
                        "Successfully imported dataset with " + controller.getDataset().size() + " flights.",
                        "Import Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (ValidationException e) {
                JOptionPane.showMessageDialog(this, "Import Failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onExportCsv() {
        JFileChooser chooser = new JFileChooser(new File("data"));
        chooser.setDialogTitle("Save Filtered Dataset to CSV");
        chooser.setSelectedFile(new File("exported_airline_data.csv"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            try {
                controller.exportData(target, true);
                JOptionPane.showMessageDialog(this,
                        "Successfully exported " + tableModel.getRowCount() + " records to " + target.getName(),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (ValidationException e) {
                JOptionPane.showMessageDialog(this, "Export Failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onAddFlight() {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        FlightDialog dialog = new FlightDialog(parent, "Add New Flight Record", null);
        dialog.setVisible(true);
        if (dialog.isConfirmed() && dialog.getFlightResult() != null) {
            try {
                controller.addFlight(dialog.getFlightResult());
                JOptionPane.showMessageDialog(this, "Flight added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (ValidationException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onEditFlight() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a flight row to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        Flight selected = tableModel.getFlightAt(modelRow);
        if (selected == null) return;

        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        FlightDialog dialog = new FlightDialog(parent, "Edit Flight - " + selected.getFlightId(), selected);
        dialog.setVisible(true);
        if (dialog.isConfirmed() && dialog.getFlightResult() != null) {
            try {
                controller.updateFlight(dialog.getFlightResult());
                JOptionPane.showMessageDialog(this, "Flight updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (ValidationException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onDeleteFlight() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a flight row to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        Flight selected = tableModel.getFlightAt(modelRow);
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete flight " + selected.getFlightId() + " (" + selected.getFlightNumber() + ")?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            controller.deleteFlight(selected.getFlightId());
        }
    }

    @Override
    public void onDatasetChanged(AirlineDataset fullDataset, List<Flight> currentViewFlights, DatasetSummary currentSummary) {
        tableModel.setFlights(currentViewFlights);
        statusCountLabel.setText("Showing " + currentViewFlights.size() + " of " + fullDataset.size() + " flights");
    }

    private static class FlightTableModel extends AbstractTableModel {
        private final String[] columns = {
                "Flight ID", "Airline", "Flight No", "Origin", "Dest",
                "Date", "Dep Time", "Arr Time", "Duration", "Distance",
                "Passengers", "Ticket Price", "Revenue", "Status", "Reason / Notes"
        };
        private final List<Flight> flights = new ArrayList<>();

        public void setFlights(List<Flight> newFlights) {
            this.flights.clear();
            if (newFlights != null) {
                this.flights.addAll(newFlights);
            }
            fireTableDataChanged();
        }

        public Flight getFlightAt(int row) {
            if (row >= 0 && row < flights.size()) {
                return flights.get(row);
            }
            return null;
        }

        @Override
        public int getRowCount() { return flights.size(); }

        @Override
        public int getColumnCount() { return columns.length; }

        @Override
        public String getColumnName(int column) { return columns[column]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Flight f = flights.get(rowIndex);
            switch (columnIndex) {
                case 0: return f.getFlightId();
                case 1: return f.getAirline().getName();
                case 2: return f.getFlightNumber();
                case 3: return f.getOrigin().getIataCode();
                case 4: return f.getDestination().getIataCode();
                case 5: return f.getDepartureDate() != null ? f.getDepartureDate().toString() : "N/A";
                case 6: return f.getDepartureTime() != null ? DateUtils.formatTime(f.getDepartureTime()) : "N/A";
                case 7: return f.getArrivalTime() != null ? DateUtils.formatTime(f.getArrivalTime()) : "N/A";
                case 8: return FormatUtils.formatDuration(f.getDurationMinutes());
                case 9: return (int) f.getDistanceMiles() + " mi";
                case 10: return FormatUtils.formatNumber(f.getPassengerCount());
                case 11: return FormatUtils.formatCurrency(f.getTicketPrice());
                case 12: return FormatUtils.formatCurrency(f.getTotalRevenue());
                case 13: return f.getStatus();
                case 14: return f.getCancellationReason().isEmpty() ? "-" : f.getCancellationReason();
                default: return "";
            }
        }
    }
}
