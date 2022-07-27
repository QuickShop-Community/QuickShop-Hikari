package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.api.shop.ShopInfoStorage;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ShopPriceChangedLog {
    private static int v = 1;
    private ShopInfoStorage shop;
    private double oldPrice;
    private double newPrice;
}
