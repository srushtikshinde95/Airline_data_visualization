package com.airdata.test;

import com.airdata.controller.AppController;
import com.airdata.view.MainFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class GuiSnapshotTest {

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "false");
        
        AppController controller = new AppController();
        controller.loadDataset(new File("data/sample_airline_data.csv"));

        File outDir = new File("test_output");
        outDir.mkdirs();

        MainFrame frame = new MainFrame(controller);
        frame.setSize(1280, 820);
        frame.addNotify();
        frame.validate();
        frame.doLayout();

        renderComponent(frame.getContentPane(), new File(outDir, "snapshot_dashboard.png"), 1280, 820);
        System.out.println("[PASS] Rendered Dashboard snapshot");

        frame.switchView("DATASET");
        frame.validate();
        renderComponent(frame.getContentPane(), new File(outDir, "snapshot_dataset.png"), 1280, 820);
        System.out.println("[PASS] Rendered Dataset snapshot");

        frame.switchView("VISUALIZATION");
        frame.validate();
        renderComponent(frame.getContentPane(), new File(outDir, "snapshot_visualizations.png"), 1280, 820);
        System.out.println("[PASS] Rendered Visualizations snapshot");

        frame.switchView("ANALYTICS");
        frame.validate();
        renderComponent(frame.getContentPane(), new File(outDir, "snapshot_analytics.png"), 1280, 820);
        System.out.println("[PASS] Rendered Analytics snapshot");

        frame.switchView("ABOUT");
        frame.validate();
        renderComponent(frame.getContentPane(), new File(outDir, "snapshot_about.png"), 1280, 820);
        System.out.println("[PASS] Rendered About snapshot");

        System.out.println("ALL SNAPSHOTS GENERATED.");
        System.exit(0);
    }

    private static void renderComponent(Container comp, File dest, int width, int height) throws Exception {
        comp.setSize(width, height);
        layoutTree(comp);
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        comp.printAll(g2);
        g2.dispose();
        ImageIO.write(img, "png", dest);
    }

    private static void layoutTree(Container c) {
        c.doLayout();
        c.validate();
        for (Component child : c.getComponents()) {
            if (child instanceof Container) {
                layoutTree((Container) child);
            }
        }
    }
}
