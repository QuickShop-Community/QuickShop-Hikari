/*
 *  This file is a part of project QuickShop, the name is Platform.java
 *  Copyright (C) Ghost_chu and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.ghostchu.quickshop.platform;

import de.tr7zw.nbtapi.NBTItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
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
    void setLine(@NotNull Sign sign, int line, @NotNull Component component);

    @NotNull
    Component getLine(@NotNull Sign sign, int line);

    @NotNull
    HoverEvent<HoverEvent.ShowItem> getItemStackHoverEvent(@NotNull ItemStack stack);

    void registerCommand(@NotNull String prefix, @NotNull PluginCommand command);


    @NotNull
    String getMinecraftVersion();

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
    String getTranslationKey(@NotNull Material material);

    @NotNull
    String getTranslationKey(@NotNull EntityType entity);

    @NotNull
    String getTranslationKey(@NotNull PotionEffectType potionEffectType);

    @NotNull
    String getTranslationKey(@NotNull Enchantment enchantment);

    @NotNull
    Component getTranslation(@NotNull Material material);

    @NotNull
    Component getTranslation(@NotNull EntityType entity);

    @NotNull
    Component getTranslation(@NotNull PotionEffectType potionEffectType);

    @NotNull
    Component getTranslation(@NotNull Enchantment enchantment);

    @NotNull
    Component getDisplayName(@NotNull ItemStack stack);

    @Nullable
    Component getDisplayName(@NotNull ItemMeta meta);

    void setDisplayName(@NotNull ItemMeta meta, @Nullable Component component);

    void setDisplayName(@NotNull ItemStack stack, @Nullable Component component);

    void setDisplayName(@NotNull Item stack, @Nullable Component component);

    void updateTranslationMappingSection(@NotNull Map<String, String> mapping);

    void setLore(@NotNull ItemStack stack, @NotNull Collection<Component> components);

    void setLore(@NotNull ItemMeta meta, @NotNull Collection<Component> components);

    @Nullable List<Component> getLore(@NotNull ItemStack stack);

    @Nullable List<Component> getLore(@NotNull ItemMeta meta);

    void sendMessage(@NotNull CommandSender sender, @NotNull Component component);
}
