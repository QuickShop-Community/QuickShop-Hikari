/*
 * This file is a part of project QuickShop, the name is Bootstrap.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.javaw.bootstrap;

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
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
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
