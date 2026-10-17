package com.airdata.main;

import com.airdata.controller.AppController;
import com.airdata.view.MainFrame;

import javax.swing.*;

/**
 * Application Entry Point.
 * Initializes the AppController, loads the default dataset, and creates the GUI on the Event Dispatch Thread (EDT).
 */
public class AirlineApp {

    public static void main(String[] args) {
        // Set cross-platform look and feel with high DPI support
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fallback gracefully to default Swing theme
        }

        SwingUtilities.invokeLater(() -> {
            AppController controller = new AppController();
            MainFrame mainFrame = new MainFrame(controller);
            
            // Load bundled sample dataset on initial launch
            controller.loadDefaultDataset();

            mainFrame.setVisible(true);
        });
    }
}
