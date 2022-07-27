package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.api.shop.ShopInfoStorage;
import lombok.Data;

import java.util.UUID;

@Data
public class ShopRemoveLog {
    private static int v = 1;
    private UUID player;
    private String reason;
    private ShopInfoStorage shop;

    public ShopRemoveLog(UUID player, String reason, ShopInfoStorage shop) {
        this.player = player;
        this.reason = reason;
        this.shop = shop;
    }
}
