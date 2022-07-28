package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.api.shop.ShopInfoStorage;
import com.ghostchu.quickshop.api.shop.ShopModerator;
import lombok.Data;

@Data
public class ShopModeratorChangedLog {
    private static int v = 1;
    private ShopInfoStorage shop;
    private ShopModerator moderator;

    public ShopModeratorChangedLog(ShopInfoStorage shop, ShopModerator moderator) {
        this.shop = shop;
        this.moderator = moderator;
    }
}
