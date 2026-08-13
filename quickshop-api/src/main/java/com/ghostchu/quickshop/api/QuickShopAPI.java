package com.ghostchu.quickshop.api;

import com.ghostchu.quickshop.api.command.CommandManager;
import com.ghostchu.quickshop.api.database.DatabaseHelper;
import com.ghostchu.quickshop.api.economy.EconomyManager;
import com.ghostchu.quickshop.api.hook.Hook;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperRegistry;
import com.ghostchu.quickshop.api.inventory.SkullProvider;
import com.ghostchu.quickshop.api.localization.text.TextManager;
import com.ghostchu.quickshop.api.registry.RegistryManager;
import com.ghostchu.quickshop.api.shop.ItemMatcher;
import com.ghostchu.quickshop.api.shop.PlayerFinder;
import com.ghostchu.quickshop.api.shop.ShopControlPanelManager;
import com.ghostchu.quickshop.api.shop.ShopItemBlackList;
import com.ghostchu.quickshop.api.shop.ShopManager;
import com.ghostchu.quickshop.api.shop.display.DisplayManager;
import com.ghostchu.quickshop.api.shop.interaction.InteractionManager;
import com.ghostchu.quickshop.api.shop.tag.TagManager;
import com.vdurmont.semver4j.Semver;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

/**
 * The unique entry point to allow you to access most features of QuickShop
 */
public interface QuickShopAPI {

  static QuickShopAPI getInstance() {

    final RegisteredServiceProvider<QuickShopProvider> provider = Bukkit.getServicesManager().getRegistration(QuickShopProvider.class);
    if(provider == null) {
      throw new IllegalStateException("QuickShop hadn't loaded at this moment.");
    }
    return provider.getProvider().getApiInstance();
  }

  static Plugin getPluginInstance() {

    final RegisteredServiceProvider<QuickShopProvider> provider = Bukkit.getServicesManager().getRegistration(QuickShopProvider.class);
    if(provider == null) {
      throw new IllegalStateException("QuickShop hadn't loaded at this moment.");
    }
    return provider.getPlugin();
  }

  /**
   * Retrieves the map of all registered hooks in the QuickShop system. Each hook
   * is identified by its unique string identifier and provides custom integration
   * capabilities for plugins or addons.
   *
   * @return a map containing hook identifiers as keys and their corresponding Hook
   *         instances as values. The map will be empty if no hooks are registered.
   */
  Map<String, Hook> hooks();

  /**
   * Iterates through all registered hooks in the QuickShop system and enables them
   * if they meet the requirements for activation. The hooks are retrieved from the
   * internal map of registered hooks, where each hook is identified by a unique string key.
   *
   * This method ensures that hooks capable of being enabled are activated, allowing
   * for custom integrations or behaviors added by plugins or addons to become functional.
   *
   * The activation of individual hooks depends on their internal logic, as determined
   * by the {@code canEnable()} method of each hook. If a hook is eligible, its {@code enable()}
   * method is invoked to update its state.
   *
   * Note: This method operates on all hooks currently available in the system through
   * the {@code hooks()} method and does not handle any hooks added or removed after its invocation.
   */
  default void loadHooks() {

    for(final Hook hook : hooks().values()) {

      if(hook.canEnable()) {
        hook.enable();
      }
    }
  }

  /**
   * Adds a new hook to the QuickShop system. The hook is identified by its unique identifier
   * and is stored in the hooks map. This allows plugins or addons to integrate with
   * QuickShop by providing custom behavior through hooks.
   *
   * @param hook the hook to be added, must not be null. The identifier of the hook must be unique.
   */
  default void addHook(final Hook hook) {

    hooks().put(hook.identifier().toLowerCase(Locale.ROOT), hook);
  }

  /**
   * Removes a hook from the QuickShop system using its unique identifier. The hook is removed
   * from the internal hooks map, which is used to manage integrations or custom behaviors added
   * to QuickShop by plugins or addons.
   *
   * @param identifier the unique identifier of the hook to be removed, must not be null.
   */
  default void removeHook(final String identifier) {

    hooks().remove(identifier.toLowerCase(Locale.ROOT));
  }

