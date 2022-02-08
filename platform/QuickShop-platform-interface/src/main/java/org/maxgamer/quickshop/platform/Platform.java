package org.maxgamer.quickshop.platform;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface Platform {
    void setLine(@NotNull Sign sign, int line, @NotNull Component component);
    @NotNull
    Component getLine(@NotNull Sign sign, int line);
    @NotNull
    TranslatableComponent getItemTranslationKey(@NotNull Material material);
    @NotNull
    HoverEvent<HoverEvent.ShowItem> getItemStackHoverEvent(@NotNull ItemStack stack);
    void registerCommand(@NotNull String prefix, @NotNull PluginCommand command);
    boolean isServerStopping();
    @NotNull
    String getMinecraftVersion();
//    @NotNull
//    Component getLine(@NotNull Sign sign, int line);
}
