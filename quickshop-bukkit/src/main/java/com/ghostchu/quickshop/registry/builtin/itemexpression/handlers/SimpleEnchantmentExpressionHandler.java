package com.ghostchu.quickshop.registry.builtin.itemexpression.handlers;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.registry.builtin.itemexpression.ItemExpressionHandler;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class SimpleEnchantmentExpressionHandler implements ItemExpressionHandler {

  private final QuickShop plugin;

  public SimpleEnchantmentExpressionHandler(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public @NotNull Plugin getPlugin() {

    return QuickShop.getInstance().getJavaPlugin();
  }

  @Override
  public String getPrefix() {

    return "%";
  }

  @Override
  public String getInternalPrefix0() {

    return getPrefix();
  }

  @Override
  public boolean match(final ItemStack stack, final String expression) {
    // spilt with |
    final String[] split = expression.split("\\|");
    if(split.length < 1) {
      return false;
    }
    final String key = split[0];
    int minLevel = -1;
    final int maxLevel;
    if(split.length > 1) {
      if(StringUtils.isNumeric(split[1])) {
        minLevel = Integer.parseInt(split[1]);
      } else {
        return false;
      }
    }
    if(split.length > 2) {
      if(StringUtils.isNumeric(split[2])) {
        maxLevel = Integer.parseInt(split[2]);
      } else {
        return false;
      }
    } else {
      maxLevel = -1;
    }
    final Enchantment enchantment = Enchantment.getByKey(NamespacedKey.fromString(key));
    if(enchantment == null) {
      return false;
    }

    int level = 0;
    // This is required for enchantment books as they store enchantments differently for some reason.
    // We'll check both stored enchantments and regular enchantments, as it is possible to "enchant"
    // an enchanted book with an enchantment, rather than adding a stored enchantment.
    // Some plugins may do this by accident.
    if(stack.getItemMeta() instanceof EnchantmentStorageMeta meta) {
      level = meta.getStoredEnchantLevel(enchantment);
    }
    if(level == 0) {
      level = stack.getEnchantmentLevel(enchantment);
    }

    if(level == 0) {
      return false;
    }

    if(minLevel != -1 && level < minLevel) {
      return false;
    }
    //noinspection RedundantIfStatement
    if(maxLevel != -1 && level > maxLevel) {
      return false;
    }
    return true;
  }
}
