package com.ghostchu.quickshop.shop.builder;

import com.ghostchu.quickshop.api.shop.ModernShop;
import com.ghostchu.quickshop.api.shop.builder.ShopPermissionBuilder;
import com.ghostchu.quickshop.api.shop.components.ShopPermission;
import com.ghostchu.quickshop.shop.components.SimpleShopPermission;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SimpleShopPermissionsBuilder implements ShopPermissionBuilder {

  protected Map<UUID, String> permissions = new HashMap<>();

  @Override
  public @NotNull Map<UUID, String> permissions() {

    return permissions;
  }

  @Override
  public ShopPermissionBuilder permissions(@NotNull final Map<UUID, String> permissions) {

    this.permissions = permissions;
    return this;
  }

  @Override
  public ShopPermissionBuilder permission(@NotNull final UUID uuid, @NotNull final String group) {

    this.permissions.put(uuid, group);
    return this;
  }

  @Override
  public ShopPermission build(final ModernShop<?, ?, ?, ?> shop) {

    return new SimpleShopPermission(shop, this.permissions);
  }
}