package com.ghostchu.quickshop.compatibility.slimefun;

import com.ghostchu.quickshop.api.registry.builtin.itemexpression.ItemExpressionHandler;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class Main extends CompatibilityModule implements SlimefunAddon, ItemExpressionHandler {

    @Override
    public void init() {
        // There no init stuffs need to do
    }

    @NotNull
    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Override
    public String getBugTrackerURL() {
        return "https://github.com/Ghost-chu/QuickShop-Hikari/issues";
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return this;
    }

    @Override
    public String getPrefix() {
        return "slimefun";
    }

    @Override
    public boolean match(ItemStack stack, String expression) {
        Map<String, SlimefunItem> slimefunItemMap = Slimefun.getRegistry().getSlimefunItemIds();
        SlimefunItem slimefunItem = slimefunItemMap.get(expression);
        if (slimefunItem == null) {
            return false;
        }
        return slimefunItem.isItem(stack);
    }
}
