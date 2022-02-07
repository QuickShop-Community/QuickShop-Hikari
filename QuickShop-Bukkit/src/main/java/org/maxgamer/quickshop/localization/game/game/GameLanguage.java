/*
 * This file is a part of project QuickShop, the name is GameLanguage.java
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

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public interface GameLanguage {
    /**
     * Getting GameLanguage impl name
     *
     * @return Impl name
     */
    @NotNull String getName();

    /**
     * Getting GameLanguage impl owned by
     *
     * @return Owned by
     */
    @NotNull Plugin getPlugin();

    /**
     * Getting a ItemStack in-game language string
     *
     * @param itemStack The ItemStack
     * @return In-game string
     */
    @NotNull String getItem(@NotNull ItemStack itemStack);

    /**
     * Getting a Material in-game language string
     *
     * @param material Material type
     * @return In-game string
     */
    @NotNull String getItem(@NotNull Material material);

    /**
     * Getting a PotionEffectType in-game language string
     *
     * @param potionEffectType The potion effect type
     * @return In-game string
     */
    @NotNull String getPotion(@NotNull PotionEffectType potionEffectType);

    /**
     * Getting a Enchantment in-game language string
     *
     * @param enchantment The Enchantment
     * @return In-game string
     */
    @NotNull String getEnchantment(@NotNull Enchantment enchantment);

    /**
     * Getting a type of Entity in-game language string
     *
     * @param entityType Type of Entity
     * @return In-game string
     */
    @NotNull String getEntity(@NotNull EntityType entityType);
}
