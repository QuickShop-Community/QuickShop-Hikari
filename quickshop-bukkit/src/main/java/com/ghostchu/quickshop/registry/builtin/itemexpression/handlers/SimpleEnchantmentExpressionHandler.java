package com.ghostchu.quickshop.registry.builtin.itemexpression.handlers;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.registry.builtin.itemexpression.ItemExpressionHandler;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class SimpleEnchantmentExpressionHandler implements ItemExpressionHandler {
    private final QuickShop plugin;

    public SimpleEnchantmentExpressionHandler(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return QuickShop.getInstance().getJavaPlugin();
    }

    @Override
    public char getPrefix() {
        return '%';
    }

    @Override
    public boolean match(ItemStack stack, String expression) {
        // spilt with |
        String[] split = expression.split("\\|");
        if (split.length < 1) {
            return false;
        }
        String key = split[0];
        int minLevel = -1;
        int maxLevel;
        if (split.length > 1) {
            if (StringUtils.isNumeric(split[1])) {
                minLevel = Integer.parseInt(split[1]);
            } else {
                return false;
            }
        }
        if (split.length > 2) {
            if (StringUtils.isNumeric(split[2])) {
                maxLevel = Integer.parseInt(split[2]);
            } else {
                return false;
            }
        } else {
            maxLevel = -1;
        }
        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.fromString(key));
        if (enchantment == null) {
            return false;
        }

        int finalMinLevel = minLevel;
        int level = stack.getEnchantmentLevel(enchantment);
        if (level == 0) {
            return false;
        }
        if (finalMinLevel != -1 && level < finalMinLevel) {
            return false;
        }
        //noinspection RedundantIfStatement
        if (maxLevel != -1 && level > maxLevel) {
            return false;
        }
        return true;
    }
}
