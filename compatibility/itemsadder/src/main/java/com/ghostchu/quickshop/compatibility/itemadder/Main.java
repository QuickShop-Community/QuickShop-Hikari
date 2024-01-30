package com.ghostchu.quickshop.compatibility.itemadder;

import com.ghostchu.quickshop.api.registry.builtin.itemexpression.ItemExpressionHandler;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class Main extends CompatibilityModule implements ItemExpressionHandler {

    @Override
    public void init() {
        // There no init stuffs need to do
    }


    @Override
    public @NotNull Plugin getPlugin() {
        return this;
    }

    @Override
    public String getPrefix() {
        return "itemadder";
    }

    @Override
    public boolean match(ItemStack stack, String expression) {
        CustomStack customStack = CustomStack.byItemStack(stack);
        if(customStack == null){
            return false;
        }
        return expression.equals(customStack.getId());
    }
}
