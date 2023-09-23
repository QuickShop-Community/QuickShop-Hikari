package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.ShopItemBlackList;
import com.ghostchu.quickshop.util.ItemExpression;
import com.ghostchu.quickshop.util.paste.item.SubPasteItem;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.Reloadable;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class SimpleShopItemBlackList implements Reloadable, ShopItemBlackList, SubPasteItem {
    private final QuickShop plugin;
    private final List<Function<ItemStack, Boolean>> BLACKLIST = new ArrayList<>();
    private final List<String> BLACKLIST_LORES = new ArrayList<>();

    public SimpleShopItemBlackList(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        init();
        plugin.getReloadManager().register(this);
    }

    private void init() {
        BLACKLIST.clear();
        BLACKLIST_LORES.clear();
        List<String> configBlacklist = plugin.getConfig().getStringList("blacklist");
        for (String s : configBlacklist) {
            Optional<Function<ItemStack, Boolean>> func = new ItemExpression(plugin, s).getFunction();
            if (func.isPresent()) {
                BLACKLIST.add(func.get());
            } else {
                plugin.logger().warn("Failed to parse item expression: {}", s);
            }
        }
        List<String> configLoresBlackList = plugin.getConfig().getStringList("shop.blacklist-lores");
        configLoresBlackList.forEach(s -> BLACKLIST_LORES.add(ChatColor.stripColor(s)));
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
        if (BLACKLIST_LORES.isEmpty()) return false; // Fast return if empty
        if (!itemStack.hasItemMeta()) return false;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return false;
        if (!meta.hasLore()) return false;
        List<String> originalLores = meta.getLore();
        if (originalLores == null) return false;
        List<String> strippedLores = new ArrayList<>(originalLores.size());
        for (String originalLore : originalLores) {
            strippedLores.add(ChatColor.stripColor(originalLore));
        }
        for (String loreLine : strippedLores) {
            for (String blacklistLore : BLACKLIST_LORES) {
                if (loreLine.contains(blacklistLore)) {
                    return true;
                }
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
