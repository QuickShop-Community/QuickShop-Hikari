package com.ghostchu.quickshop.api.accompatibility;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

@Deprecated(forRemoval = true)
public interface AntiCheatCompatibilityModule {
    /**
     * Gets the CompatibilityModule provider name
     *
     * @return Provider name
     */
    @Deprecated(forRemoval = true)
    @NotNull String getName();

    /**
     * Gets the CompatibilityModule provider plugin instance
     *
     * @return Provider Plugin instance
     */
    @Deprecated(forRemoval = true)
    @NotNull Plugin getPlugin();

    /**
     * Calls CompatibilityModule to toggle the detection status for player between on and off
     *
     * @param player The player
     * @param status On or Off
     */
    @Deprecated(forRemoval = true)
    void toggle(@NotNull Player player, boolean status);
}
