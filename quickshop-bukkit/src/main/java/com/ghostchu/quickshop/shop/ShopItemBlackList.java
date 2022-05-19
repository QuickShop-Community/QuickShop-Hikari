package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.simplereloadlib.Reloadable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ShopItemBlackList implements Reloadable {
    private final QuickShop plugin;
    private final List<Function<ItemStack, Boolean>> BLACKLIST = new ArrayList<>();

    public ShopItemBlackList(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        init();
        plugin.getReloadManager().register(this);
    }

    private void init() {
        List<String> configBlacklist = plugin.getConfig().getStringList("blacklist");
        for (String s : configBlacklist) {
            if (s.startsWith("@")) {
                String name = s.substring(1);
                ItemStack stack = plugin.getItemMarker().get(name);
                if (stack == null) {
                    Log.debug("ItemMarker not found specific name: " + name);
                    continue;
                }
                BLACKLIST.add(itemStack -> plugin.getItemMatcher().matches(stack, itemStack));
                Log.debug("Blacklisted item registered: " + name);
            } else {
                Material mat = Material.getMaterial(s.toUpperCase());
                if (mat == null) {
                    mat = Material.matchMaterial(s);
                }
                if (mat == null) {
                    plugin.getLogger().warning(s + " is not a valid material.  Check your spelling or ID");
                    continue;
                }
                Material finalMat = mat;
                BLACKLIST.add(itemStack -> itemStack.getType() == finalMat);
                Log.debug("Blacklisted material registered: " + finalMat.name());
            }
        }
    }

    /**
     * Check if an Item has been blacklisted for puchase.
     *
     * @param itemStack The ItemStack to check
     * @return true if blacklisted, false if not
     */
    public boolean isBlacklisted(@NotNull ItemStack itemStack) {
        for (Function<ItemStack, Boolean> f : BLACKLIST) {
            if (f.apply(itemStack)) {
                return true;
            }
        }
        return false;
    }
}
