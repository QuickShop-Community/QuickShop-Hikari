package com.ghostchu.quickshop.addon.limited;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.limited.command.SubCommand_Limit;
import com.ghostchu.quickshop.api.command.CommandContainer;
import com.ghostchu.quickshop.api.event.CalendarEvent;
import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.economy.ShopPurchaseEvent;
import com.ghostchu.quickshop.api.event.economy.ShopSuccessPurchaseEvent;
import com.ghostchu.quickshop.api.event.management.ShopClickEvent;
import com.ghostchu.quickshop.api.localization.text.Text;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
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
    final int limit = shop.getExtra(new NamespacedKey(this, "limit"), 0);
    if (limit < 1) {
      return;
    }
    final UUID uuid = event.getPurchaser().getUniqueIdIfRealPlayer().orElse(null);
    if(uuid != null) {
      final int playerUsedLimit = shop.getExtra(new NamespacedKey(this, "data." + uuid), 0);
      if(playerUsedLimit + event.getAmount() > limit) {
        final Text text = plugin.text().of(event.getPurchaser(), "addon.limited.trade-limit-reached-cancel-reason");
        text.send();
        event.setCancelled(true, PlainTextComponentSerializer.plainText().serialize(text.forLocale()));
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void shopClick(final ShopClickEvent event) {

    if(event.isPhase(Phase.POST)) {

      final Shop shop = event.shop().get();
      final int limit = shop.getExtra(new NamespacedKey(this, "limit"), 0);
      if(limit < 1) {
        Log.debug("Shop limit is not enabled on this shop.");
        return;
      }
      final int playerUsedLimit = shop.getExtra(new NamespacedKey(this, "data." + event.user().getUniqueId()), 0);
      plugin.text().of(event.user(), "addon.limited.remains-limits", limit - playerUsedLimit).send();
      Log.debug("Shop limit is enabled on this shop. Limit: " + limit + " Used: " + playerUsedLimit);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void shopPurchaseSuccess(final ShopSuccessPurchaseEvent event) {

    final Shop shop = event.getShop();
    final int limit = shop.getExtra(new NamespacedKey(this, "limit"), 0);
    if(limit < 1) {
      return;
    }
    final UUID uuid = event.getPurchaser().getUniqueIdIfRealPlayer().orElse(null);
    if(uuid != null) {
      int playerUsedLimit = shop.getExtra(new NamespacedKey(this, "data." + uuid), 0);
      playerUsedLimit += event.getAmount();
      shop.setExtra(new NamespacedKey(this, "data." + uuid), playerUsedLimit);
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
      final int limit = shop.getExtra(new NamespacedKey(this, "limit"), 0);
      if(limit < 1) {
        return;
      }
      if(CommonUtil.isEmptyString(shop.getExtra(new NamespacedKey(this, "period"), ""))) {
        return;
      }
      try {
        if(event.getCalendarTriggerType().ordinal() >= CalendarEvent.CalendarTriggerType.valueOf(shop.getExtra(new NamespacedKey(this, "period"), "")).ordinal()) {
          shop.removeExtra(new NamespacedKey(this, "data"));
          Log.debug("Limit data has been reset. Shop -> " + shop);
        }
      } catch(final IllegalArgumentException ignored) {
        Log.debug("Limit data failed to reset. Shop -> " + shop + " type " + shop.getExtra(new NamespacedKey(this, "period"), "") + " not exists.");
        shop.removeExtra(new NamespacedKey(this, "period"));
      }
    }));

  }
}
