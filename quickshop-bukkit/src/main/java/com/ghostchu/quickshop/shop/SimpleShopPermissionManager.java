package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.shop.permission.BuiltInShopPermissionGroup;
import com.ghostchu.quickshop.util.logger.Log;
import com.google.common.collect.MapMaker;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

public class SimpleShopPermissionManager {
    private final Map<String, Set<String>> permissionMapping = new MapMaker().makeMap();

    public SimpleShopPermissionManager(@NotNull QuickShop plugin) {

    }

    private void initDefault() {
        for (BuiltInShopPermissionGroup group : BuiltInShopPermissionGroup.values()) {
            registerGroup(group.getName(), new CopyOnWriteArraySet<>(group.getPermissions().stream().map(perm -> "quickshop." + perm.getNode()).collect(Collectors.toSet())));
        }
        Log.debug("Initialized default permission mapping.");
    }

    public void registerPermission(@NotNull String group, @NotNull Plugin namespace, @NotNull String permission) {
        if (!permissionMapping.containsKey(group)) {
            throw new IllegalArgumentException("Group " + group + " does not exist.");
        }
        Log.debug("Register permission " + permission + " to group " + group);
        permissionMapping.get(group).add(namespace.getName().toLowerCase(Locale.ROOT) + "." + permission);
    }

    public void unregisterPermission(@NotNull String group, @NotNull Plugin namespace, @NotNull String permission) {
        if (!permissionMapping.containsKey(group)) {
            return;
        }
        Log.debug("Unregister permission " + permission + " from group " + group);
        permissionMapping.get(group).remove(namespace.getName().toLowerCase(Locale.ROOT) + "." + permission);
    }

    public void registerGroup(@NotNull String group, @NotNull Collection<String> permissions) {
        if (permissionMapping.containsKey(group)) {
            throw new IllegalArgumentException("Group " + group + " already exists.");
        }
        Log.debug("Register group " + group);
        permissionMapping.put(group, new CopyOnWriteArraySet<>(permissions));
    }

    public void unregisterGroup(@NotNull String group) {
        if (!permissionMapping.containsKey(group)) {
            return;
        }
        Log.debug("Unregister group " + group);
        permissionMapping.remove(group);
    }

    public boolean hasPermission(@NotNull String group, @NotNull Plugin namespace, @NotNull String permission) {
        if (!permissionMapping.containsKey(group)) {
            return false;
        }
        return permissionMapping.get(group).contains(namespace.getName().toLowerCase(Locale.ROOT) + "." + permission);
    }

    public boolean hasPermission(@NotNull String group, @NotNull BuiltInShopPermission permission) {
        return hasPermission(group, QuickShop.getInstance(), permission.getNode());
    }
}
