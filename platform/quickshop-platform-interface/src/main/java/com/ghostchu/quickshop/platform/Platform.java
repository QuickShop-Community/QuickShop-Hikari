package com.ghostchu.quickshop.platform;

import de.tr7zw.nbtapi.NBTItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @Nullable
    default String getItemShopId(@NotNull ItemStack stack) {
        if(!Bukkit.getPluginManager().isPluginEnabled("NBTAPI")) {
            return null;
        }
        NBTItem nbtItem = new NBTItem(stack);
        String shopId = nbtItem.getString("shopId");
        if(shopId == null || shopId.isEmpty() || shopId.isBlank()) {
            return null;
        }
        return shopId;
    }
//    @NotNull
//    Component getLine(@NotNull Sign sign, int line);
}
