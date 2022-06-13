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

package com.ghostchu.quickshop.platform.spigot.v1_18_1;

import com.ghostchu.quickshop.platform.Platform;
import com.ghostchu.quickshop.platform.Util;
import com.ghostchu.quickshop.platform.spigot.AbstractSpigotPlatform;
import de.tr7zw.nbtapi.NBTTileEntity;
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
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.craftbukkit.v1_18_R1.potion.CraftPotionEffectType;
import org.bukkit.craftbukkit.v1_18_R1.util.CraftMagicNumbers;
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
import java.util.Optional;
import java.util.stream.Collectors;

public class Spigot1181Platform extends AbstractSpigotPlatform implements Platform {

    public Spigot1181Platform(@NotNull Map<String, String> mapping) {
        super(mapping);
    }

    @Override
    public void setLine(@NotNull Sign sign, int line, @NotNull Component component) {
        if (super.nbtapi != null) {
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
        BinaryTagHolder holder;
        if (Util.methodExists(BinaryTagHolder.class, "binaryTagHolder")) {
            holder = BinaryTagHolder.binaryTagHolder(CraftMagicNumbers.getItem(stack.getType()).getDescriptionId());
        } else {
            //noinspection UnstableApiUsage
            holder = BinaryTagHolder.of(CraftMagicNumbers.getItem(stack.getType()).getDescriptionId());
        }
        return HoverEvent.showItem(key, stack.getAmount(), holder);
    }

    @Override
    public void registerCommand(@NotNull String prefix, @NotNull PluginCommand command) {
        ((CraftServer) Bukkit.getServer()).getCommandMap().register(prefix, command);
        ((CraftServer) Bukkit.getServer()).syncCommands();
    }

    @Override
    public @NotNull String getMinecraftVersion() {
        return ((CraftServer) Bukkit.getServer()).getServer().getServerVersion();
    }

    private String postProcessingTranslationKey(String key) {
        return this.translationMapping.getOrDefault(key, key);
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull Material material) {
        return postProcessingTranslationKey(CraftMagicNumbers.getItem(material).getDescriptionId());
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull EntityType type) {
        Optional<net.minecraft.world.entity.EntityType<?>> op = net.minecraft.world.entity.EntityType.byString(type.getKey().toString());
        if (op.isPresent()) {
            return postProcessingTranslationKey(op.get().getDescriptionId());
        } else {
            return postProcessingTranslationKey("entity." + type.getKey());
        }
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull PotionEffectType potionEffectType) {
        CraftPotionEffectType craftPotionEffectType = (CraftPotionEffectType) potionEffectType;
        return postProcessingTranslationKey(craftPotionEffectType.getHandle().getDescriptionId());
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull Enchantment enchantment) {
        return postProcessingTranslationKey(localeManager.queryEnchantments(Map.of(enchantment, 1)).getOrDefault(enchantment, "Unknown"));
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
}
