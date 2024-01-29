package com.ghostchu.quickshop.api.registry.builtin.itemexpression;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public interface ItemExpressionHandler extends Comparable<ItemExpressionHandler> {
    @NotNull
    Plugin getPlugin();
    String getPrefix();
    boolean match(ItemStack stack, String expression);
    int hashCode();
    @Override
    default int compareTo(@NotNull ItemExpressionHandler o){
        return (getClass().getName()+getPrefix()).compareTo(o.getClass().getName()+o.getPrefix());
    }
}
