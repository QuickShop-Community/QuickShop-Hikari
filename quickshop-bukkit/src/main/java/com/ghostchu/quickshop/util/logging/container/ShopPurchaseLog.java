package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.api.shop.ShopInfoStorage;
import com.ghostchu.quickshop.api.shop.ShopType;
import lombok.Data;

import java.util.UUID;

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

    public ShopPurchaseLog(ShopInfoStorage shop, ShopType type, UUID trader, String itemName, String itemStack, int amount, double balance, double tax) {
        this.shop = shop;
        this.type = type;
        this.trader = trader;
        this.itemName = itemName;
        this.itemStack = itemStack;
        this.amount = amount;
        this.balance = balance;
        this.tax = tax;
    }
}
