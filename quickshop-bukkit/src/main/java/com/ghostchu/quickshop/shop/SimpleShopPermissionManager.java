package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.ShopPermissionManager;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermissionGroup;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.Reloadable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapMaker;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;

public class SimpleShopPermissionManager implements ShopPermissionManager, Reloadable {
    private final Map<String, Set<String>> permissionMapping = new MapMaker().makeMap();
    private final QuickShop plugin;

    public SimpleShopPermissionManager(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        loadConfiguration();
        plugin.getReloadManager().register(this);
    }

    @Override
    @NotNull
    public List<String> getGroups() {
        return ImmutableList.copyOf(this.permissionMapping.keySet());
    }

    @Override
    public @NotNull List<String> getGroupPermissions(@NotNull String group) {
        Set<String> set = this.permissionMapping.get(group);
        if (set == null) {
            return Collections.emptyList();
        }
        return ImmutableList.copyOf(set);
    }

    @Override
    public boolean hasGroup(@NotNull String group) {
        return permissionMapping.containsKey(group);
    }

    @Override
    public boolean hasPermission(@NotNull String group, @NotNull BuiltInShopPermission permission) {
        return hasPermission(group, plugin.getJavaPlugin(), permission.getRawNode());
    }

    @Override
    public boolean hasPermission(@NotNull String group, @NotNull Plugin namespace, @NotNull String permission) {
        if (!permissionMapping.containsKey(group)) {
            return false;
        }
        String fullPermissionPath = namespace.getName().toLowerCase(Locale.ROOT) + "." + permission;
        boolean result = permissionMapping.get(group).contains(fullPermissionPath);
        Log.permission("Check permission " + fullPermissionPath + " for group " + group + ": " + result);
        return result;
    }

    @Override
    public void registerGroup(@NotNull String group, @NotNull Collection<String> permissions) {
        if (permissionMapping.containsKey(group)) {
            throw new IllegalArgumentException("Group " + group + " already exists.");
        }
        Log.permission("Register group " + group);
        permissionMapping.put(group, new CopyOnWriteArraySet<>(permissions));
    }

    @Override
    public void registerPermission(@NotNull String group, @NotNull Plugin namespace, @NotNull String permission) {
        if (!permissionMapping.containsKey(group)) {
            throw new IllegalArgumentException("Group " + group + " does not exist.");
        }
        String fullPermissionPath = namespace.getName().toLowerCase(Locale.ROOT) + "." + permission;
        Log.permission("Register permission " + fullPermissionPath + " to group " + group);
        permissionMapping.get(group).add(fullPermissionPath);
    }

    @Override
    public void unregisterGroup(@NotNull String group) {
        if (!permissionMapping.containsKey(group)) {
            return;
        }
        Log.permission("Unregister group " + group);
        permissionMapping.remove(group);
    }

    @Override
    public void unregisterPermission(@NotNull String group, @NotNull Plugin namespace, @NotNull String permission) {
        if (!permissionMapping.containsKey(group)) {
            return;
        }
        String fullPermissionPath = namespace.getName().toLowerCase(Locale.ROOT) + "." + permission;
        Log.permission("Unregister permission " + fullPermissionPath + " from group " + group);
        permissionMapping.get(group).remove(fullPermissionPath);
    }

    private void initDefaultConfiguration(@NotNull File file) {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        yamlConfiguration.set("version", 1);
        for (BuiltInShopPermissionGroup group : BuiltInShopPermissionGroup.values()) {
            yamlConfiguration.set(group.getNamespacedNode(), group.getPermissions().stream().map(BuiltInShopPermission::getNamespacedNode).toList());
        }
        try {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
            yamlConfiguration.save(file);
        } catch (Exception e) {
            Log.permission(Level.SEVERE, "Failed to create default group configuration file");
            plugin.logger().error("Failed to create default group configuration", e);
        }
    }

    private void loadConfiguration() {
        Log.permission("Loading group configuration...");
        permissionMapping.clear();
        File file = new File(plugin.getDataFolder(), "group.yml");
        if (!file.exists()) {
            initDefaultConfiguration(file);
        }
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        String namespace = plugin.getJavaPlugin().getName().toLowerCase(Locale.ROOT);
        if (!yamlConfiguration.isSet(namespace + ".everyone")
                || !yamlConfiguration.isSet(namespace + ".staff")
                || !yamlConfiguration.isSet(namespace + ".blocked")) {
            plugin.logger().warn("Corrupted group configuration file, creating new one...");
            try {
                Files.move(file.toPath(), file.toPath().resolveSibling(file.getName() + ".corrupted." + UUID.randomUUID().toString().replace("-", "")));
                loadConfiguration();
            } catch (IOException e) {
                plugin.logger().error("Failed to move corrupted group configuration file", e);
            }
        }
        yamlConfiguration.getKeys(true).forEach(group -> {
            if (yamlConfiguration.isList(group)) {
                List<String> perms = yamlConfiguration.getStringList(group);
                this.permissionMapping.put(group, new HashSet<>(perms));
                Log.permission("Permission loaded for group " + group + ": " + CommonUtil.list2String(perms));
            }
        });
    }

    @Override
    public ReloadResult reloadModule() throws Exception {
        this.loadConfiguration();
        return Reloadable.super.reloadModule();
    }
}
