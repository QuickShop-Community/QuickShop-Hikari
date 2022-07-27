package com.ghostchu.quickshop.localization.game.game;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class InternalGameLanguageImpl implements GameLanguage {
    private final QuickShop plugin;

    public InternalGameLanguageImpl(QuickShop plugin) {
        this.plugin = plugin;
    }

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
