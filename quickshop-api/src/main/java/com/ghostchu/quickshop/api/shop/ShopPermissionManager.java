package com.ghostchu.quickshop.api.shop;

import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * Shop permission manager.
 * Comment: Something in QuickShop just like Flags in other plugins.
 */
public interface ShopPermissionManager {
    /**
     * Gets all groups was registered.
     *
     * @return Groups.
     */
    @NotNull List<String> getGroups();

    /**
     * Gets the permission list of a group.
     *
     * @param group The group.
     * @return The permission list.
     */
    @NotNull
    List<String> getGroupPermissions(@NotNull String group);

    /**
     * Check if specified group exists.
     *
     * @param group Group name.
     * @return True if exists.
     */
    boolean hasGroup(@NotNull String group);

    /**
     * Get permissions of specified group was granted.
     *
     * @param group      Group name.
     * @param permission Permission.
     * @return True if granted.
     */
    boolean hasPermission(@NotNull String group, @NotNull BuiltInShopPermission permission);

    /**
     * Get permissions of specified group was granted.
     *
     * @param group      Group name.
     * @param namespace  Plugin instance for namespace.
     * @param permission Permission name.
     * @return True if granted.
     */
    boolean hasPermission(@NotNull String group, @NotNull Plugin namespace, @NotNull String permission);

    /**
     * Register a group with specified permissions and name.
     *
     * @param group       Group name.
     * @param permissions Permissions.
     */
    void registerGroup(@NotNull String group, @NotNull Collection<String> permissions);

    /**
     * Register a permission to specified group.
     *
     * @param group      Group name.
     * @param namespace  Plugin instance for namespace.
     * @param permission Permission name.
     * @throws IllegalArgumentException throws if group not exists
     */
    void registerPermission(@NotNull String group, @NotNull Plugin namespace, @NotNull String permission);

    /**
     * Unregister specified group.
     *
     * @param group Group name.
     */
    void unregisterGroup(@NotNull String group);

    /**
     * Unregister specified permission from specified group.
     *
     * @param group      Group name.
     * @param namespace  Plugin instance for namespace.
     * @param permission Permission name.
     */
    void unregisterPermission(@NotNull String group, @NotNull Plugin namespace, @NotNull String permission);
}
