package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.ShopItemBlackList;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.paste.item.SubPasteItem;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.Reloadable;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SimpleShopItemBlackList implements Reloadable, ShopItemBlackList, SubPasteItem {
    private final QuickShop plugin;
    private final List<Function<ItemStack, Boolean>> BLACKLIST = new ArrayList<>();

    public SimpleShopItemBlackList(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        init();
        plugin.getReloadManager().register(this);
    }

    private void init() {
        BLACKLIST.clear();
        List<String> configBlacklist = plugin.getConfig().getStringList("blacklist");
        for (String s : configBlacklist) {
            if (s.startsWith("@")) {
                parseItemReference(s);
            } else if (s.startsWith("%")) {
                parseEnchantment(s);
            } else {
                parseItemMaterial(s);
            }
        }
    }

    private void parseItemReference(@NotNull String s) {
        String name = s.substring(1);
        ItemStack stack = plugin.getItemMarker().get(name);
        if (stack == null) {
            Log.debug("ItemMarker not found specific name: " + name);
            return;
        }
        BLACKLIST.add(itemStack -> plugin.getItemMatcher().matches(stack, itemStack));
        Log.debug("Blacklisted item registered: " + name);
    }

    private void parseEnchantment(@NotNull String s) {
        // minecraft:sharpness|min|max
        String input = s.substring(1);
        // spilt with |
        String[] split = input.split("\\|");
        if (split.length < 1) {
            plugin.logger().warn(s + " is not a valid enchantment.  Check your spelling or ID");
            return;
        }
        String key = split[0];
        int minLevel = -1;
        int maxLevel;
        if (split.length > 1) {
            if (StringUtils.isNumeric(split[1])) {
                minLevel = Integer.parseInt(split[1]);
            } else {
                plugin.logger().warn("{} enchantment syntax error. Skipping...", s);
                return;
            }
        }
        if (split.length > 2) {
            if (StringUtils.isNumeric(split[2])) {
                maxLevel = Integer.parseInt(split[2]);
            } else {
                maxLevel = -1;
                plugin.logger().warn("{} enchantment syntax error. Skipping...", s);
                return;
            }
        } else {
            maxLevel = -1;
        }
        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.fromString(key));
        if (enchantment == null) {
            plugin.logger().warn("{} is not a valid enchantment namespaced key. Skipping...", key);
            return;
        }

        int finalMinLevel = minLevel;
        BLACKLIST.add((itemStack) -> {
            int level = itemStack.getEnchantmentLevel(enchantment);
            if (level == 0) {
                return false;
            }
            if (finalMinLevel != -1 && level < finalMinLevel) {
                return false;
            }
            //noinspection RedundantIfStatement
            if (maxLevel != -1 && level > maxLevel) {
                return false;
            }
            return true;
        });
    }

    private void parseItemMaterial(@NotNull String s) {
        Material mat = Material.getMaterial(s.toUpperCase());
        if (mat == null) {
            mat = Material.matchMaterial(s);
        }
        if (mat == null) {
            plugin.logger().warn(s + " is not a valid material.  Check your spelling or ID");
            return;
        }
        Material finalMat = mat;
        BLACKLIST.add(itemStack -> itemStack.getType() == finalMat);
        Log.debug("Blacklisted material registered: " + finalMat.name());
    }

    /**
     * Check if an Item has been blacklisted for puchase.
     *
     * @param itemStack The ItemStack to check
     * @return true if blacklisted, false if not
     */
    @Override
    public boolean isBlacklisted(@NotNull ItemStack itemStack) {
        for (Function<ItemStack, Boolean> f : BLACKLIST) {
            if (f.apply(itemStack)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public ReloadResult reloadModule() throws Exception {
        init();
        return Reloadable.super.reloadModule();
    }

    @Override
    public @NotNull String genBody() {
        return "<p>Blacklist Rules: " + BLACKLIST.size() + "</p>";
    }

    @Override
    public @NotNull String getTitle() {
        return "Shop Item Blacklist";
    }
}
