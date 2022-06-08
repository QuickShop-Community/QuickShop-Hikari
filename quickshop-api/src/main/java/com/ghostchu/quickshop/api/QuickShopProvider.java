package com.ghostchu.quickshop.api;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public interface QuickShopProvider {
    @NotNull
    Plugin getInstance();

    @NotNull
    QuickShopAPI getApiInstance();


}
