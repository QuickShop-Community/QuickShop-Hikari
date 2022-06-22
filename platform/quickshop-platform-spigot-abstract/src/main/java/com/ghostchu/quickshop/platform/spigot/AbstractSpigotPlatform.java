/*
 *  This file is a part of project QuickShop, the name is SpigotPlatform.java
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

package com.ghostchu.quickshop.platform.spigot;

import com.ghostchu.quickshop.platform.Platform;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class AbstractSpigotPlatform implements Platform {
    private final Plugin plugin;
    private BukkitAudiences audience;
    protected Map<String, String> translationMapping;
    protected final Logger logger = Logger.getLogger("QuickShop-Hikari");

    public AbstractSpigotPlatform(@NotNull Plugin instance, @NotNull Map<String, String> mapping) {
        this.plugin = instance;
        this.translationMapping = mapping;
    }

    @Override
    public void setLine(@NotNull Sign sign, int line, @NotNull Component component) {
        sign.setLine(line, LegacyComponentSerializer.legacySection().serialize(component));
    }

    @Override
    public @NotNull Component getLine(@NotNull Sign sign, int line) {
        return LegacyComponentSerializer.legacySection().deserialize(sign.getLine(line));
    }

    @Override
    public abstract @NotNull HoverEvent<HoverEvent.ShowItem> getItemStackHoverEvent(@NotNull ItemStack stack);

    @Override
    public abstract void registerCommand(@NotNull String prefix, @NotNull PluginCommand command);

    @Override
    public abstract @NotNull String getMinecraftVersion();

    @NotNull
    public static String getNMSVersion() {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    private String postProcessingTranslationKey(String key) {
        return this.translationMapping.getOrDefault(key, key);
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull Material material) {
        return material.getKey().toString();
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull EntityType type) {
        return type.getKey().toString();
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull PotionEffectType potionEffectType) {
        return postProcessingTranslationKey(potionEffectType.getKey().toString());
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull Enchantment enchantment) {
        return enchantment.getKey().toString();
    }

    @Override
    public @NotNull Component getTranslation(@NotNull Material material) {
        return Component.translatable(getTranslationKey(material));
    }

    @Override
    public @NotNull Component getTranslation(@NotNull EntityType entity) {
        return Component.translatable(getTranslationKey(entity));
    }

    @Override
    public @NotNull Component getTranslation(@NotNull PotionEffectType potionEffectType) {
        return Component.translatable(getTranslationKey(potionEffectType));
    }

    @Override
    public @NotNull Component getTranslation(@NotNull Enchantment enchantment) {
        return Component.translatable(getTranslationKey(enchantment));
    }

    @Override
    public @NotNull Component getDisplayName(@NotNull ItemStack stack) {
        if (stack.getItemMeta() != null) {
            return LegacyComponentSerializer.legacySection().deserialize(stack.getItemMeta().getDisplayName());
        }
        return Component.empty();
    }

    @Override
    public @NotNull Component getDisplayName(@NotNull ItemMeta meta) {
        if (meta.hasDisplayName()) {
            return LegacyComponentSerializer.legacySection().deserialize(meta.getDisplayName());
        }
        return Component.empty();
    }

    @Override
    public void setDisplayName(@NotNull ItemMeta meta, @Nullable Component component) {
        if (component == null)
            meta.setDisplayName(null);
        else
            meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(component));
    }

    @Override
    public void setDisplayName(@NotNull ItemStack stack, @Nullable Component component) {
        if (stack.getItemMeta() == null)
            return;
        ItemMeta meta = stack.getItemMeta();
        if (component == null)
            meta.setDisplayName(null);
        else
            meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(component));
        stack.setItemMeta(meta);
    }

    @Override
    public void setDisplayName(@NotNull Item stack, @Nullable Component component) {
        if (component == null)
            stack.setCustomName(null);
        else
            stack.setCustomName(LegacyComponentSerializer.legacySection().serialize(component));
    }

    @Override
    public void updateTranslationMappingSection(@NotNull Map<String, String> mapping) {
        this.translationMapping = mapping;
    }

    @Override
    public void setLore(@NotNull ItemStack stack, @NotNull Collection<Component> components) {
        if (!stack.hasItemMeta())
            return;
        ItemMeta meta = stack.getItemMeta();
        meta.setLore(components.stream().map(LegacyComponentSerializer.legacySection()::serialize).collect(Collectors.toList()));
        stack.setItemMeta(meta);
    }

    @Override
    public void setLore(@NotNull ItemMeta meta, @NotNull Collection<Component> components) {
        meta.setLore(components.stream().map(LegacyComponentSerializer.legacySection()::serialize).collect(Collectors.toList()));
    }

    @Override
    public @Nullable List<Component> getLore(@NotNull ItemStack stack) {
        if (!stack.hasItemMeta())
            return null;
        if (!stack.getItemMeta().hasLore())
            return null;
        return stack.getItemMeta().getLore().stream().map(LegacyComponentSerializer.legacySection()::deserialize).collect(Collectors.toList());
    }

    @Override
    public @Nullable List<Component> getLore(@NotNull ItemMeta meta) {
        if (!meta.hasLore())
            return null;
        return meta.getLore().stream().map(LegacyComponentSerializer.legacySection()::deserialize).collect(Collectors.toList());
    }

    @Override
    public void sendMessage(@NotNull CommandSender sender, @NotNull Component component) {
        if (this.audience == null)
            this.audience = BukkitAudiences.create(this.plugin);
        this.audience.sender(sender).sendMessage(component);
    }

    @Override
    public @NotNull MiniMessage miniMessage() {
        return MiniMessage.miniMessage();
    }
}
