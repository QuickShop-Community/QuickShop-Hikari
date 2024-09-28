package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logging.container.ShopRemoveLog;
import com.ghostchu.quickshop.util.performance.BatchBukkitExecutor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public class SubCommand_CleanGhost implements CommandHandler<CommandSender> {

  private final QuickShop plugin;

  public SubCommand_CleanGhost(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final CommandSender sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if(parser.getArgs().isEmpty()) {
      plugin.text().of(sender, "cleanghost-warning").send();
      return;
    }

    if(!"confirm".equalsIgnoreCase(parser.getArgs().get(0))) {
      plugin.text().of(sender, "cleanghost-warning").send();
      return;
    }

    plugin.text().of(sender, "cleanghost-starting").send();
    final AtomicInteger deletionCounter = new AtomicInteger(0);
    final BatchBukkitExecutor<Shop> updateExecutor = new BatchBukkitExecutor<>();
    updateExecutor.addTasks(plugin.getShopManager().getAllShops());
    updateExecutor.startHandle(plugin.getJavaPlugin(), (shop)->{
      if(shop == null) {
        return; // WTF
      }
      if(shop.getOwner() == null) {
        plugin.text().of(sender, "cleanghost-deleting", shop.getShopId(), "invalid owner data").send();
        plugin.getShopManager().deleteShop(shop);
        deletionCounter.incrementAndGet();
        plugin.logEvent(new ShopRemoveLog(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "SYSTEM", false), "/quickshop cleanghost command", shop.saveToInfoStorage()));
        return;
      }
      if(shop.getItem().getType() == Material.AIR) {
        plugin.text().of(sender, "cleanghost-deleting", shop.getShopId(), "invalid item data").send();
        plugin.getShopManager().deleteShop(shop);
        deletionCounter.incrementAndGet();
        plugin.logEvent(new ShopRemoveLog(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "SYSTEM", false), "/quickshop cleanghost command", shop.saveToInfoStorage()));
        return;
      }
      if(plugin.getShopItemBlackList().isBlacklisted(shop.getItem())) {
        plugin.text().of(sender, "cleanghost-deleting", shop.getShopId(), "blacklisted item").send();
        plugin.getShopManager().deleteShop(shop);
        deletionCounter.incrementAndGet();
        plugin.logEvent(new ShopRemoveLog(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "SYSTEM", false), "/quickshop cleanghost command", shop.saveToInfoStorage()));
      }
      if(!shop.getLocation().isWorldLoaded()) {
        plugin.text().of(sender, "cleanghost-deleting", shop.getShopId(), "unloaded world").send();
        plugin.getShopManager().deleteShop(shop);
        deletionCounter.incrementAndGet();
        plugin.logEvent(new ShopRemoveLog(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "SYSTEM", false), "/quickshop cleanghost command", shop.saveToInfoStorage()));
        return;
      }
      if(!Util.canBeShop(shop.getLocation().getBlock())) {
        plugin.text().of(sender, "cleanghost-deleting", shop.getShopId(), "invalid shop block").send();
        plugin.getShopManager().deleteShop(shop);
        deletionCounter.incrementAndGet();
        plugin.logEvent(new ShopRemoveLog(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "SYSTEM", false), "/quickshop cleanghost command", shop.saveToInfoStorage()));
      }
    }).whenComplete((aVoid, throwable)->
                            plugin.text().of(sender, "cleanghost-deleted", deletionCounter.get()
                                            ).send());

  }

}
