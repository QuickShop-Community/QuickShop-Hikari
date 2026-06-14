package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class SubCommand_FindNear implements CommandHandler<Player> {

  private final QuickShop plugin;

  public SubCommand_FindNear(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    final Location senderLocation = sender.getLocation();

    final Material material = sender.getInventory().getItemInMainHand().getType();
    if(material == Material.AIR) {
      sender.sendMessage(Component.text("Invalid item in hand."));
      return;
    }
    Util.asyncThreadRun(()->{

      final List<Shop> shops = plugin.getShopManager().getAllShops().stream()
              .map((shop -> {

                final Location shopLocation = shop.bukkitLocation();

                if (shop.getItem().getType() != material) {

                  return null;
                }

                if (shopLocation.distance(senderLocation) > 15) {

                  return null;
                }
                return shop;
              })).toList();

      if(shops.isEmpty()) {

        sender.sendMessage(Component.text("No shops of the material in your hand nearby!"));
        return;
      }

      for(final Shop shop : shops) {

        if(shop == null) continue;

        QuickShop.folia().getScheduler().runAtLocation(shop.bukkitLocation(), task->{

          final Location location = shop.bukkitLocation().getBlock().getLocation();

          final BlockDisplay display = EntityUtil.spawnDisplayBlockFor(sender, location, Material.GREEN_STAINED_GLASS);

          EntityUtil.showShopTrail(sender, shop.bukkitLocation(), 10);
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
