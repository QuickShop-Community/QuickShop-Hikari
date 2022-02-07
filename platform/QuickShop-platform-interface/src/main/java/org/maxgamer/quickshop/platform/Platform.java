package org.maxgamer.quickshop.platform;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;

public interface Platform {
    void setLine(@NotNull Sign sign, int line, @NotNull Component component);
    @NotNull
    TranslatableComponent getItemTranslationKey(@NotNull Material material);

    void registerCommand(@NotNull String prefix, @NotNull PluginCommand command);

    boolean isServerStopping();
//    @NotNull
//    Component getLine(@NotNull Sign sign, int line);
}
