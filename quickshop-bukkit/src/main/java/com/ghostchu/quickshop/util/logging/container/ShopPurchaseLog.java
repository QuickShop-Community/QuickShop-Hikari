package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.ShopInfoStorage;
import com.ghostchu.quickshop.api.shop.ShopType;
import com.ghostchu.quickshop.obj.QUserSimpleRecord;
import lombok.Data;

@Data
public class ShopPurchaseLog {
    private static int v = 2;
    private ShopInfoStorage shop;
    private ShopType type;
    private QUserSimpleRecord trader;
    private String itemName;
    private String itemStack;
    private int amount;
    private double balance;
    private double tax;

    public ShopPurchaseLog(ShopInfoStorage shop, ShopType type, QUser trader, String itemName, String itemStack, int amount, double balance, double tax) {
        this.shop = shop;
        this.type = type;
        this.trader = QUserSimpleRecord.wrap(trader);
        this.itemName = itemName;
        this.itemStack = itemStack;
        this.amount = amount;
        this.balance = balance;
        this.tax = tax;
    }
}
