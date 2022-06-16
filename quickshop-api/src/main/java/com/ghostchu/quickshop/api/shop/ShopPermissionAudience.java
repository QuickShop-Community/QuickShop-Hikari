package com.ghostchu.quickshop.api.shop;

import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import org.jetbrains.annotations.NotNull;

/**
 * Permission Audience
 * Can be group or permission item
 */
public interface ShopPermissionAudience {
    /**
     * Check if audience has permission
     *
     * @param permission permission to check
     * @return true if has permission
     */
    boolean hasPermission(@NotNull String permission);

    /**
     * Check if audience has permission
     *
     * @param permission permission to check
     * @return true if has permission
     */
    boolean hasPermission(@NotNull BuiltInShopPermission permission);

    /**
     * Gets the name of the audience
     *
     * @return name of the audience, its may an i18n key or plain text.
     */
    @NotNull
    String getName();
}
