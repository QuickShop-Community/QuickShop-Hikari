package com.ghostchu.quickshop.api.accompatibility;

import com.ghostchu.quickshop.api.QuickShopInstanceHolder;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

@Deprecated(forRemoval = true)
public abstract class AbstractQSAntiCheatCompatibilityModule extends QuickShopInstanceHolder implements AntiCheatCompatibilityModule {
    @Deprecated(forRemoval = true)
    protected AbstractQSAntiCheatCompatibilityModule(Plugin plugin) {
        super(plugin);
    }

    @Deprecated(forRemoval = true)
    @Override
    public @NotNull Plugin getPlugin() {
        return plugin;
    }
}
