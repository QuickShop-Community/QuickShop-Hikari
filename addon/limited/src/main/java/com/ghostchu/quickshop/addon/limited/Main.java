package com.ghostchu.quickshop.addon.limited;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.limited.command.SubCommand_Limit;
import com.ghostchu.quickshop.api.command.CommandContainer;
import com.ghostchu.quickshop.api.event.CalendarEvent;
import com.ghostchu.quickshop.api.event.economy.ShopPurchaseEvent;
import com.ghostchu.quickshop.api.event.economy.ShopSuccessPurchaseEvent;
import com.ghostchu.quickshop.api.event.management.ShopClickEvent;
import com.ghostchu.quickshop.api.localization.text.Text;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

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
            .description((locale)->plugin.text().of("addon.limited.commands.limit").forLocale(locale))
            .executor(new SubCommand_Limit(plugin))
            .build();
    plugin.getCommandManager().registerCmd(container);

  }

  @EventHandler(ignoreCancelled = true)
  public void shopPurchase(final ShopPurchaseEvent event) {

    final Shop shop = event.getShop();
    final ConfigurationSection storage = shop.getExtra(this);
    if(storage.getInt("limit") < 1) {
      return;
    }
    final int limit = storage.getInt("limit");
    final UUID uuid = event.getPurchaser().getUniqueIdIfRealPlayer().orElse(null);
    if(uuid != null) {
      final int playerUsedLimit = storage.getInt("data." + uuid, 0);
      if(playerUsedLimit + event.getAmount() > limit) {
        final Text text = plugin.text().of(event.getPurchaser(), "addon.limited.trade-limit-reached-cancel-reason");
        text.send();
        event.setCancelled(true, PlainTextComponentSerializer.plainText().serialize(text.forLocale()));
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void shopClick(final ShopClickEvent event) {

    final Shop shop = event.shop();
    final ConfigurationSection storage = shop.getExtra(this);
    if(storage.getInt("limit") < 1) {
      Log.debug("Shop limit is not enabled on this shop.");
      return;
    }
    final int limit = storage.getInt("limit");
    final int playerUsedLimit = storage.getInt("data." + event.user().getUniqueId(), 0);
    plugin.text().of(event.user(), "addon.limited.remains-limits", limit - playerUsedLimit).send();
    Log.debug("Shop limit is enabled on this shop. Limit: " + limit + " Used: " + playerUsedLimit);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void shopPurchaseSuccess(final ShopSuccessPurchaseEvent event) {

    final Shop shop = event.getShop();
    final ConfigurationSection storage = shop.getExtra(this);
    if(storage.getInt("limit") < 1) {
      return;
    }
    final UUID uuid = event.getPurchaser().getUniqueIdIfRealPlayer().orElse(null);
    if(uuid != null) {
      final int limit = storage.getInt("limit");
      int playerUsedLimit = storage.getInt("data." + uuid, 0);
      playerUsedLimit += event.getAmount();
      storage.set("data." + uuid, playerUsedLimit);
      shop.setExtra(this, storage);
      final Player player = Bukkit.getPlayer(uuid);
      if(player != null) {
        player.sendTitle(plugin.text().of(player, "addon.limited.titles.title").legacy(),
                         plugin.text().of(player, "addon.limited.titles.subtitle", (limit - playerUsedLimit)).legacy());
      }
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void scheduleEvent(final CalendarEvent event) {

    if(event.getCalendarTriggerType() == CalendarEvent.CalendarTriggerType.SECOND
       || event.getCalendarTriggerType() == CalendarEvent.CalendarTriggerType.NOTHING_CHANGED) {
      return;
    }
    Util.asyncThreadRun(()->plugin.getShopManager().getAllShops().forEach(shop->{
      final ConfigurationSection manager = shop.getExtra(this);
      final int limit = manager.getInt("limit");
      if(limit < 1) {
        return;
      }
      if(StringUtils.isEmpty(manager.getString("period"))) {
        return;
      }
      try {
        if(event.getCalendarTriggerType().ordinal() >= CalendarEvent.CalendarTriggerType.valueOf(manager.getString("period")).ordinal()) {
          manager.set("data", null);
          shop.setExtra(this, manager);
          Log.debug("Limit data has been reset. Shop -> " + shop);
        }
      } catch(final IllegalArgumentException ignored) {
        Log.debug("Limit data failed to reset. Shop -> " + shop + " type " + manager.getString("period") + " not exists.");
        manager.set("period", null);
        shop.setExtra(this, manager);
      }
    }));

  }
}
