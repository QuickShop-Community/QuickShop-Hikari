package com.ghostchu.quickshop.api.shop;

import com.ghostchu.quickshop.api.serialize.BlockPos;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

/**
 * Minimal information about a shop.
 */
@AllArgsConstructor
@Data
@Builder
public class ShopInfoStorage {
    private final String world;
    private final BlockPos position;
    private final UUID owner;
    private final double price;
    private final String item;
    private final int unlimited;
    private final int shopType;
    private final String extra;
    private final String currency;
    private final boolean disableDisplay;
    private final UUID taxAccount;
    private final String inventoryWrapperName;
    private final String symbolLink;
    private final Map<UUID, String> permission;
}
