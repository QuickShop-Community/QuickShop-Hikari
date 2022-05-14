package com.ghostchu.quickshop.api.shop;

import com.ghostchu.quickshop.shop.permission.BuiltInShopPermission;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface ShopPermissionManager {
    void registerPermission(@NotNull String group, @NotNull Plugin namespace, @NotNull String permission);

    void unregisterPermission(@NotNull String group, @NotNull Plugin namespace, @NotNull String permission);

    boolean hasGroup(@NotNull String group);

    void registerGroup(@NotNull String group, @NotNull Collection<String> permissions);

    void unregisterGroup(@NotNull String group);

    boolean hasPermission(@NotNull String group, @NotNull Plugin namespace, @NotNull String permission);

    boolean hasPermission(@NotNull String group, @NotNull BuiltInShopPermission permission);

    @NotNull List<String> getGroups();
}
