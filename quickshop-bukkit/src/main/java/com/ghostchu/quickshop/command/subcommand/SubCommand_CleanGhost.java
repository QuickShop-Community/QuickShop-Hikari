package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logging.container.ShopRemoveLog;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
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

    if(!"confirm".equalsIgnoreCase(parser.getArgs().getFirst())) {
      plugin.text().of(sender, "cleanghost-warning").send();
      return;
    }

    plugin.text().of(sender, "cleanghost-starting").send();
    final AtomicInteger deletionCounter = new AtomicInteger(0);
    final List<CompletableFuture<Void>> pendingTasks = new CopyOnWriteArrayList<>();

    for(final Shop shop : plugin.getShopManager().getAllShops()) {
      final CompletableFuture<Void> task = QuickShop.folia().getScheduler().runAtLocation(shop.bukkitLocation(), (loc)->{
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
          return;
        }
        if(!shop.bukkitLocation().isWorldLoaded()) {
          plugin.text().of(sender, "cleanghost-deleting", shop.getShopId(), "unloaded world").send();
          plugin.getShopManager().deleteShop(shop);
          deletionCounter.incrementAndGet();
          plugin.logEvent(new ShopRemoveLog(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "SYSTEM", false), "/quickshop cleanghost command", shop.saveToInfoStorage()));
          return;
        }
        final BlockState shopBlockState = shop.bukkitLocation().getBlock().getState(false);
        if(!Util.canBeShop(shopBlockState.getBlock(), shopBlockState)) {
          plugin.text().of(sender, "cleanghost-deleting", shop.getShopId(), "invalid shop block").send();
          plugin.getShopManager().deleteShop(shop);
          deletionCounter.incrementAndGet();
          plugin.logEvent(new ShopRemoveLog(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "SYSTEM", false), "/quickshop cleanghost command", shop.saveToInfoStorage()));
        }
      });
      pendingTasks.add(task);
    }

    CompletableFuture.allOf(pendingTasks.toArray(new CompletableFuture[0]))
            .whenComplete((v, t)->plugin.text().of(sender, "cleanghost-deleted", deletionCounter.get()).send());

  }

}
