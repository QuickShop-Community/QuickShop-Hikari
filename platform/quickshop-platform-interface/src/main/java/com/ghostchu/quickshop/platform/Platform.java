package com.ghostchu.quickshop.platform;

import de.tr7zw.nbtapi.NBTItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface Platform {
    @NotNull Component getDisplayName(@NotNull ItemStack stack);

    @Nullable Component getDisplayName(@NotNull ItemMeta meta);

    @Nullable
    default String getItemShopId(@NotNull ItemStack stack) {
        if (!Bukkit.getPluginManager().isPluginEnabled("NBTAPI")) {
            return null;
        }
        NBTItem nbtItem = new NBTItem(stack);
        String shopId = nbtItem.getString("shopId");
        if (shopId == null || shopId.isEmpty() || shopId.isBlank()) {
            return null;
        }
        return shopId;
    }

    @NotNull HoverEvent<HoverEvent.ShowItem> getItemStackHoverEvent(@NotNull ItemStack stack);

    @NotNull Component getLine(@NotNull Sign sign, int line);

    @Nullable List<Component> getLore(@NotNull ItemStack stack);

    @Nullable List<Component> getLore(@NotNull ItemMeta meta);

    @NotNull String getMinecraftVersion();

    @NotNull Component getTranslation(@NotNull Material material) throws Throwable;

    @NotNull Component getTranslation(@NotNull EntityType entity) throws Throwable;

    @NotNull Component getTranslation(@NotNull PotionEffectType potionEffectType) throws Throwable;

    @NotNull Component getTranslation(@NotNull Enchantment enchantment) throws Throwable;

    @NotNull Component getTranslation(@NotNull ItemStack itemStack) throws Throwable;

    @NotNull String getTranslationKey(@NotNull Material material) throws Throwable;

    @NotNull String getTranslationKey(@NotNull EntityType entity) throws Throwable;

    @NotNull String getTranslationKey(@NotNull PotionEffectType potionEffectType) throws Throwable;

    @NotNull String getTranslationKey(@NotNull Enchantment enchantment) throws Throwable;

    @NotNull String getTranslationKey(@NotNull ItemStack stack) throws Throwable;

    @NotNull MiniMessage miniMessage();

    void registerCommand(@NotNull String prefix, @NotNull Command command);

    void sendMessage(@NotNull CommandSender sender, @NotNull Component component);

    void sendSignTextChange(@NotNull Player player, @NotNull Sign sign, boolean glowing, @NotNull List<Component> components);

    void setDisplayName(@NotNull ItemStack stack, @Nullable Component component);

    void setDisplayName(@NotNull Item stack, @Nullable Component component);

    void setLine(@NotNull Sign sign, int line, @NotNull Component component);
    void setLines(@NotNull Sign sign, @NotNull List<Component> component);

    void setLore(@NotNull ItemStack stack, @NotNull Collection<Component> components);

    void updateTranslationMappingSection(@NotNull Map<String, String> mapping);

    @NotNull Logger getSlf4jLogger(@NotNull Plugin parent);
}
