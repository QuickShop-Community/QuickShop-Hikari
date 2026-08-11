package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopChunk;
import com.ghostchu.quickshop.util.MsgUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class SubCommand_Info implements CommandHandler<CommandSender> {

  private final QuickShop plugin;

  public SubCommand_Info(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final CommandSender sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    int buying = 0;
    int selling = 0;
    int chunks = 0;
    int worlds = 0;
    int nostock = 0;
    final boolean folia = QuickShop.folia().isFolia();
    final List<Shop> outOfStockCheckQueue = folia? new ArrayList<>() : null;

    for(final Map<ShopChunk, Map<Location, Shop>> inWorld :
            plugin.getShopManager().getShops().values()) {
      worlds++;

      for(final Map<Location, Shop> inChunk : inWorld.values()) {
        chunks++;
        for(final Shop shop : inChunk.values()) {
          if(shop.isBuying()) {
            buying++;
          } else if(shop.isSelling()) {
            selling++;
          }
          if(shop.isSelling() && shop.isLoaded()) {
            if(folia) {
              outOfStockCheckQueue.add(shop);
            } else if(shop.getRemainingStock() == 0) {
              nostock++;
            }
          }
        }
      }
    }

    if(!folia) {
      sendStats(sender, buying, selling, chunks, worlds, nostock);
      return;
    }

    if(outOfStockCheckQueue.isEmpty()) {
      sendStats(sender, buying, selling, chunks, worlds, 0);
      return;
    }

    final AtomicInteger noStockCounter = new AtomicInteger(0);
    final List<CompletableFuture<Void>> futures = new ArrayList<>(outOfStockCheckQueue.size());
    for(final Shop shop : outOfStockCheckQueue) {
      final CompletableFuture<Void> future = QuickShop.folia().getScheduler().runAtLocation(shop.bukkitLocation(), task->{
        try {
          if(shop.getRemainingStock() == 0) {
            noStockCounter.incrementAndGet();
          }
        } catch(final Throwable ignored) {
        }
      });
      futures.add(future);
    }

    final int buyingFinal = buying;
    final int sellingFinal = selling;
    final int chunksFinal = chunks;
    final int worldsFinal = worlds;
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .whenComplete((unused, throwable)->QuickShop.folia().getScheduler().runLater(()->
                                                                                                 sendStats(sender, buyingFinal, sellingFinal, chunksFinal, worldsFinal, noStockCounter.get()), 1));
  }

  private void sendStats(@NotNull final CommandSender sender, final int buying, final int selling, final int chunks, final int worlds, final int nostock) {

    MsgUtil.sendDirectMessage(sender, Component.text("QuickShop Statistics...").color(NamedTextColor.GOLD));
    MsgUtil.sendDirectMessage(sender, Component.text("Server UniqueId: " + plugin.getServerUniqueID()).color(NamedTextColor.GREEN));
    MsgUtil.sendDirectMessage(sender, Component.text((buying + selling)
                                                     + " shops in "
                                                     + chunks
                                                     + " chunks spread over "
                                                     + worlds
                                                     + " worlds.").color(NamedTextColor.GREEN));
    MsgUtil.sendDirectMessage(sender, Component.text(nostock
                                                     + " out-of-stock loaded shops (excluding doubles) which will be removed by /quickshop clean.").color(NamedTextColor.GREEN));
    MsgUtil.sendDirectMessage(sender, Component.text("QuickShop " + QuickShop.getInstance().getVersion()).color(NamedTextColor.GREEN));
  }

}
