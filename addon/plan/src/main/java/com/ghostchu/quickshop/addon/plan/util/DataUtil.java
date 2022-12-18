package com.ghostchu.quickshop.addon.plan.util;

import com.ghostchu.quickshop.addon.plan.Main;
import com.ghostchu.quickshop.api.database.ShopMetricRecord;
import com.ghostchu.quickshop.api.database.bean.DataRecord;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.Util;
import com.google.common.html.HtmlEscapers;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;
import org.enginehub.squirrelid.Profile;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.UUID;

public class DataUtil {
    public final Main main;

    public DataUtil(@NotNull Main main) {
        this.main = main;
    }

    @NotNull
    public String formatEconomy(@NotNull ShopMetricRecord record) {
        Shop shop = main.getQuickShop().getShopManager().getShop(record.getShopId());
        if (shop == null || main.getQuickShop().getEconomy() == null) {
            DecimalFormat df = new DecimalFormat("#.00");
            return df.format(record.getTotal());
        }
        return main.getQuickShop().getEconomy().format(record.getTotal(), shop.getLocation().getWorld(), shop.getCurrency());
    }

    @NotNull
    public String getItemName(@NotNull DataRecord dataRecord) {
        ItemStack stack;
        try {
            stack = Util.deserialize(dataRecord.getItem());
        } catch (InvalidConfigurationException e) {
            return "[Failed to deserialize]";
        }
        if (stack == null) return "[Failed to deserialize]";
        String name = CommonUtil.prettifyText(stack.getType().name());
        if (stack.getItemMeta() != null && stack.getItemMeta().hasDisplayName()) {
            name = stack.getItemMeta().getDisplayName();
        }
        return HtmlEscapers.htmlEscaper().escape(name);
    }

    @NotNull
    public String getItemName(@NotNull ItemStack stack) {
        String name = CommonUtil.prettifyText(stack.getType().name());
        if (stack.getItemMeta() != null && stack.getItemMeta().hasDisplayName()) {
            name = stack.getItemMeta().getDisplayName();
        }
        return HtmlEscapers.htmlEscaper().escape(name);
    }

    @NotNull
    public String getShopName(@NotNull ShopMetricRecord record, @NotNull DataRecord dataRecord) {
        StringBuilder nameBuilder = new StringBuilder();
        Shop shop = main.getQuickShop().getShopManager().getShop(record.getShopId());
        if (shop == null) nameBuilder.append("[Deleted] ");
        String shopName = dataRecord.getName();
        if (shopName != null) {
            nameBuilder.append(ChatColor.stripColor(shopName));
        } else {
            if (shop != null) {
                Location location = shop.getLocation();
                String template = "%s %s,%s,%s";
                nameBuilder.append(String.format(template, location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ()));
            } else {
                nameBuilder.append("N/A");
            }
        }
        return HtmlEscapers.htmlEscaper().escape(nameBuilder.toString());
    }

    @NotNull
    public String loc2String(@NotNull Location location) {
        String template = "%s %s,%s,%s";
        return String.format(template, location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @NotNull
    public String getPlayerName(@NotNull UUID uuid) {
        if (CommonUtil.getNilUniqueId().equals(uuid)) {
            return "[Server]";
        }
        Profile profile = main.getQuickShop().getPlayerFinder().find(uuid);
        if (profile == null || profile.getName() == null || profile.getName().equals("")) {
            return uuid.toString();
        }
        return HtmlEscapers.htmlEscaper().escape(profile.getName());
    }
}
