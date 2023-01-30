package com.ghostchu.quickshop.addon.limited;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.limited.command.SubCommand_Limit;
import com.ghostchu.quickshop.api.command.CommandContainer;
import com.ghostchu.quickshop.api.event.CalendarEvent;
import com.ghostchu.quickshop.api.event.ShopPurchaseEvent;
import com.ghostchu.quickshop.api.event.ShopSuccessPurchaseEvent;
import com.ghostchu.quickshop.api.localization.text.Text;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin implements Listener {
    public static Main instance;
    private QuickShop plugin;

    private CommandContainer container;

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        plugin.getCommandManager().unregisterCmd(container);
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        this.plugin = QuickShop.getInstance();
        this.container = CommandContainer.builder()
                .prefix("limit")
                .permission("quickshopaddon.limit.use")
                .description((locale) -> plugin.text().of("addon.limited.commands.limit").forLocale(locale))
                .executor(new SubCommand_Limit(plugin))
                .build();
        plugin.getCommandManager().registerCmd(container);
    }

    @EventHandler(ignoreCancelled = true)
    public void shopPurchase(ShopPurchaseEvent event) {
        Shop shop = event.getShop();
        ConfigurationSection storage = shop.getExtra(this);
        if (storage.getInt("limit") < 1) {
            return;
        }
        int limit = storage.getInt("limit");
        int playerUsedLimit = storage.getInt("data." + event.getPlayer().getUniqueId(), 0);
        if (playerUsedLimit + event.getAmount() > limit) {
            Text text = plugin.text().of(event.getPlayer(), "addon.limited.trade-limit-reached-cancel-reason");
            text.send();
            event.setCancelled(true, PlainTextComponentSerializer.plainText().serialize(text.forLocale()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void shopPurchaseSuccess(ShopSuccessPurchaseEvent event) {
        Shop shop = event.getShop();
        ConfigurationSection storage = shop.getExtra(this);
        if (storage.getInt("limit") < 1) {
            return;
        }
        int limit = storage.getInt("limit");
        int playerUsedLimit = storage.getInt("data." + event.getPlayer().getUniqueId(), 0);
        playerUsedLimit += event.getAmount();
        storage.set("data." + event.getPlayer().getUniqueId(), playerUsedLimit);
        shop.setExtra(this, storage);
        event.getPlayer().sendTitle(
                LegacyComponentSerializer.legacySection()
                        .serialize(plugin.text().of(event.getPlayer(), "addon.limited.titles.title")
                                .forLocale()),
                LegacyComponentSerializer.legacySection()
                        .serialize(plugin.text().of(event.getPlayer(), "addon.limited.titles.subtitle"
                                , (limit - playerUsedLimit)).forLocale()));
    }

    @EventHandler(ignoreCancelled = true)
    public void scheduleEvent(CalendarEvent event) {
        if (event.getCalendarTriggerType() == CalendarEvent.CalendarTriggerType.SECOND
                || event.getCalendarTriggerType() == CalendarEvent.CalendarTriggerType.NOTHING_CHANGED) {
            return;
        }
        Util.asyncThreadRun(() -> plugin.getShopManager().getAllShops().forEach(shop -> {
            ConfigurationSection manager = shop.getExtra(this);
            int limit = manager.getInt("limit");
            if (limit < 1) {
                return;
            }
            if (StringUtils.isEmpty(manager.getString("period"))) {
                return;
            }
            try {
                if (event.getCalendarTriggerType().ordinal() >= CalendarEvent.CalendarTriggerType.valueOf(manager.getString("period")).ordinal()) {
                    manager.set("data", null);
                    shop.setExtra(this, manager);
                    Log.debug("Limit data has been reset. Shop -> " + shop);
                }
            } catch (IllegalArgumentException ignored) {
                Log.debug("Limit data failed to reset. Shop -> " + shop + " type " + manager.getString("period") + " not exists.");
                manager.set("period", null);
                shop.setExtra(this, manager);
            }
        }));

    }
}
