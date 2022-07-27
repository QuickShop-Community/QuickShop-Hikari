package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.api.shop.ShopInfoStorage;
import com.ghostchu.quickshop.api.shop.ShopModerator;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ShopModeratorChangedLog {
    private static int v = 1;
    private ShopInfoStorage shop;
    private ShopModerator moderator;
}
