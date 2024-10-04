package com.ghostchu.quickshop.papi.impl;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopManager;
import com.ghostchu.quickshop.api.shop.ShopType;
import com.ghostchu.quickshop.papi.PAPISubHandler;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class ShopManagerPAPI implements PAPISubHandler {

  private final QuickShop plugin;
  private final ShopManager shopManager;

  public ShopManagerPAPI(@NotNull final QuickShop plugin) {

    this.plugin = plugin;
    this.shopManager = plugin.getShopManager();
  }

  @Override
  public @NotNull String getPrefix() {

    return "shopmanager";
  }

  @Override
  @Nullable
  public String handle0(@NotNull final OfflinePlayer player, @NotNull final String paramsTrimmed) {

    final String[] args = paramsTrimmed.split("_");
    if(args.length < 1) {
      return null;
    }
    final String[] passThroughArgs = new String[args.length - 1];
    System.arraycopy(args, 1, passThroughArgs, 0, passThroughArgs.length);

    return switch(args[0]) {
      case "global" -> handleGlobal(player, passThroughArgs);
      case "player" -> handlePlayer(player, passThroughArgs);
      default -> null;
    };
  }

  @Nullable
  private String handleGlobal(@NotNull final OfflinePlayer player, @NotNull final String[] passThroughArgs) {

    if(passThroughArgs.length < 2) {
      return null;
    }
    final String type = passThroughArgs[0];
    final String condition = passThroughArgs[1];
    if(!"total".equalsIgnoreCase(type)) {
      return null;
    }
    final String[] passThroughArgsChild = new String[passThroughArgs.length - 1];
    System.arraycopy(passThroughArgs, 1, passThroughArgsChild, 0, passThroughArgsChild.length);
    return handleGlobalTotal(player, passThroughArgsChild);
  }

  @Nullable
  private String handlePlayer(@NotNull final OfflinePlayer player, final String[] passThroughArgs) {

    if(passThroughArgs.length < 2) {
      return null;
    }
    final String type = passThroughArgs[0];
    final String condition = passThroughArgs[1];
    if(!"total".equalsIgnoreCase(type)) {
      return null;
    }
    final String[] passThroughArgsChild = new String[passThroughArgs.length - 1];
    System.arraycopy(passThroughArgs, 1, passThroughArgsChild, 0, passThroughArgsChild.length);
    return handlePlayerTotal(player.getUniqueId(), passThroughArgsChild);
  }

  @Nullable
  private String handleGlobalTotal(@NotNull final OfflinePlayer player, final String[] passThroughArgsChild) {

    if(passThroughArgsChild.length < 1) {
      return null;
    }
    final List<Shop> allShops = shopManager.getAllShops();
    return String.valueOf(switch(passThroughArgsChild[0]) {
      case "all" -> allShops.size();
      case "selling" ->
              allShops.stream().filter(shop->shop.getShopType() == ShopType.SELLING).count();
      case "buying" ->
              allShops.stream().filter(shop->shop.getShopType() == ShopType.BUYING).count();
      case "freeze" ->
              allShops.stream().filter(shop->shop.getShopType() == ShopType.FROZEN).count();
      case "loaded" -> shopManager.getLoadedShops().size();
      case "unloaded" -> allShops.size() - shopManager.getLoadedShops().size();
      default -> null;
    });
  }

  @Nullable
  private String handlePlayerTotal(@NotNull final UUID player, final String[] passThroughArgsChild) {

    if(passThroughArgsChild.length < 1) {
      return null;
    }
    final List<Shop> belongToPlayers = shopManager.getAllShops(player);
    return String.valueOf(switch(passThroughArgsChild[0]) {
      case "all" -> belongToPlayers.size();
      case "selling" ->
              belongToPlayers.stream().filter(shop->shop.getShopType() == ShopType.SELLING).count();
      case "buying" ->
              belongToPlayers.stream().filter(shop->shop.getShopType() == ShopType.BUYING).count();
      case "freeze" ->
              belongToPlayers.stream().filter(shop->shop.getShopType() == ShopType.FROZEN).count();
      case "loaded" -> shopManager.getLoadedShops().size();
      case "unloaded" -> belongToPlayers.stream().filter(shop->!shop.isLoaded()).count();
      default -> null;
    });
  }
}
