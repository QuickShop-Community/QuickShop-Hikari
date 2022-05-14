package com.ghostchu.quickshop.shop.permission;

import com.ghostchu.quickshop.api.shop.ShopPermissionAudience;
import org.jetbrains.annotations.NotNull;

public enum BuiltInShopPermission implements ShopPermissionAudience {
    PURCHASE("quickshop.purchase", "purchase"),
    SHOW_INFORMATION("quickshop.show_information", "show-information"),
    PREVIEW_SHOP("quickshop.preview_shop", "preview-shop"),
    SEARCH("quickshop.search", "search"),
    DELETE("quickshop.delete", "delete"),
    ACCESS_INVENTORY("quickshop.access_inventory", "access-inventory"),
    OWNERSHIP_TRANSFER("quickshop.ownership_transfer", "ownership-transfer"),
    MODIFY_MODERATORS("quickshop.modify_moderators", "modify-moderators"),
    MANAGEMENT_PERMISSION("quickshop.management_permission", "management-permission"),
    TOGGLE_DISPLAY("quickshop.toggle_display", "toggle-display"),
    TOGGLE_UNLIMITED("quickshop.toggle_unlimited", "toggle-unlimited"),
    SET_SHOPTYPE("quickshop.set_shoptype", "set-shoptype"),
    SET_PRICE("quickshop.set_price", "set-price"),
    SET_ITEM("quickshop.set_item", "set-item"),
    SET_STACK_AMOUNT("quickshop.set_stack_amount", "set-stack-amount"),
    SET_ALWAYS_COUNTING("quickshop.set_always_counting", "set-always-counting"),
    SET_TAX_ACCOUNT("quickshop.set_tax_account", "set-tax-account"),
    SET_CURRENCY("quickshop.set_currency", "set-currency");

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
    public String getNode() {
        return node;
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
