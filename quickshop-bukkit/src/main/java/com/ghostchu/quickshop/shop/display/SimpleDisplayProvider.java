package com.ghostchu.quickshop.shop.display;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.shop.DisplayProvider;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleDisplayProvider implements DisplayProvider {
    @Override
    public @NotNull Plugin getProvider() {
        return QuickShop.getInstance();
    }

    @Override
    public @Nullable AbstractDisplayItem provide(@NotNull Shop shop) {
        return null;
    }
}
