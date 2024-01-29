package com.ghostchu.quickshop.api;

import com.ghostchu.quickshop.api.command.CommandManager;
import com.ghostchu.quickshop.api.database.DatabaseHelper;
import com.ghostchu.quickshop.api.inventory.InventoryWrapperRegistry;
import com.ghostchu.quickshop.api.localization.text.TextManager;
import com.ghostchu.quickshop.api.registry.RegistryManager;
import com.ghostchu.quickshop.api.shop.*;
import com.vdurmont.semver4j.Semver;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * The unique entry point to allow you to access most features of QuickShop
 */
public interface QuickShopAPI {

    static QuickShopAPI getInstance() {
        RegisteredServiceProvider<QuickShopProvider> provider = Bukkit.getServicesManager().getRegistration(QuickShopProvider.class);
        if (provider == null) {
            throw new IllegalStateException("QuickShop hadn't loaded at this moment.");
        }
        return provider.getProvider().getApiInstance();
    }

    static Plugin getPluginInstance() {
        RegisteredServiceProvider<QuickShopProvider> provider = Bukkit.getServicesManager().getRegistration(QuickShopProvider.class);
        if (provider == null) {
            throw new IllegalStateException("QuickShop hadn't loaded at this moment.");
        }
        return provider.getPlugin();
    }


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
    @NotNull InventoryWrapperRegistry getInventoryWrapperRegistry();

    /**
     * Getting current using ItemMatcher impl
     *
     * @return The item matcher
     */
    ItemMatcher getItemMatcher();

    /**
     * Getting the mapping of permission to shop amounts
     *
     * @return Permissions <-> Shop Amounts mapping
     * @deprecated Replaced by RankLimiter
     */
    @ApiStatus.Obsolete
    @Deprecated(forRemoval = true)
    Map<String, Integer> getLimits();

    /**
     * Getting the control panel manager
     *
     * @return Shop control panel manager
     */
    ShopControlPanelManager getShopControlPanelManager();

    /**
     * Getting Shop Manager which managing most of shops
     *
     * @return Shop manager
     */
    ShopManager getShopManager();

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

    /**
     * Getting shop limit system status
     * false if limit system is disabled
     *
     * @return Limit enabled
     * @deprecated Replaced by RankLimiter
     */
    @ApiStatus.Obsolete
    @Deprecated(forRemoval = true)
    boolean isLimit();

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
