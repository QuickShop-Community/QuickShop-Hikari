package com.ghostchu.quickshop.shop.controlpanel;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.localization.text.ProxiedLocale;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopControlPanel;
import com.ghostchu.quickshop.api.shop.ShopControlPanelPriority;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.util.MsgUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SimpleShopControlPanel implements ShopControlPanel {
    /**
     * The shop control panel's priority.
     * HIGH = Earlier shown
     * LOW = Later shown
     *
     * @return The priority.
     */
    @Override
    public @NotNull ShopControlPanelPriority getPriority() {
        return ShopControlPanelPriority.HIGH;
    }

    /**
     * The shop control panel impl's plugin instance.
     *
     * @return Your plugin instance;
     */
    @Override
    public @NotNull Plugin getPlugin() {
        return QuickShop.getInstance();
    }

    /**
     * Generate components for the shop control panel.
     *
     * @param sender The player
     * @param shop   The shop
     * @return The components, or empty list if nothing to show. Every component will be shown in a new line.
     */
    @Override
    public @NotNull List<Component> generate(@NotNull Player sender, @NotNull Shop shop) {
        QuickShop plugin = QuickShop.getInstance();
        List<Component> components = new ArrayList<>();
        ProxiedLocale locale = plugin.text().findRelativeLanguages(sender.getLocale());
        // Owner
        if (!plugin.perm().hasPermission(sender, "quickshop.setowner")) {
            components.add(plugin.text().of(sender, "menu.owner", shop.ownerName(locale)).forLocale());
        } else {
            Component text;
            if (plugin.getConfig().getBoolean("shop.show-owner-uuid-in-controlpanel-if-op") && shop.isUnlimited()) {
                text = plugin.text().of(sender, "controlpanel.setowner-uuid", shop.ownerName(locale), shop.getOwner().toString()).forLocale();
            } else {
                text = plugin.text().of(sender, "controlpanel.setowner", shop.ownerName(locale)).forLocale();
            }
            components.add(text
                    .hoverEvent(HoverEvent.showText(plugin.text().of(sender, "controlpanel.setowner-hover").forLocale()))
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/qs setowner ")));
        }
        // Unlimited
        if (plugin.perm().hasPermission(sender, "quickshop.unlimited")) {
            Component text = plugin.text().of(sender, "controlpanel.unlimited", MsgUtil.bool2String(shop.isUnlimited())).forLocale();
            Component hoverText = plugin.text().of(sender, "controlpanel.unlimited-hover").forLocale();
            String clickCommand = MsgUtil.fillArgs("/qs silentunlimited {0}", shop.getRuntimeRandomUniqueId().toString());
            components.add(text
                    .hoverEvent(HoverEvent.showText(hoverText))
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, clickCommand)));
        }
//        // Always Counting
//        if (plugin.perm().hasPermission(sender, "quickshop.alwayscounting")) {
//            Component text = plugin.text().of(sender, "controlpanel.alwayscounting", MsgUtil.bool2String(shop.isAlwaysCountingContainer())).forLocale();
//            Component hoverText = plugin.text().of(sender, "controlpanel.alwayscounting-hover").forLocale();
//            String clickCommand = MsgUtil.fillArgs("/qs silentalwayscounting {0}", shop.getRuntimeRandomUniqueId().toString());
//            components.add(text
//                    .hoverEvent(HoverEvent.showText(hoverText))
//                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, clickCommand)));
//        }
        // Buying/Selling Mode
        if (plugin.perm().hasPermission(sender, "quickshop.create.buy")
                && plugin.perm().hasPermission(sender, "quickshop.create.sell")
                && (shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_SHOPTYPE) ||
                plugin.perm().hasPermission(sender, "quickshop.create.admin"))) {
            if (shop.isSelling()) {
                Component text = plugin.text().of(sender, "controlpanel.mode-selling").forLocale();
                Component hoverText = plugin.text().of(sender, "controlpanel.mode-selling-hover").forLocale();
                String clickCommand = MsgUtil.fillArgs("/qs silentbuy {0}", shop.getRuntimeRandomUniqueId().toString());
                components.add(text
                        .hoverEvent(HoverEvent.showText(hoverText))
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, clickCommand)));
            } else if (shop.isBuying()) {
                Component text = plugin.text().of(sender, "controlpanel.mode-buying").forLocale();
                Component hoverText = plugin.text().of(sender, "controlpanel.mode-buying-hover").forLocale();
                String clickCommand = MsgUtil.fillArgs("/qs silentsell {0}", shop.getRuntimeRandomUniqueId().toString());
                components.add(text
                        .hoverEvent(HoverEvent.showText(hoverText))
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, clickCommand)));
            }
        }
        // Set Price
        if (plugin.perm().hasPermission(sender, "quickshop.other.price")
                || (plugin.perm().hasPermission(sender, "quickshop.create.changeprice") && shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_PRICE))) {
            Component text = MsgUtil.fillArgs(
                    plugin.text().of(sender, "controlpanel.price").forLocale(),
                    LegacyComponentSerializer.legacySection().deserialize(
                            (plugin.getConfig().getBoolean("use-decimal-format"))
                                    ? MsgUtil.decimalFormat(shop.getPrice())
                                    : Double.toString(shop.getPrice()))
            );
            Component hoverText = plugin.text().of(sender, "controlpanel.price-hover").forLocale();
            String clickCommand = "/qs price ";

            components.add(text
                    .hoverEvent(HoverEvent.showText(hoverText))
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickCommand)));
        }
        //Set amount per bulk
        if (plugin.isAllowStack()) {
            if (plugin.perm().hasPermission(sender, "quickshop.other.amount") ||
                    shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_STACK_AMOUNT) &&
                            plugin.perm().hasPermission(sender, "quickshop.create.changeamount")) {
                Component text = plugin.text().of(sender, "controlpanel.stack", shop.getItem().getAmount()).forLocale();
                Component hoverText = plugin.text().of(sender, "controlpanel.stack-hover").forLocale();
                String clickCommand = "/qs size ";
                components.add(text
                        .hoverEvent(HoverEvent.showText(hoverText))
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickCommand)));
            }
        }
        if (!shop.isUnlimited()) {
            // Refill
            if (plugin.perm().hasPermission(sender, "quickshop.refill")) {
                Component text = plugin.text().of(sender, "controlpanel.refill", Component.text(shop.getPrice())).forLocale();
                Component hoverText = plugin.text().of(sender, "controlpanel.refill-hover").forLocale();
                String clickCommand = "/qs refill ";
                components.add(text
                        .hoverEvent(HoverEvent.showText(hoverText))
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickCommand)));
            }
            // Empty
            if (plugin.perm().hasPermission(sender, "quickshop.empty")) {
                Component text = plugin.text().of(sender, "controlpanel.empty", Component.text(shop.getPrice())).forLocale();
                Component hoverText = plugin.text().of(sender, "controlpanel.empty-hover").forLocale();
                String clickCommand = MsgUtil.fillArgs("/qs silentempty {0}", shop.getRuntimeRandomUniqueId().toString());
                components.add(text
                        .hoverEvent(HoverEvent.showText(hoverText))
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, clickCommand)));
            }
        }
        // Remove
        if (plugin.perm().hasPermission(sender, "quickshop.other.destroy") || shop.playerAuthorize(((OfflinePlayer) sender).getUniqueId(), BuiltInShopPermission.DELETE)) {
            Component text = plugin.text().of(sender, "controlpanel.remove", Component.text(shop.getPrice())).forLocale();
            Component hoverText = plugin.text().of(sender, "controlpanel.remove-hover").forLocale();
            String clickCommand = MsgUtil.fillArgs("/qs silentremove {0}", shop.getRuntimeRandomUniqueId().toString());
            components.add(text
                    .hoverEvent(HoverEvent.showText(hoverText))
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, clickCommand)));
        }
        return components;
    }
}
