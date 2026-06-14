package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.EntityUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class SubCommand_Sale implements CommandHandler<Player> {

  private final QuickShop plugin;

  public SubCommand_Sale(final QuickShop plugin) {

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

          final Material[] colors = {
                  Material.YELLOW_STAINED_GLASS,
                  Material.ORANGE_STAINED_GLASS,
                  Material.RED_STAINED_GLASS
          };

          final BlockDisplay display = EntityUtil.spawnDisplayBlockAnimationFor(sender, location, colors, new Vector3f(1.02f, 1.02f, 1.02f), 15, 20, 2L);

          final TextDisplay text = EntityUtil.spawnDisplayTextAnimationFor(sender, location, new Component[]{
                  MiniMessage.miniMessage().deserialize("<white>SALE!?!?!?"),
                  MiniMessage.miniMessage().deserialize("<yellow>HURRY WHILE SUPPLIES LAST!")
          }, new Vector3f(1.0f, 1.0f, 1.0f), 15, TextDisplay.TextAlignment.CENTER, 20, 2L);
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
