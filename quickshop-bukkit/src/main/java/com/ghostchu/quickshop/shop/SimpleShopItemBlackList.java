package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.registry.BuiltInRegistry;
import com.ghostchu.quickshop.api.registry.builtin.itemexpression.ItemExpressionRegistry;
import com.ghostchu.quickshop.api.shop.ShopItemBlackList;
import com.ghostchu.quickshop.util.ItemContainerUtil;
import com.ghostchu.quickshop.util.paste.item.SubPasteItem;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.Reloadable;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SimpleShopItemBlackList implements Reloadable, ShopItemBlackList, SubPasteItem {
    private final QuickShop plugin;
    private final List<String> BLACKLIST_LORES = new ArrayList<>();
    private List<String> configBlacklist;

    public SimpleShopItemBlackList(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        init();
        plugin.getReloadManager().register(this);
    }

    private void init() {
        BLACKLIST_LORES.clear();
        this.configBlacklist = plugin.getConfig().getStringList("blacklist");
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
        ItemExpressionRegistry itemExpressionRegistry = (ItemExpressionRegistry) plugin.getRegistry().getRegistry(BuiltInRegistry.ITEM_EXPRESSION);
        for (ItemStack is : ItemContainerUtil.flattenContents(itemStack, true, true)) {
            for (String s : this.configBlacklist) {
                if (itemExpressionRegistry.match(is, s)) {
                    return true;
                }
            }

            if (BLACKLIST_LORES.isEmpty()) {
                return false; // Fast return if empty
            }
            if (!is.hasItemMeta()) {
                return false;
            }
            ItemMeta meta = is.getItemMeta();
            if (meta == null) {
                return false;
            }
            if (!meta.hasLore()) {
                return false;
            }
            List<String> originalLores = meta.getLore();
            if (originalLores == null) {
                return false;
            }
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
        return "<p>Blacklist Rules: " + configBlacklist.size() + "</p>";
    }

    @Override
    public @NotNull String getTitle() {
        return "Shop Item Blacklist";
    }
}
