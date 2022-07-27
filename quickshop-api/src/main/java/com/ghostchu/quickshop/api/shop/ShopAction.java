package com.ghostchu.quickshop.api.shop;

/**
 * The actions about player and shop
 */
public enum ShopAction {
    // buy = trading create = creating shop cancelled = stopped
    PURCHASE_BUY,
    PURCHASE_SELL,
    CREATE_SELL,
    CREATE_BUY,
    CANCELLED;

    public boolean isTrading() {
        return this == PURCHASE_BUY || this == PURCHASE_SELL;
    }

    public boolean isCreating() {
        return this == CREATE_SELL || this == CREATE_BUY;
    }
}
