package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.ShopItemBlackList;
import com.ghostchu.quickshop.util.ItemExpression;
import com.ghostchu.quickshop.util.paste.item.SubPasteItem;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.Reloadable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
            Optional<Function<ItemStack, Boolean>> func = new ItemExpression(plugin, s).getFunction();
            if (func.isPresent()) {
                BLACKLIST.add(func.get());
            } else {
                plugin.logger().warn("Failed to parse item expression: {}", s);
            }
        }
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
