package com.ghostchu.quickshop.api.registry.builtin.itemexpression;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public interface ItemExpressionHandler extends Comparable<ItemExpressionHandler> {

  @NotNull
  Plugin getPlugin();

  String getPrefix();

  default String getInternalPrefix0() {

    return getPrefix() + ":";
  }

  boolean match(ItemStack stack, String expression);

  @Override
  int hashCode();

  @Override
  default int compareTo(@NotNull final ItemExpressionHandler o) {

    return (getClass().getName() + getPrefix()).compareTo(o.getClass().getName() + o.getPrefix());
  }
}
