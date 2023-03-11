package com.ghostchu.quickshop.addon.discordsrv.bean;

import org.jetbrains.annotations.NotNull;

public enum NotificationFeature {
    USER_SHOP_PERMISSION_CHANGED("notify-shop-permission-changed", "user.shop.permission_changed", true),
    USER_SHOP_PRICE_CHANGED("notify-shop-price-changed", "user.shop.price_changed", true),
    USER_SHOP_TRANSFER("notify-shop-transfer", "user.shop.transfer", true),
    USER_SHOP_OUT_OF_STOCK("notify-shop-out-of-stock", "user.shop.out_of_stock", true),
    USER_SHOP_OUT_OF_SPACE("notify-shop-out-of-space", "user.shop.out_of_space", true),
    USER_SHOP_PURCHASE("notify-shop-purchase", "user.shop.purchase", true),
    MOD_SHOP_PURCHASE("mod-notify-shop-purchase", "mod.shop.purchase", false),
    MOD_SHOP_PRICE_CHANGED("mod-notify-shop-price-changed", "mod.shop.price_changed", false),
    MOD_SHOP_REMOVED("mod-notify-shop-removed", "mod.shop.removed", false),
    MOD_SHOP_TRANSFER("mod-notify-shop-transfer", "mod.shop.trasfer", false);
    private final String databaseNode;
    private final String configNode;
    private final boolean playerToggleable;

    NotificationFeature(String configNode, String databaseNode, boolean playerToggleable) {
        this.configNode = configNode;
        this.databaseNode = databaseNode;
        this.playerToggleable = playerToggleable;
    }

    @NotNull
    public String getConfigNode() {
        return configNode;
    }

    @NotNull
    public String getDatabaseNode() {
        return databaseNode;
    }

    public boolean isPlayerToggleable() {
        return playerToggleable;
    }
}
