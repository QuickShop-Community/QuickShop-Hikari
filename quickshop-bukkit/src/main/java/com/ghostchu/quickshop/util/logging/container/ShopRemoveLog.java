package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.api.shop.ShopInfoStorage;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class ShopRemoveLog {
    private static int v = 1;
    private UUID player;
    private String reason;
    private ShopInfoStorage shop;
}
