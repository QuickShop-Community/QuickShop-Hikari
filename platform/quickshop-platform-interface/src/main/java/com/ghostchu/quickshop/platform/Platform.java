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
import org.bukkit.command.PluginCommand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface Platform {
    @NotNull
    Component getDisplayName(@NotNull ItemStack stack);

    @Nullable
    Component getDisplayName(@NotNull ItemMeta meta);

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

    @NotNull
    HoverEvent<HoverEvent.ShowItem> getItemStackHoverEvent(@NotNull ItemStack stack);

    @NotNull
    Component getLine(@NotNull Sign sign, int line);

    @Nullable List<Component> getLore(@NotNull ItemStack stack);

    @Nullable List<Component> getLore(@NotNull ItemMeta meta);

    @NotNull
    String getMinecraftVersion();

    @NotNull
    Component getTranslation(@NotNull Material material);

    @NotNull
    Component getTranslation(@NotNull EntityType entity);

    @NotNull
    Component getTranslation(@NotNull PotionEffectType potionEffectType);

    @NotNull
    Component getTranslation(@NotNull Enchantment enchantment);

    @NotNull
    String getTranslationKey(@NotNull Material material);

    @NotNull
    String getTranslationKey(@NotNull EntityType entity);

    @NotNull
    String getTranslationKey(@NotNull PotionEffectType potionEffectType);

    @NotNull
    String getTranslationKey(@NotNull Enchantment enchantment);

    @NotNull
    MiniMessage miniMessage();

    void registerCommand(@NotNull String prefix, @NotNull Command command);

    void sendMessage(@NotNull CommandSender sender, @NotNull Component component);

    void setDisplayName(@NotNull ItemMeta meta, @Nullable Component component);

    void setDisplayName(@NotNull ItemStack stack, @Nullable Component component);

    void setDisplayName(@NotNull Item stack, @Nullable Component component);

    void setLine(@NotNull Sign sign, int line, @NotNull Component component);

    void setLore(@NotNull ItemStack stack, @NotNull Collection<Component> components);

    void setLore(@NotNull ItemMeta meta, @NotNull Collection<Component> components);

    void updateTranslationMappingSection(@NotNull Map<String, String> mapping);
}
