package com.ghostchu.quickshop.api.shop;

import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.serialize.BlockPos;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

/**
 * Minimal information about a shop.
 */
@Data
public class ShopInfoStorage {
    private final String world;
    private final BlockPos position;
    private final String owner;
    private final double price;
    private final String item;
    private final int unlimited;
    private final int shopType;
    private final String extra;
    private final String currency;
    private final boolean disableDisplay;
    private final String taxAccount;
    private final String inventoryWrapperName;
    private final String symbolLink;
    private final Map<UUID, String> permission;

    public ShopInfoStorage(String world, BlockPos position, QUser owner, double price, String item, int unlimited, int shopType, String extra, String currency, boolean disableDisplay, QUser taxAccount, String inventoryWrapperName, String symbolLink, Map<UUID, String> permission) {
        this.world = world;
        this.position = position;
        this.owner = owner.serialize();
        this.price = price;
        this.item = item;
        this.unlimited = unlimited;
        this.shopType = shopType;
        this.extra = extra;
        this.currency = currency;
        this.disableDisplay = disableDisplay;
        if (taxAccount != null) {
            this.taxAccount = taxAccount.serialize();
        } else {
            this.taxAccount = null;
        }
        this.inventoryWrapperName = inventoryWrapperName;
        this.symbolLink = symbolLink;
        this.permission = permission;
    }
}
