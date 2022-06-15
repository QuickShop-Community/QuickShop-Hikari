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

package com.ghostchu.quickshop.platform.spigot.v1_18_2;

import com.ghostchu.quickshop.platform.Platform;
import com.ghostchu.quickshop.platform.Util;
import com.ghostchu.quickshop.platform.spigot.AbstractSpigotPlatform;
import de.tr7zw.nbtapi.NBTTileEntity;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.platform.bukkit.MinecraftComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.command.PluginCommand;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_18_R2.potion.CraftPotionEffectType;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public class Spigot1182Platform extends AbstractSpigotPlatform implements Platform {

    public Spigot1182Platform(@NotNull Plugin plugin, @NotNull Map<String, String> mapping) {
        super(plugin, mapping);
    }

    @Override
    public void setLine(@NotNull Sign sign, int line, @NotNull Component component) {
        if (super.nbtapi != null) {
            NBTTileEntity tileSign = new NBTTileEntity(sign);
            try {
                tileSign.setObject("Text" + (line + 1), MinecraftComponentSerializer.get().serialize(component));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            sign.setLine(line, LegacyComponentSerializer.legacySection().serialize(component));
        }
    }

    @Override
    public @NotNull HoverEvent<HoverEvent.ShowItem> getItemStackHoverEvent(@NotNull ItemStack stack) {
        NamespacedKey namespacedKey = stack.getType().getKey();
        Key key = Key.key(namespacedKey.toString());
        BinaryTagHolder holder;
        if (Util.methodExists(BinaryTagHolder.class, "binaryTagHolder")) {
            holder = BinaryTagHolder.binaryTagHolder(CraftItemStack.asNMSCopy(stack).save(new CompoundTag()).toString());
        } else {
            //noinspection UnstableApiUsage
            holder = BinaryTagHolder.of(CraftItemStack.asNMSCopy(stack).save(new CompoundTag()).toString());
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
        if (material.isBlock()) {
            return CraftMagicNumbers.getBlock(material).getDescriptionId();
        } else {
            return postProcessingTranslationKey(CraftMagicNumbers.getItem(material).getDescriptionId());
        }
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
}
