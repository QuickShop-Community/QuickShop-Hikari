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
import de.tr7zw.nbtapi.NBTTileEntity;
import de.tr7zw.nbtapi.plugin.NBTAPI;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.command.PluginCommand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.logging.Logger;

public class SpigotPlatform implements Platform {
    private NBTAPI nbtapi;
    private final ReflServerStateProvider provider;
    private Map<String, String> translationMapping;
    private final Logger logger = Logger.getLogger("QuickShop-Hikari");

    public SpigotPlatform(@NotNull Map<String, String> mapping) {
        this.provider = new ReflServerStateProvider();
        if (Bukkit.getPluginManager().isPluginEnabled("NBTAPI")) {
            if(NBTAPI.getInstance().isCompatible()) {
                nbtapi = NBTAPI.getInstance();
            }else{
                logger.warning("NBTAPI not compatible with this minecraft version, disabling NBTAPI support.");
            }
        }
        this.translationMapping = mapping;
    }

    @Override
    public void setLine(@NotNull Sign sign, int line, @NotNull Component component) {
        if (this.nbtapi != null) {
            NBTTileEntity tileSign = new NBTTileEntity(sign);
            try {
                tileSign.setString("Text" + (line + 1), GsonComponentSerializer.gson().serialize(component));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            sign.setLine(line, LegacyComponentSerializer.legacySection().serialize(component));
        }
    }

    @Override
    public @NotNull Component getLine(@NotNull Sign sign, int line) {
        return LegacyComponentSerializer.legacySection().deserialize(sign.getLine(line));
    }

    @Override
    public @NotNull HoverEvent<HoverEvent.ShowItem> getItemStackHoverEvent(@NotNull ItemStack stack) {
        NamespacedKey namespacedKey = stack.getType().getKey();
        Key key = Key.key(namespacedKey.toString());
        return HoverEvent.showItem(key, stack.getAmount(), BinaryTagHolder.of(ReflectFactory.getMaterialMinecraftNamespacedKey(stack.getType())));
    }

    @Override
    public void registerCommand(@NotNull String prefix, @NotNull PluginCommand command) {
        try {
            ReflectFactory.getCommandMap().register(prefix, command);
            ReflectFactory.syncCommands();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isServerStopping() {
        return this.provider.isStopping();
    }

    @Override
    public @NotNull String getMinecraftVersion() {
        return ReflectFactory.getServerVersion();
    }

    private String postProcessingTranslationKey(String key) {
        return this.translationMapping.getOrDefault(key, key);
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull Material material) {
        String key;
        if (!material.isBlock())
            key = "item." + material.getKey().getNamespace() + "." + material.getKey().getKey();
        else
            key = "block." + material.getKey().getNamespace() + "." + material.getKey().getKey();
        return postProcessingTranslationKey(key);
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull EntityType type) {
        String key;
        key = "entity." + type.getKey().getNamespace() + "." + type.getKey().getKey();
        return postProcessingTranslationKey(key);
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull PotionEffectType potionEffectType) {
        String key;
        key = "effect." + potionEffectType.getKey().getNamespace() + "." + potionEffectType.getKey().getKey();
        return postProcessingTranslationKey(key);
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull Enchantment enchantment) {
        String key;
        key = enchantment.getKey().getNamespace() + "." + enchantment.getKey().getKey();
        return postProcessingTranslationKey(key);
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
        if (stack.hasItemMeta()) {
            return LegacyComponentSerializer.legacySection().deserialize(stack.getItemMeta().getDisplayName());
        }
        return Component.empty();
    }

    @Override
    public void setDisplayName(@NotNull ItemStack stack, @NotNull Component component) {
        if (!stack.hasItemMeta())
            return;
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(component));
        stack.setItemMeta(meta);
    }

    @Override
    public void setDisplayName(@NotNull Item stack, @NotNull Component component) {
        stack.setCustomName(LegacyComponentSerializer.legacySection().serialize(component));
    }

    @Override
    public void updateTranslationMappingSection(@NotNull Map<String, String> mapping) {
        this.translationMapping = mapping;
    }
}
