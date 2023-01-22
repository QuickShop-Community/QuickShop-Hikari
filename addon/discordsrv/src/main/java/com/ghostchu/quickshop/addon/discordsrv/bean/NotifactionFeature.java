package com.ghostchu.quickshop.addon.discordsrv.bean;

import org.jetbrains.annotations.NotNull;

public enum NotifactionFeature {
    USER_SHOP_PERMISSION_CHANGED("notify-shop-permission-changed", "user.shop.permission_changed"),
    USER_SHOP_PRICE_CHANGED("notify-shop-price-changed", "user.shop.price_changed"),
    USER_SHOP_TRANSFER("notify-shop-transfer", "user.shop.transfer"),
    USER_SHOP_OUT_OF_STOCK("notify-shop-out-of-stock", "user.shop.out_of_stock"),
    USER_SHOP_OUT_OF_SPACE("notify-shop-out-of-space", "user.shop.out_of_space"),
    USER_SHOP_PURCHASE("notify-shop-purchase", "user.shop.purchase"),
    MOD_SHOP_PURCHASE("mod-notify-shop-purchase", "mod.shop.purchase"),
    MOD_SHOP_PRICE_CHANGED("mod-notify-shop-price-changed", "mod.shop.price_changed"),
    MOD_SHOP_REMOVED("mod-notify-shop-removed", "mod.shop.removed"),
    MOD_SHOP_TRANSFER("mod-notify-shop-transfer", "mod.shop.trasfer");
    private final String databaseNode;
    private final String configNode;

    NotifactionFeature(String configNode, String databaseNode) {
        this.configNode = configNode;
        this.databaseNode = databaseNode;
    }

    @NotNull
    public String getConfigNode() {
        return configNode;
    }

    @NotNull
    public String getDatabaseNode() {
        return databaseNode;
    }
}
