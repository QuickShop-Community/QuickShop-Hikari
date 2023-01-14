package com.ghostchu.quickshop.papi;

import com.ghostchu.quickshop.QuickShop;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuickShopPAPI extends PlaceholderExpansion {
    private final PAPIManager manager;

    public QuickShopPAPI(@NotNull QuickShop plugin) {
        manager = new PAPIManager(plugin);
    }

    @Override
    public @NotNull String getIdentifier() {
        return "qs";
    }

    @Override
    public @NotNull String getAuthor() {
        return "QuickShopBundled";
    }

    @Override
    public @NotNull String getVersion() {
        return QuickShop.getInstance().getVersion();
    }

    // Prevent the expansion being unregistered on /papi reload
    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @NotNull
    public PAPIManager getManager() {
        return manager;
    }

    @Override
    public @Nullable String onRequest(@NotNull OfflinePlayer player, @NotNull String params) {
        return manager.handle(player, params);
    }
}
