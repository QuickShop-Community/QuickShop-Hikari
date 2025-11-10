package com.ghostchu.quickshop.bootstrap;

import javax.swing.*;

/**
 * Java default bootstrap if user trying launch QuickShop directly
 *
 * @author Ghost_chu
 */
@SuppressWarnings("CallToPrintStackTrace")
public class Bootstrap {

  public static void main(final String[] args) {

    System.out.println("QuickShop is a Paper plugin.");
    System.out.println("You cannot directly execute this jar file, please install it as server plugin following the tutorials.");
    System.out.println("https://docs.papermc.io/paper/getting-started/.");
    if(java.awt.Desktop.isDesktopSupported()) {
      try {
        final java.net.URI uri = java.net.URI.create("https://docs.papermc.io/paper/adding-plugins/");
        final java.awt.Desktop dp = java.awt.Desktop.getDesktop();
        if(dp.isSupported(java.awt.Desktop.Action.BROWSE)) {
          createAndShowGUI(true);
          dp.browse(uri);
        } else {
          createAndShowGUI(false);
        }
      } catch(final java.io.IOException e) {
        e.printStackTrace();
      }
    } else {
      createAndShowGUI(false);
    }
  }

  private static void createAndShowGUI(final boolean supportBrowse) {

    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch(final ClassNotFoundException | UnsupportedLookAndFeelException |
                  IllegalAccessException |
                  InstantiationException e) {
      e.printStackTrace();
    }
    if(supportBrowse) {
      JOptionPane.showMessageDialog(new JPanel(), "<html><body><p>QuickShop is a Paper plugin.</p>" +
                                                  "<p>You cannot directly execute this jar file, please install it as server plugin following the <a href=\"https://docs.papermc.io/paper/adding-plugins/\">tutorials</a>." +
                                                  "<p>Press \"OK\" button to open Paper's plugin installation tutorial.</p>" +
                                                  "</body></html>", "QuickShop Alert", JOptionPane.ERROR_MESSAGE);
    } else {
      JOptionPane.showMessageDialog(new JPanel(), "<html><body><p>QuickShop is a Paper plugin.</p>" +
                                                  "<p>You cannot directly execute this jar file, please install it as server plugin following the tutorials." +
                                                  "<p>Please open the link https://docs.papermc.io/paper/adding-plugins/ in your browser to view.</p>" +
                                                  "</body></html>", "QuickShop Alert", JOptionPane.ERROR_MESSAGE);
    }
  }
}
