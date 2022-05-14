package com.ghostchu.quickshop.shop.permission;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.ShopPermissionAudience;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum BuiltInShopPermission implements ShopPermissionAudience {
    PURCHASE("purchase", "purchase"),
    SHOW_INFORMATION("show_information", "show-information"),
    PREVIEW_SHOP("preview_shop", "preview-shop"),
    SEARCH("search", "search"),
    DELETE("delete", "delete"),
    RECEIVE_ALERT("alert.receive", "receive-alert"),
    ACCESS_INVENTORY("access_inventory", "access-inventory"),
    OWNERSHIP_TRANSFER("ownership_transfer", "ownership-transfer"),
    MANAGEMENT_PERMISSION("management_permission", "management-permission"),
    TOGGLE_DISPLAY("toggle_display", "toggle-display"),
    SET_SHOPTYPE("set_shoptype", "set-shoptype"),
    SET_PRICE("set_price", "set-price"),
    SET_ITEM("set_item", "set-item"),
    SET_STACK_AMOUNT("set_stack_amount", "set-stack-amount"),
    SET_CURRENCY("set_currency", "set-currency"),
    SET_NAME("set_name", "set-name");

    private final String node;

    private final String descriptionKey;

    BuiltInShopPermission(@NotNull String node, @NotNull String descriptionKey) {
        this.node = node;
        this.descriptionKey = descriptionKey;
    }

    @NotNull
    public String getDescriptionKey() {
        return descriptionKey;
    }

    @NotNull
    public String getRawNode() {
        return node;
    }

    @NotNull
    public String getNamespacedNode() {
        return QuickShop.getInstance().getName().toLowerCase(Locale.ROOT) + this.node;
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return this.node.equals(permission);
    }

    @Override
    public boolean hasPermission(@NotNull BuiltInShopPermission permission) {
        return this == permission;
    }

    @Override
    public @NotNull String getName() {
        return this.descriptionKey;
    }
}
