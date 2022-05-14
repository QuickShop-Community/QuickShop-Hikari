package com.ghostchu.quickshop.shop.permission;

import com.ghostchu.quickshop.api.shop.ShopPermissionAudience;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.ghostchu.quickshop.shop.permission.BuiltInShopPermission.*;

public enum BuiltInShopPermissionGroup implements ShopPermissionAudience {
    EVERYONE("everyone", "everyone", PURCHASE, SHOW_INFORMATION, PREVIEW_SHOP, SEARCH),
    STAFF("staff", "staff", ImmutableList.of(EVERYONE), ImmutableList.of(ACCESS_INVENTORY,
            TOGGLE_DISPLAY, SET_SHOPTYPE, SET_PRICE, SET_ITEM, SET_STACK_AMOUNT,
            SET_CURRENCY, RECEIVE_ALERT)),
    ADMINISTRATOR("administrator", "administrator", ImmutableList.of(STAFF, EVERYONE), ImmutableList.copyOf(BuiltInShopPermission.values()));

    BuiltInShopPermissionGroup(@NotNull String node, @NotNull String descriptionKey, @NotNull BuiltInShopPermission... permissions) {
        this.node = node;
        this.descriptionKey = descriptionKey;
        this.permissions = ImmutableList.copyOf(permissions);
        this.subGroups = Collections.emptyList();
    }

    BuiltInShopPermissionGroup(@NotNull String node, @NotNull String descriptionKey, @NotNull List<BuiltInShopPermissionGroup> subGroups, @NotNull List<BuiltInShopPermission> permissions) {
        this.node = node;
        this.descriptionKey = descriptionKey;
        this.permissions = ImmutableList.copyOf(permissions);
        this.subGroups = ImmutableList.copyOf(subGroups);
    }

    private final String node;
    private final String descriptionKey;
    private final List<BuiltInShopPermissionGroup> subGroups;
    private final List<BuiltInShopPermission> permissions;

    @Override
    public boolean hasPermission(@NotNull String permission) {
        List<BuiltInShopPermission> perm = bakeFlatPermissionList(this, new HashSet<>());
        List<String> node = perm.stream().map(BuiltInShopPermission::getNode).toList();
        return node.contains(permission);
    }

    /**
     * Check specific permission is/or contains the given permission.
     *
     * @param permission The permission to check.
     * @return True if the given permission is/or contains the given permission.
     */
    public boolean hasPermission(@NotNull BuiltInShopPermission permission) {
        List<BuiltInShopPermission> perm = bakeFlatPermissionList(this, new HashSet<>());
        return perm.contains(permission);
    }

    @Override
    public @NotNull String getName() {
        return this.descriptionKey;
    }

    @NotNull
    private List<BuiltInShopPermission> bakeFlatPermissionList(@NotNull BuiltInShopPermissionGroup group, @NotNull Set<BuiltInShopPermissionGroup> scanned) {
        // Initial with this group's permissions
        Set<BuiltInShopPermission> shopPermissions = new HashSet<>(group.getPermissions());
        // Add subgroups permissions to this flat permission list
        for (BuiltInShopPermissionGroup subGroup : subGroups) {
            // Dead-loop overflow check
            if (!scanned.add(subGroup)) continue;
            shopPermissions.addAll(bakeFlatPermissionList(subGroup, scanned));
        }
        return ImmutableList.copyOf(shopPermissions);
    }

    @NotNull
    public String getDescriptionKey() {
        return descriptionKey;
    }

    @NotNull
    public String getNode() {
        return node;
    }

    @NotNull
    public String getNamespacedNode() {
        return "quickshop." + this.node;
    }

    @NotNull
    public List<BuiltInShopPermission> getPermissions() {
        return permissions;
    }
}
