package com.ghostchu.quickshop.api.shop;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;

/**
 * Shop Control Manager and registry
 */
public interface ShopControlPanelManager {

  /**
   * Open ShopControlPanels for the player about specified shop
   *
   * @param player the player to open
   * @param shop   the shop to open
   */

  void openControlPanel(@NotNull Player player, @NotNull Shop shop);

  /**
   * Retrieves a map of control components associated with the shop control panel manager.
   *
   * @return A map containing control components where the key is a string identifier and the value is a ControlComponent object.
   */
  LinkedHashMap<String, ControlComponent> controlComponents();

  /**
   * Adds the provided ControlComponent to the controlComponents map associated with the Shop Control Panel Manager.
   *
   * @param component The ControlComponent to add. Must not be null.
   */
  default void addComponent(@NotNull final ControlComponent component) {

    controlComponents().put(component.identifier(), component);
  }

  /**
   * Initializes the Shop Control Panel Manager and registers necessary components.
   */
  void initialize();

  /**
   * Register a {@link ShopControlPanel} to the manager
   *
   * @param panel the panel to register
   */
  void register(@NotNull ShopControlPanel panel);

  /**
   * Unregister all {@link ShopControlPanel} from the manager that registered by specified plugin
   *
   * @param plugin the plugin to unregister
   */
  void unregister(@NotNull Plugin plugin);

  /**
   * Unregister a {@link ShopControlPanel} from the manager
   *
   * @param panel the panel to unregister
   */

  void unregister(@NotNull ShopControlPanel panel);
}
