package com.airdata.view.panels;

import com.airdata.model.*;
import com.airdata.util.DateUtils;
import com.airdata.util.ThemeConstants;
import com.airdata.util.ValidationException;
import com.airdata.view.components.ModernButton;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Modal dialog for Creating and Editing Flight records with comprehensive field validation.
 */
public class FlightDialog extends JDialog {

    private final JTextField flightIdField = new JTextField(12);
    private final JTextField airlineField = new JTextField(15);
    private final JTextField flightNumField = new JTextField(10);
    private final JTextField originField = new JTextField(6);
    private final JTextField destField = new JTextField(6);
    private final JTextField depDateField = new JTextField(10);
    private final JTextField depTimeField = new JTextField(8);
    private final JTextField arrTimeField = new JTextField(8);
    private final JTextField durationField = new JTextField(6);
    private final JTextField distanceField = new JTextField(6);
    private final JTextField passengersField = new JTextField(6);
    private final JTextField priceField = new JTextField(8);
    private final JTextField delayField = new JTextField(6);
    private final JComboBox<FlightStatus> statusCombo = new JComboBox<>(FlightStatus.values());
    private final JTextField cancelReasonField = new JTextField(15);

    private boolean confirmed = false;
    private Flight flightResult = null;
    private final boolean isEditMode;

    public FlightDialog(Frame owner, String title, Flight flightToEdit) {
        super(owner, title, true);
        this.isEditMode = (flightToEdit != null);
        setSize(520, 560);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(ThemeConstants.BG_MAIN);

        initUI();
        if (flightToEdit != null) {
            populateFields(flightToEdit);
        } else {
            depDateField.setText(LocalDate.now().toString());
            depTimeField.setText("08:00");
            arrTimeField.setText("11:30");
            durationField.setText("210");
            distanceField.setText("1200");
            passengersField.setText("160");
            priceField.setText("250.00");
            delayField.setText("0");
        }
    }

    private void initUI() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addField(formPanel, gbc, row++, "Flight ID *:", flightIdField);
        addField(formPanel, gbc, row++, "Airline Name *:", airlineField);
        addField(formPanel, gbc, row++, "Flight Number *:", flightNumField);
        addField(formPanel, gbc, row++, "Origin IATA (e.g. JFK) *:", originField);
        addField(formPanel, gbc, row++, "Destination IATA (e.g. LAX) *:", destField);
        addField(formPanel, gbc, row++, "Departure Date (YYYY-MM-DD):", depDateField);
        addField(formPanel, gbc, row++, "Departure Time (HH:mm):", depTimeField);
        addField(formPanel, gbc, row++, "Arrival Time (HH:mm):", arrTimeField);
        addField(formPanel, gbc, row++, "Duration (Minutes):", durationField);
        addField(formPanel, gbc, row++, "Distance (Miles):", distanceField);
        addField(formPanel, gbc, row++, "Passenger Count:", passengersField);
        addField(formPanel, gbc, row++, "Ticket Price ($):", priceField);
        addField(formPanel, gbc, row++, "Delay (Minutes):", delayField);
        addField(formPanel, gbc, row++, "Flight Status:", statusCombo);
        addField(formPanel, gbc, row++, "Cancellation Reason:", cancelReasonField);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        buttonPanel.setOpaque(false);

        ModernButton saveBtn = new ModernButton(isEditMode ? "Update Flight" : "Add Flight", ModernButton.ButtonStyle.PRIMARY);
        saveBtn.addActionListener(e -> onSave());

        ModernButton cancelBtn = new ModernButton("Cancel", ModernButton.ButtonStyle.SECONDARY);
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(ThemeConstants.FONT_BODY_BOLD);
        lbl.setForeground(ThemeConstants.TEXT_DARK);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        field.setFont(ThemeConstants.FONT_BODY);
        panel.add(field, gbc);
    }

    private void populateFields(Flight f) {
        flightIdField.setText(f.getFlightId());
        flightIdField.setEnabled(false); // ID is primary key
        airlineField.setText(f.getAirline().getName());
        flightNumField.setText(f.getFlightNumber());
        originField.setText(f.getOrigin().getIataCode());
        destField.setText(f.getDestination().getIataCode());
        depDateField.setText(f.getDepartureDate() != null ? f.getDepartureDate().toString() : "");
        depTimeField.setText(f.getDepartureTime() != null ? DateUtils.formatTime(f.getDepartureTime()) : "");
        arrTimeField.setText(f.getArrivalTime() != null ? DateUtils.formatTime(f.getArrivalTime()) : "");
        durationField.setText(String.valueOf(f.getDurationMinutes()));
        distanceField.setText(String.valueOf(f.getDistanceMiles()));
        passengersField.setText(String.valueOf(f.getPassengerCount()));
        priceField.setText(String.format(java.util.Locale.US, "%.2f", f.getTicketPrice()));
        delayField.setText(String.valueOf(f.getDelayMinutes()));
        statusCombo.setSelectedItem(f.getStatus());
        cancelReasonField.setText(f.getCancellationReason());
    }

    private void onSave() {
        try {
            String flightId = flightIdField.getText().trim();
            String airlineName = airlineField.getText().trim();
            String flightNum = flightNumField.getText().trim();
            String originCode = originField.getText().trim().toUpperCase();
            String destCode = destField.getText().trim().toUpperCase();

            if (flightId.isEmpty()) throw new ValidationException("Flight ID is required.");
            if (airlineName.isEmpty()) throw new ValidationException("Airline name is required.");
            if (flightNum.isEmpty()) throw new ValidationException("Flight number is required.");
            if (originCode.isEmpty() || destCode.isEmpty()) throw new ValidationException("Origin and Destination airports are required.");

            LocalDate depDate = DateUtils.parseDate(depDateField.getText());
            LocalTime depTime = DateUtils.parseTime(depTimeField.getText());
            LocalTime arrTime = DateUtils.parseTime(arrTimeField.getText());

            int duration = Integer.parseInt(durationField.getText().trim());
            double distance = Double.parseDouble(distanceField.getText().trim());
            int passengers = Integer.parseInt(passengersField.getText().trim());
            double price = Double.parseDouble(priceField.getText().trim());
            int delay = Integer.parseInt(delayField.getText().trim());
            FlightStatus status = (FlightStatus) statusCombo.getSelectedItem();
            String cancelReason = cancelReasonField.getText().trim();

            Airline airline = new Airline(airlineName);
            Airport origin = new Airport(originCode);
            Airport dest = new Airport(destCode);
            Route route = new Route(origin, dest, distance);
            double totalRevenue = passengers * price;

            flightResult = new CommercialFlight(
                    flightId, airline, flightNum, route,
                    depDate, depTime, arrTime, duration,
                    passengers, price, totalRevenue, delay,
                    status, cancelReason, "Commercial Jet", Math.max(passengers, 200)
            );

            confirmed = true;
            dispose();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter valid numerical values for numeric fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(this, ve.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Flight getFlightResult() {
        return flightResult;
    }
}
