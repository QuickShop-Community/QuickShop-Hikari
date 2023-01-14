package com.ghostchu.quickshop.papi.impl;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopManager;
import com.ghostchu.quickshop.api.shop.ShopType;
import com.ghostchu.quickshop.papi.PAPISubHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class ShopManagerPAPI implements PAPISubHandler {
    private final QuickShop plugin;
    private final ShopManager shopManager;

    public ShopManagerPAPI(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        this.shopManager = plugin.getShopManager();
    }

    @Override
    public @NotNull String getPrefix() {
        return "qs_shopmanager";
    }

    @Override
    @Nullable
    public String handle0(@NotNull UUID player, @NotNull String paramsTrimmed) {
        String[] args = paramsTrimmed.split("_");
        if (args.length < 1) {
            return null;
        }
        String[] passThroughArgs = new String[args.length - 1];
        System.arraycopy(args, 1, passThroughArgs, 0, passThroughArgs.length);

        return switch (args[0]) {
            case "global" -> handleGlobal(player, passThroughArgs);
            case "player" -> handlePlayer(player, passThroughArgs);
            default -> null;
        };
    }

    @Nullable
    private String handleGlobal(@NotNull UUID player, @NotNull String[] passThroughArgs) {
        if (passThroughArgs.length < 2) {
            return null;
        }
        String type = passThroughArgs[0];
        String condition = passThroughArgs[1];
        if (!type.equalsIgnoreCase("total")) return null;
        String[] passThroughArgsChild = new String[passThroughArgs.length - 1];
        System.arraycopy(passThroughArgs, 1, passThroughArgsChild, 0, passThroughArgsChild.length);
        return handleGlobalTotal(player, passThroughArgsChild);
    }

    @Nullable
    private String handlePlayer(@NotNull UUID player, String[] passThroughArgs) {
        if (passThroughArgs.length < 2) {
            return null;
        }
        String type = passThroughArgs[0];
        String condition = passThroughArgs[1];
        if (!type.equalsIgnoreCase("total")) return null;
        String[] passThroughArgsChild = new String[passThroughArgs.length - 1];
        System.arraycopy(passThroughArgs, 1, passThroughArgsChild, 0, passThroughArgsChild.length);
        return handlePlayerTotal(player, passThroughArgsChild);
    }

    @Nullable
    private String handleGlobalTotal(@NotNull UUID player, String[] passThroughArgsChild) {
        if (passThroughArgsChild.length < 1) {
            return null;
        }
        List<Shop> allShops = shopManager.getAllShops();
        return String.valueOf(switch (passThroughArgsChild[0]) {
            case "all" -> allShops.size();
            case "selling" -> allShops.stream().filter(shop -> shop.getShopType() == ShopType.SELLING).count();
            case "buying" -> allShops.stream().filter(shop -> shop.getShopType() == ShopType.BUYING).count();
            case "loaded" -> shopManager.getLoadedShops().size();
            case "unloaded" -> allShops.size() - shopManager.getLoadedShops().size();
        });
    }

    @Nullable
    private String handlePlayerTotal(@NotNull UUID player, String[] passThroughArgsChild) {
        if (passThroughArgsChild.length < 1) {
            return null;
        }
        List<Shop> belongToPlayers = shopManager.getPlayerAllShops(player);
        return String.valueOf(switch (passThroughArgsChild[0]) {
            case "all" -> belongToPlayers.size();
            case "selling" -> belongToPlayers.stream().filter(shop -> shop.getShopType() == ShopType.SELLING).count();
            case "buying" -> belongToPlayers.stream().filter(shop -> shop.getShopType() == ShopType.BUYING).count();
            case "loaded" -> shopManager.getLoadedShops().size();
            case "unloaded" -> belongToPlayers.stream().filter(shop -> !shop.isLoaded()).count();
        });
    }
}
