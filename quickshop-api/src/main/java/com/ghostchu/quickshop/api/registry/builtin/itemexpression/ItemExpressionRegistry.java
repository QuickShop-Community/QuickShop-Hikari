package com.ghostchu.quickshop.api.registry.builtin.itemexpression;

import com.ghostchu.quickshop.api.registry.Registry;
import com.ghostchu.quickshop.api.registry.builtin.itemexpression.exception.PrefixAlreadyRegisteredException;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Set;

public interface ItemExpressionRegistry extends Registry {
    Set<ItemExpressionHandler> getHandlers();

    boolean match(ItemStack stack, String expression);

    void registerHandler(ItemExpressionHandler handler) throws PrefixAlreadyRegisteredException;
    boolean registerHandlerSafely(ItemExpressionHandler handler);

    void unregisterHandler(ItemExpressionHandler handler);

    void unregisterHandler(char prefix);

    void unregisterHandlers(Plugin plugin);
}
