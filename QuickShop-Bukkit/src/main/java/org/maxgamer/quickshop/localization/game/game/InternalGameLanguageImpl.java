/*
 * This file is a part of project QuickShop, the name is InternalGameLanguageImpl.java
 *  Copyright (C) PotatoCraft Studio and contributors
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

package org.maxgamer.quickshop.localization.game.game;

import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.Util;

@AllArgsConstructor
public class InternalGameLanguageImpl implements GameLanguage {
    private final QuickShop plugin;

    @Override
    public @NotNull String getName() {
        return plugin.getName();
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return plugin;
    }

    @Override
    public @NotNull String getItem(@NotNull ItemStack itemStack) {
        return Util.prettifyText(itemStack.getType().name());
    }

    @Override
    public @NotNull String getItem(@NotNull Material material) {
        return Util.prettifyText(material.name());
    }

    @Override
    public @NotNull String getPotion(@NotNull PotionEffectType potionEffectType) {
        return Util.prettifyText(potionEffectType.getName());
    }

    @Override
    public @NotNull String getEnchantment(@NotNull Enchantment enchantment) {
        return Util.prettifyText(enchantment.getKey().getKey());
    }

    @Override
    public @NotNull String getEntity(@NotNull EntityType entityType) {
        return Util.prettifyText(entityType.name());
    }
}
