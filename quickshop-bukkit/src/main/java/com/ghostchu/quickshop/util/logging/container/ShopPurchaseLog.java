package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.api.shop.ShopInfoStorage;
import com.ghostchu.quickshop.api.shop.ShopType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class ShopPurchaseLog {
    private static int v = 1;
    private ShopInfoStorage shop;
    private ShopType type;
    private UUID trader;
    private String itemName;
    private String itemStack;
    private int amount;
    private double balance;
    private double tax;

}