  /**
   * Retrieves the main configuration object of the QuickShop system.
   *
   * This method provides access to the QSConfig instance, which is responsible
   * for managing the configuration settings of the plugin. The returned object
   * allows for reading, updating, and saving configuration options crucial for
   * the proper functioning of the system.
   *
   * @since 6.3.0.0
   *
   * @return The QSConfig instance representing the main configuration of QuickShop.
   */
  @NotNull
  YamlDocument getConfig();

  /**
   * Getting command manager that allow addon direct access QuickShop sub-command system
   *
   * @return The command manager
   */
  CommandManager getCommandManager();

  /**
   * Getting the helper to directly access the database
   *
   * @return The database helper
   */
  DatabaseHelper getDatabaseHelper();

  /**
   * Getting this server game version
   *
   * @return Game version
   */
  GameVersion getGameVersion();

  /**
   * Gets registry of InventoryWrappers
   *
   * @return registry
   */
  @NotNull
  InventoryWrapperRegistry getInventoryWrapperRegistry();

  /**
   * Retrieves the instance of the {@code SkullProvider}, which is responsible for
   * providing methods to asynchronously generate player skulls or skull profiles.
   *
   * The {@code SkullProvider} interface offers functionality to retrieve player
   * skulls (as {@code ItemStack} objects) or skull profile information (via
   * {@code SkullProfile}), using identifiers such as UUID or player name.
   *
   * @return The {@code SkullProvider} instance for accessing skull-related utilities.
   *
   * @since 6.3.0.0
   */
  SkullProvider getSkullProvider();

  /**
   * Getting current using ItemMatcher impl
   *
   * @return The item matcher
   */
  ItemMatcher getItemMatcher();

  /**
   * Retrieves the EconomyManager instance that manages all economies associated with their unique
   * identifiers.
   *
   * @return The EconomyManager instance.
   */
  EconomyManager getEconomyManager();

  /**
   * Retrieves the TagManager associated with the QuickShop system. The TagManager
   * is responsible for handling operations related to managing tags, which may
   * include inventory item metadata and custom item tags used by the system.
   *
   * @return The TagManager instance that provides functionality for tag-related
   *         operations within the QuickShop system.
   */
  TagManager tagManager();

  /**
   * Getting the control panel manager
   *
   * @return Shop control panel manager
   */
  ShopControlPanelManager getShopControlPanelManager();

  /**
   * Retrieves the InteractionManager associated with this QuickShopProvider.
   *
   * @return The InteractionManager that manages InteractionBehaviors and InteractionTypes.
   */
  InteractionManager getInteractionManager();

  /**
   * Getting Shop Manager which managing most of shops
   *
   * @return Shop manager
   */
  ShopManager getShopManager();

  /**
   * Retrieves the instance of the DisplayManager responsible for managing display functionalities.
   *
   * @return The DisplayManager instance, which handles operations related to displays for various types of entities.
   */
  @Nullable
  DisplayManager<?> getDisplayManager();

  /**
   * Getting text manager that allow addon to create a user language locale based message
   *
   * @return The text maanger
   */
  TextManager getTextManager();

  /**
   * Getting QuickShop current stacking item support status
   *
   * @return Stacking Item support enabled
   */
  boolean isAllowStack();

  /**
   * Getting QuickShop current display item support status
   *
   * @return Display item enabled
   */
  boolean isDisplayEnabled();

  RankLimiter getRankLimiter();

  PlayerFinder getPlayerFinder();

  /**
   * Check if fee required for changing shop price
   *
   * @return requires fee
   */
  boolean isPriceChangeRequiresFee();

  /**
   * Logs a event into logs database / file
   *
   * @param eventObject event object, must can be serialized by Gson.
   */
  void logEvent(@NotNull Object eventObject);

  /**
   * Register a localized translation key mapping to another key or fixed string
   *
   * @param translationKey the key to
   * @param key            the key to map to or a fixed string
   */
  void registerLocalizedTranslationKeyMapping(@NotNull String translationKey, @NotNull String key);

  Semver getSemVersion();

  ShopItemBlackList getShopItemBlackList();

  RegistryManager getRegistry();
}
