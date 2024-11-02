package com.ghostchu.quickshop.compatibility.itemsadder;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.registry.BuiltInRegistry;
import com.ghostchu.quickshop.api.registry.Registry;
import com.ghostchu.quickshop.api.registry.builtin.itemexpression.ItemExpressionHandler;
import com.ghostchu.quickshop.api.registry.builtin.itemexpression.ItemExpressionRegistry;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class Main extends CompatibilityModule implements ItemExpressionHandler {

  @Override
  public void init() {

    final Registry registry = QuickShop.getInstance().getRegistry().getRegistry(BuiltInRegistry.ITEM_EXPRESSION);
    if(registry instanceof ItemExpressionRegistry itemExpressionRegistry) {
      if(itemExpressionRegistry.registerHandlerSafely(this)) {
        getLogger().info("Register ItemsAdder ItemExpressionHandler successfully!");
      }
    }
  }


  @Override
  public @NotNull Plugin getPlugin() {

    return this;
  }

  @Override
  public String getPrefix() {

    return "itemsadder";
  }

  @Override
  public boolean match(final ItemStack stack, final String expression) {

    final CustomStack customStack = CustomStack.byItemStack(stack);
    if(customStack == null) {
      return false;
    }
    return expression.equals(customStack.getId());
  }
}
