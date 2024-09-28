package com.ghostchu.quickshop.api.shop.permission;

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.shop.ShopPermissionAudience;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

import static com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission.ACCESS_INVENTORY;
import static com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission.PREVIEW_SHOP;
import static com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission.PURCHASE;
import static com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission.RECEIVE_ALERT;
import static com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission.SEARCH;
import static com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission.SET_BENEFIT;
import static com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission.SET_CURRENCY;
import static com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission.SET_ITEM;
import static com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission.SET_PRICE;
import static com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission.SET_SHOPTYPE;
import static com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission.SET_SIGN_TYPE;
import static com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission.SET_STACK_AMOUNT;
import static com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission.SHOW_INFORMATION;
import static com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission.TOGGLE_DISPLAY;
import static com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission.VIEW_PURCHASE_LOGS;

public enum BuiltInShopPermissionGroup implements ShopPermissionAudience {
  BLOCKED("blocked", "blocked"),
  EVERYONE("everyone", "everyone", PURCHASE, SHOW_INFORMATION, PREVIEW_SHOP, SEARCH),
  STAFF("staff", "staff", PURCHASE, SHOW_INFORMATION, PREVIEW_SHOP, SEARCH, ACCESS_INVENTORY,
        TOGGLE_DISPLAY, SET_SHOPTYPE, SET_PRICE, SET_ITEM, SET_STACK_AMOUNT,
        SET_CURRENCY, RECEIVE_ALERT, SET_BENEFIT, SET_SIGN_TYPE, VIEW_PURCHASE_LOGS),
  ADMINISTRATOR("administrator", "administrator", BuiltInShopPermission.values());

  private final String node;
  private final String descriptionKey;
  private final List<BuiltInShopPermission> permissions;

  BuiltInShopPermissionGroup(@NotNull String node, @NotNull String descriptionKey, @NotNull BuiltInShopPermission... permissions) {

    this.node = node;
    this.descriptionKey = descriptionKey;
    this.permissions = ImmutableList.copyOf(permissions);
  }

  @NotNull
  public String getDescriptionKey() {

    return descriptionKey;
  }

  @Override
  public @NotNull String getName() {

    return this.descriptionKey;
  }

  /**
   * Check specific permission is/or contains the given permission.
   *
   * @param permission The permission to check.
   *
   * @return True if the given permission is/or contains the given permission.
   */
  @Override
  public boolean hasPermission(@NotNull BuiltInShopPermission permission) {

    return this.getPermissions().contains(permission);
  }

  @Override
  public boolean hasPermission(@NotNull String permission) {

    return node.contains(permission);
  }

  @NotNull
  public List<BuiltInShopPermission> getPermissions() {

    return permissions;
  }

  @NotNull
  public String getNamespacedNode() {

    return QuickShopAPI.getPluginInstance().getName().toLowerCase(Locale.ROOT) + "." + this.node;
  }

  @NotNull
  public String getRawNode() {

    return node;
  }
}
