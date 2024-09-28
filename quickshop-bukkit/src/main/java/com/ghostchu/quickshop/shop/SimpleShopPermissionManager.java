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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;

public class SimpleShopPermissionManager implements ShopPermissionManager, Reloadable {

  private final Map<String, Set<String>> permissionMapping = new MapMaker().makeMap();
  private final QuickShop plugin;

  public SimpleShopPermissionManager(@NotNull final QuickShop plugin) {

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
  public @NotNull List<String> getGroupPermissions(@NotNull final String group) {

    final Set<String> set = this.permissionMapping.get(group);
    if(set == null) {
      return Collections.emptyList();
    }
    return ImmutableList.copyOf(set);
  }

  @Override
  public boolean hasGroup(@NotNull final String group) {

    return permissionMapping.containsKey(group);
  }

  @Override
  public boolean hasPermission(@NotNull final String group, @NotNull final BuiltInShopPermission permission) {

    return hasPermission(group, plugin.getJavaPlugin(), permission.getRawNode());
  }

  @Override
  public boolean hasPermission(@NotNull final String group, @NotNull final Plugin namespace, @NotNull final String permission) {

    if(!permissionMapping.containsKey(group)) {
      return false;
    }
    final String fullPermissionPath = namespace.getName().toLowerCase(Locale.ROOT) + "." + permission;
    final boolean result = permissionMapping.get(group).contains(fullPermissionPath);
    Log.permission("Check permission " + fullPermissionPath + " for group " + group + ": " + result);
    return result;
  }

  @Override
  public void registerGroup(@NotNull final String group, @NotNull final Collection<String> permissions) {

    if(permissionMapping.containsKey(group)) {
      throw new IllegalArgumentException("Group " + group + " already exists.");
    }
    Log.permission("Register group " + group);
    permissionMapping.put(group, new CopyOnWriteArraySet<>(permissions));
  }

  @Override
  public void registerPermission(@NotNull final String group, @NotNull final Plugin namespace, @NotNull final String permission) {

    if(!permissionMapping.containsKey(group)) {
      throw new IllegalArgumentException("Group " + group + " does not exist.");
    }
    final String fullPermissionPath = namespace.getName().toLowerCase(Locale.ROOT) + "." + permission;
    Log.permission("Register permission " + fullPermissionPath + " to group " + group);
    permissionMapping.get(group).add(fullPermissionPath);
  }

  @Override
  public void unregisterGroup(@NotNull final String group) {

    if(!permissionMapping.containsKey(group)) {
      return;
    }
    Log.permission("Unregister group " + group);
    permissionMapping.remove(group);
  }

  @Override
  public void unregisterPermission(@NotNull final String group, @NotNull final Plugin namespace, @NotNull final String permission) {

    if(!permissionMapping.containsKey(group)) {
      return;
    }
    final String fullPermissionPath = namespace.getName().toLowerCase(Locale.ROOT) + "." + permission;
    Log.permission("Unregister permission " + fullPermissionPath + " from group " + group);
    permissionMapping.get(group).remove(fullPermissionPath);
  }

  private void initDefaultConfiguration(@NotNull final File file) {

    final YamlConfiguration yamlConfiguration = new YamlConfiguration();
    yamlConfiguration.set("version", 1);
    for(final BuiltInShopPermissionGroup group : BuiltInShopPermissionGroup.values()) {
      yamlConfiguration.set(group.getNamespacedNode(), group.getPermissions().stream().map(BuiltInShopPermission::getNamespacedNode).toList());
    }
    try {
      //noinspection ResultOfMethodCallIgnored
      file.createNewFile();
      yamlConfiguration.save(file);
    } catch(Exception e) {
      Log.permission(Level.SEVERE, "Failed to create default group configuration file");
      plugin.logger().error("Failed to create default group configuration", e);
    }
  }

  private void loadConfiguration() {

    Log.permission("Loading group configuration...");
    permissionMapping.clear();
    final File file = new File(plugin.getDataFolder(), "group.yml");
    if(!file.exists()) {
      initDefaultConfiguration(file);
    }
    final YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
    final String namespace = plugin.getJavaPlugin().getName().toLowerCase(Locale.ROOT);
    if(!yamlConfiguration.isSet(namespace + ".everyone")
       || !yamlConfiguration.isSet(namespace + ".staff")
       || !yamlConfiguration.isSet(namespace + ".blocked")) {
      plugin.logger().warn("Corrupted group configuration file, creating new one...");
      try {
        Files.move(file.toPath(), file.toPath().resolveSibling(file.getName() + ".corrupted." + UUID.randomUUID().toString().replace("-", "")));
        loadConfiguration();

      } catch(IOException e) {
        plugin.logger().error("Failed to move corrupted group configuration file", e);
      }
    }

    try {
      updateConfiguration(yamlConfiguration, file, namespace);
    } catch(IOException e) {
      plugin.logger().error("Failed to update group configuration file", e);
    }

    yamlConfiguration.getKeys(true).forEach(group->{
      if(yamlConfiguration.isList(group)) {
        final List<String> perms = yamlConfiguration.getStringList(group);
        this.permissionMapping.put(group, new HashSet<>(perms));
        Log.permission("Permission loaded for group " + group + ": " + CommonUtil.list2String(perms));
      }
    });
  }

  private void updateConfiguration(final YamlConfiguration yamlConfiguration, final File file, final String namespace) throws IOException {

    if(yamlConfiguration.getInt("version", 1) == 1) {
      List<String> perms = yamlConfiguration.getStringList(namespace + ".staff");
      perms.add(BuiltInShopPermission.SET_SIGN_TYPE.getNamespacedNode());
      perms.add(BuiltInShopPermission.VIEW_PURCHASE_LOGS.getNamespacedNode());
      perms = yamlConfiguration.getStringList(namespace + ".administrator");
      perms.add(BuiltInShopPermission.SET_SIGN_TYPE.getNamespacedNode());
      perms.add(BuiltInShopPermission.VIEW_PURCHASE_LOGS.getNamespacedNode());
      yamlConfiguration.set("version", 2);
      yamlConfiguration.save(file);
    }
  }

  @Override
  public ReloadResult reloadModule() throws Exception {

    this.loadConfiguration();
    return Reloadable.super.reloadModule();
  }
}
