package com.ghostchu.quickshop.api.shop;

import com.ghostchu.quickshop.api.serialize.BlockPos;
import com.ghostchu.quickshop.common.obj.QUser;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

/**
 * Minimal information about a shop.
 */
@Data
@Builder
public class ShopInfoStorage {
    private final String world;
    private final BlockPos position;
    private final QUser owner;
    private final double price;
    private final String item;
    private final int unlimited;
    private final int shopType;
    private final String extra;
    private final String currency;
    private final boolean disableDisplay;
    private final QUser taxAccount;
    private final String inventoryWrapperName;
    private final String symbolLink;
    private final Map<UUID, String> permission;

    public ShopInfoStorage(String world, BlockPos position, QUser owner, double price, String item, int unlimited, int shopType, String extra, String currency, boolean disableDisplay, QUser taxAccount, String inventoryWrapperName, String symbolLink, Map<UUID, String> permission) {
        this.world = world;
        this.position = position;
        this.owner = owner;
        this.price = price;
        this.item = item;
        this.unlimited = unlimited;
        this.shopType = shopType;
        this.extra = extra;
        this.currency = currency;
        this.disableDisplay = disableDisplay;
        this.taxAccount = taxAccount;
        this.inventoryWrapperName = inventoryWrapperName;
        this.symbolLink = symbolLink;
        this.permission = permission;
    }
}
