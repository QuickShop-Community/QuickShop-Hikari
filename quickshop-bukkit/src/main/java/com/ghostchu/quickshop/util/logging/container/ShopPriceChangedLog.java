package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.api.shop.ShopInfoStorage;
import lombok.Data;

@Data
public class ShopPriceChangedLog {
    private static int v = 1;
    private ShopInfoStorage shop;
    private double oldPrice;
    private double newPrice;

    public ShopPriceChangedLog(ShopInfoStorage shop, double oldPrice, double newPrice) {
        this.shop = shop;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
    }
}
