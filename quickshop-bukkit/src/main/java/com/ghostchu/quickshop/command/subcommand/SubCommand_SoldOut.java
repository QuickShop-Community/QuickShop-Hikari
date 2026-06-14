package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.EntityUtil;
import com.ghostchu.quickshop.util.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

public class SubCommand_SoldOut implements CommandHandler<Player> {

  private final QuickShop plugin;

  public SubCommand_SoldOut(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    final Location senderLocation = sender.getLocation();

    final List<CompletableFuture<Shop>> futures = plugin.getShopManager().getAllShops().stream()
            .map(shop -> shop.getRemainingStockAsync().thenApply(stock -> {

              final Location shopLocation = shop.bukkitLocation();

              if(shopLocation.getWorld() == null || senderLocation.getWorld() == null) {

                return null;
              }

              if(!shopLocation.getWorld().equals(senderLocation.getWorld())) {

                return null;
              }

              if(stock > 0) {

                return null;
              }

              if (shopLocation.distance(senderLocation) > 15) {

                return null;
              }
              return shop;
            })).toList();

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept(ignored -> {

      final List<Shop> shops = futures.stream().map(CompletableFuture::join).filter(Objects::nonNull).toList();

      if(!shops.isEmpty()) {

        sender.sendMessage(Component.text("Showing sold out shops"));
      }

      for(final Shop shop : shops) {

        QuickShop.folia().getScheduler().runAtLocation(shop.bukkitLocation(), task->{

          final Location location = shop.bukkitLocation().getBlock().getLocation();

          final BlockDisplay display = EntityUtil.spawnDisplayBlockFor(sender, location, Material.YELLOW_STAINED_GLASS);
        });
      }
    });
  }

  @NotNull
  @Override
  public List<String> onTabComplete(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    return Collections.emptyList();
  }

}
