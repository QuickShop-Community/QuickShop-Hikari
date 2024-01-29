package com.ghostchu.quickshop.registry.builtin.itemexpression.handlers;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.registry.builtin.itemexpression.ItemExpressionHandler;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class SimpleMaterialExpressionHandler implements ItemExpressionHandler {
    private final QuickShop plugin;

    public SimpleMaterialExpressionHandler(QuickShop plugin){
        this.plugin = plugin;
    }
    @Override
    public @NotNull Plugin getPlugin() {
        return QuickShop.getInstance().getJavaPlugin();
    }

    @Override
    public char getPrefix() {
        return '\0'; // \0 mean no prefix, only should only be used in quickshop internal
    }

    @Override
    public boolean match(ItemStack stack, String expression) {
        return stack.getType().equals(Material.matchMaterial(expression));
    }
}
