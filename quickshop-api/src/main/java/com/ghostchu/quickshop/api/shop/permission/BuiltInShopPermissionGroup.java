package com.ghostchu.quickshop.api.shop.permission;

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.shop.ShopPermissionAudience;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

import static com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission.*;

public enum BuiltInShopPermissionGroup implements ShopPermissionAudience {
    BLOCKED("blocked", "blocked"),
    EVERYONE("everyone", "everyone", PURCHASE, SHOW_INFORMATION, PREVIEW_SHOP, SEARCH),
    STAFF("staff", "staff", PURCHASE, SHOW_INFORMATION, PREVIEW_SHOP, SEARCH, ACCESS_INVENTORY,
            TOGGLE_DISPLAY, SET_SHOPTYPE, SET_PRICE, SET_ITEM, SET_STACK_AMOUNT,
            SET_CURRENCY, RECEIVE_ALERT),
    ADMINISTRATOR("administrator", "administrator", BuiltInShopPermission.values());

    BuiltInShopPermissionGroup(@NotNull String node, @NotNull String descriptionKey, @NotNull BuiltInShopPermission... permissions) {
        this.node = node;
        this.descriptionKey = descriptionKey;
        this.permissions = ImmutableList.copyOf(permissions);
    }

    private final String node;
    private final String descriptionKey;
    private final List<BuiltInShopPermission> permissions;

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return node.contains(permission);
    }

    /**
     * Check specific permission is/or contains the given permission.
     *
     * @param permission The permission to check.
     * @return True if the given permission is/or contains the given permission.
     */
    @Override
    public boolean hasPermission(@NotNull BuiltInShopPermission permission) {
        return this.getPermissions().contains(permission);
    }

    @Override
    public @NotNull String getName() {
        return this.descriptionKey;
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
        return QuickShopAPI.getPluginInstance().getName().toLowerCase(Locale.ROOT) + "." + this.node;
    }

    @NotNull
    public List<BuiltInShopPermission> getPermissions() {
        return permissions;
    }
}
