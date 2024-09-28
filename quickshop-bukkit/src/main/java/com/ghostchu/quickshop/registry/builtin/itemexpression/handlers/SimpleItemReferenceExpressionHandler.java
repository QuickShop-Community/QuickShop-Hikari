package com.ghostchu.quickshop.registry.builtin.itemexpression.handlers;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.registry.builtin.itemexpression.ItemExpressionHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class SimpleItemReferenceExpressionHandler implements ItemExpressionHandler {

  private final QuickShop plugin;

  public SimpleItemReferenceExpressionHandler(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public @NotNull Plugin getPlugin() {

    return QuickShop.getInstance().getJavaPlugin();
  }

  @Override
  public String getPrefix() {

    return "@";
  }

  @Override
  public String getInternalPrefix0() {

    return getPrefix();
  }

  @Override
  public boolean match(final ItemStack stack, final String expression) {

    return QuickShop.getInstance().getItemMatcher().matches(stack, QuickShop.getInstance().getItemMarker().get(expression));
  }
}
