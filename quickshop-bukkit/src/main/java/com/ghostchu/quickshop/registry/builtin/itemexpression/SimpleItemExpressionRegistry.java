package com.ghostchu.quickshop.registry.builtin.itemexpression;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.registry.PrefixAlreadyRegisteredException;
import com.ghostchu.quickshop.api.registry.builtin.itemexpression.ItemExpressionHandler;
import com.ghostchu.quickshop.api.registry.builtin.itemexpression.ItemExpressionRegistry;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class SimpleItemExpressionRegistry implements ItemExpressionRegistry {

  private final QuickShop plugin;
  private final Set<ItemExpressionHandler> handlers = new ConcurrentSkipListSet<>();

  public SimpleItemExpressionRegistry(final QuickShop quickShop) {

    this.plugin = quickShop;
  }

  @Override
  public Set<ItemExpressionHandler> getHandlers() {

    return ImmutableSet.copyOf(handlers);
  }

  @Override
  public boolean match(final ItemStack stack, final String expression) {

    for(final ItemExpressionHandler handler : handlers) {
      if(handler.getPrefix().isBlank() || expression.startsWith(handler.getPrefix())) {
        if(handler.match(stack, StringUtils.substringAfter(expression, handler.getInternalPrefix0()))) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public void registerHandler(final ItemExpressionHandler handler) throws PrefixAlreadyRegisteredException {

    for(final ItemExpressionHandler itemExpressionHandler : handlers) {
      if(itemExpressionHandler.getPrefix().equals(handler.getPrefix())) {
        // Same plugin register request
        if(!itemExpressionHandler.getPlugin().getName().equals(handler.getPlugin().getName())) {
          throw new PrefixAlreadyRegisteredException(itemExpressionHandler.getPrefix(), itemExpressionHandler.getPlugin(), itemExpressionHandler);
        }
      }
    }
    // Unregister exists handlers
    unregisterHandler(handler);
    handlers.add(handler);
  }

  @Override
  public boolean registerHandlerSafely(final ItemExpressionHandler handler) {

    try {
      registerHandler(handler);
      return true;
    } catch(PrefixAlreadyRegisteredException e) {
      return false;
    }
  }

  @Override
  public void unregisterHandler(final ItemExpressionHandler handler) {

    handlers.remove(handler);
  }

  @Override
  public void unregisterHandler(final String prefix) {

    handlers.removeIf(h->h.getPrefix().equals(prefix));
  }

  @Override
  public void unregisterHandlers(final Plugin plugin) {

    handlers.removeIf(h->h.getPlugin().equals(plugin));
  }

}
