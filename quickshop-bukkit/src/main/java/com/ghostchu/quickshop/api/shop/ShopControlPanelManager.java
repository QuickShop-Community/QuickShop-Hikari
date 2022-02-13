package com.ghostchu.quickshop.api.shop;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public interface ShopControlPanelManager {
    void register(@NotNull ShopControlPanel panel);

    void unregister(@NotNull ShopControlPanel panel);

    void unregister(@NotNull Plugin plugin);

    void openControlPanel(@NotNull Player player, @NotNull Shop shop);
}
