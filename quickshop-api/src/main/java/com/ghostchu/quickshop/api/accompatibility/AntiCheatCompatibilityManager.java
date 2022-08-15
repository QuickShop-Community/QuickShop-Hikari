package com.ghostchu.quickshop.api.accompatibility;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Manager that managing all registered compatibility for anti-cheat modules
 */
@Deprecated(forRemoval = true)
public interface AntiCheatCompatibilityManager {
    /**
     * Check a module registered
     *
     * @param moduleName Module name
     * @return Is registered
     */
    @Deprecated(forRemoval = true)
    boolean isRegistered(String moduleName);

    /**
     * Register compatibility module
     *
     * @param module Compatibility module
     */
    @Deprecated(forRemoval = true)
    void register(@NotNull AntiCheatCompatibilityModule module);

    /**
     * Switch the compatibility mode on or off, set false to disable all we known incompatiable plugin
     * listener, set true to enable back all disabled plugin liseners. WIP
     *
     * @param status true=turn on closed listeners, false=turn off all turned on listeners.
     * @param player The player to check the listeners
     */
    @Deprecated(forRemoval = true)
    void toggleProtectionListeners(boolean status, @NotNull Player player);

    /**
     * Unregister a registered compatibility modules
     *
     * @param moduleName Compatibility module name
     */
    @Deprecated(forRemoval = true)
    void unregister(@NotNull String moduleName);

    /**
     * Unregister a registered compatibility modules
     *
     * @param module Compatibility module
     */
    @Deprecated(forRemoval = true)
    void unregister(@NotNull AntiCheatCompatibilityModule module);

    /**
     * Unregister all registered compatibility modules
     */
    @Deprecated(forRemoval = true)
    void unregisterAll();
}
