package com.ghostchu.quickshop.localization.game.game;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public interface GameLanguage {
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
     * Getting a PotionEffectType in-game language string
     *
     * @param potionEffectType The potion effect type
     * @return In-game string
     */
    @NotNull String getPotion(@NotNull PotionEffectType potionEffectType);
}
