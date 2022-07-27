package com.ghostchu.quickshop.papi;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.logger.Log;
import lombok.Getter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuickShopPAPI extends PlaceholderExpansion {
    
    @Getter
    private final PAPICache papiCache = new PAPICache();

    @Override
    public boolean canRegister() {
        return true;
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
        return QuickShop.getVersion();
    }
    
    // Prevent the expansion being unregistered on /papi reload
    @Override
    public boolean persist(){
        return true;
    }
    
    @Override
    public @Nullable String onRequest(@NotNull OfflinePlayer player, @NotNull String params) {
        String cached = papiCache.readCache(player.getUniqueId(), params);
        if (cached != null) {
            Log.debug("Processing cached placeholder: " + params);
            return cached;
        }
        String[] args = params.split("_");
        if (args.length < 1) {
            return null;
        }
        
        return papiCache.getCached(player.getUniqueId(), args);
    }
}
