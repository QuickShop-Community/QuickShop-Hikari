package com.ghostchu.quickshop.bootstrap;

import javax.swing.*;

/**
 * Java default bootstrap if user trying launch QuickShop directly
 *
 * @author Ghost_chu
 */
public class Bootstrap {
    public static void main(String[] args) {
        System.out.println("QuickShop is a Spigot plugin.");
        System.out.println("You cannot directly execute this jar file, please install it as server plugin following the tutorials.");
        System.out.println("https://www.spigotmc.org/wiki/spigot-installation.");
        if (java.awt.Desktop.isDesktopSupported()) {
            try {
                java.net.URI uri = java.net.URI.create("https://www.spigotmc.org/wiki/spigot-installation/#plugins");
                java.awt.Desktop dp = java.awt.Desktop.getDesktop();
                if (dp.isSupported(java.awt.Desktop.Action.BROWSE)) {
                    createAndShowGUI(true);
                    dp.browse(uri);
                } else {
                    createAndShowGUI(false);
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        } else {
            createAndShowGUI(false);
        }
    }

    private static void createAndShowGUI(boolean supportBrowse) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException |
                 InstantiationException e) {
            e.printStackTrace();
        }
        if (supportBrowse) {
            JOptionPane.showMessageDialog(new JPanel(), "<html><body><p>QuickShop is a Spigot plugin.</p>" +
                    "<p>You cannot directly execute this jar file, please install it as server plugin following the <a href=\"https://www.spigotmc.org/wiki/spigot-installation/\">tutorials</a>." +
                    "<p>Press \"OK\" button to open Spigot's plugin installation tutorial.</p>" +
                    "</body></html>", "QuickShop Alert", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(new JPanel(), "<html><body><p>QuickShop is a Spigot plugin.</p>" +
                    "<p>You cannot directly execute this jar file, please install it as server plugin following the tutorials." +
                    "<p>Please open the link https://www.spigotmc.org/wiki/spigot-installation/ in your browser to view.</p>" +
                    "</body></html>", "QuickShop Alert", JOptionPane.ERROR_MESSAGE);
        }
    }
}
