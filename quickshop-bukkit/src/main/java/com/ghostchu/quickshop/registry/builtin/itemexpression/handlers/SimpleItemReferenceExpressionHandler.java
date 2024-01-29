package com.ghostchu.quickshop.registry.builtin.itemexpression.handlers;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.registry.builtin.itemexpression.ItemExpressionHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class SimpleItemReferenceExpressionHandler implements ItemExpressionHandler {
    private final QuickShop plugin;

    public SimpleItemReferenceExpressionHandler(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return QuickShop.getInstance().getJavaPlugin();
    }

    @Override
    public char getPrefix() {
        return '@';
    }

    @Override
    public boolean match(ItemStack stack, String expression) {
        return QuickShop.getInstance().getItemMatcher().matches(stack, QuickShop.getInstance().getItemMarker().get(expression));
    }
}
