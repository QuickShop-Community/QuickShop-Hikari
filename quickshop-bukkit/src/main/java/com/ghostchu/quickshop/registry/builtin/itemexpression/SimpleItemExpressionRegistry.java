package com.ghostchu.quickshop.registry.builtin.itemexpression;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.registry.builtin.itemexpression.ItemExpressionHandler;
import com.ghostchu.quickshop.api.registry.builtin.itemexpression.ItemExpressionRegistry;
import com.ghostchu.quickshop.api.registry.builtin.itemexpression.exception.PrefixAlreadyRegisteredException;
import com.google.common.collect.ImmutableSet;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class SimpleItemExpressionRegistry implements ItemExpressionRegistry {
    private final QuickShop plugin;
    private Set<ItemExpressionHandler> handlers = new ConcurrentSkipListSet<>();

    public SimpleItemExpressionRegistry(QuickShop quickShop) {
        this.plugin = quickShop;
    }

    @Override
    public Set<ItemExpressionHandler> getHandlers() {
        return ImmutableSet.copyOf(handlers);
    }

    @Override
    public boolean match(ItemStack stack, String expression){
        for (ItemExpressionHandler handler : handlers) {
            if(handler.getPrefix() == '\0' || expression.charAt(0) == handler.getPrefix()){
                if(handler.match(stack,expression.substring(1))){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void registerHandler(ItemExpressionHandler handler) throws PrefixAlreadyRegisteredException {
        if((handler.getPrefix() >= 'A' && handler.getPrefix() <= 'Z') || (handler.getPrefix() >= 'a' && handler.getPrefix() <= 'z')){
            throw new IllegalArgumentException("The alpha characters A-Z and a-z was reserved and cannot be used as prefix, but it was requested by handler "+handler.getClass().getName()+" from plugin "+handler.getPlugin().getName());
        }
        for (ItemExpressionHandler itemExpressionHandler : handlers) {
            if(itemExpressionHandler.getPrefix() == handler.getPrefix()) {
                // Same plugin register request
                if (!itemExpressionHandler.getPlugin().getName().equals(handler.getPlugin().getName())) {
                    throw new PrefixAlreadyRegisteredException(itemExpressionHandler.getPrefix(), itemExpressionHandler.getPlugin(), itemExpressionHandler);
                }
            }
        }
        // Unregister exists handlers
        unregisterHandler(handler);
        handlers.add(handler);
    }

    @Override
    public boolean registerHandlerSafely(ItemExpressionHandler handler) {
        try{
            registerHandler(handler);
            return true;
        } catch (PrefixAlreadyRegisteredException e) {
            return false;
        }
    }

    @Override
    public void unregisterHandler(ItemExpressionHandler handler) {
        handlers.remove(handler);
    }

    @Override
    public void unregisterHandler(char prefix) {
        handlers.removeIf(h->h.getPrefix() == prefix);
    }

    @Override
    public void unregisterHandlers(Plugin plugin) {
        handlers.removeIf(h->h.getPlugin().equals(plugin));
    }


}
