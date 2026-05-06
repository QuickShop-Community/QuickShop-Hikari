package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.api.shop.ModernShop;
import com.ghostchu.quickshop.api.shop.ShopWorldAdapter;
import com.ghostchu.quickshop.api.shop.builder.ShopBuilder;
import com.ghostchu.quickshop.api.shop.components.ShopInteraction;
import com.ghostchu.quickshop.api.shop.components.ShopItem;
import com.ghostchu.quickshop.api.shop.components.ShopLifecycle;
import com.ghostchu.quickshop.api.shop.components.ShopMeta;
import com.ghostchu.quickshop.api.shop.components.ShopPermission;
import com.ghostchu.quickshop.api.shop.components.ShopPrice;
import com.ghostchu.quickshop.api.shop.components.ShopTrading;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ModernContainerShop implements ModernShop<Double, Location, Player, InventoryPreview> {

  @Override
  public @NotNull UUID getRuntimeRandomUniqueId() {

    return null;
  }

  @Override
  public ShopItem item() {

    return null;
  }

  @Override
  public ShopInteraction<Player, InventoryPreview> interaction() {

    return null;
  }

  @Override
  public ShopLifecycle lifecycle() {

    return null;
  }

  @Override
  public ShopMeta meta() {

    return null;
  }

  @Override
  public ShopPermission permission() {

    return null;
  }

  @Override
  public ShopPrice<Double> price() {

    return null;
  }

  @Override
  public ShopTrading trading() {

    return null;
  }

  @Override
  public ShopBuilder<Double, Location, Player, InventoryPreview> asBuilder() {

    return null;
  }

  @Override
  public ShopWorldAdapter worldAdapter() {

    return null;
  }

  @Override
  public Location getLocation() {

    return null;
  }

  @Override
  public Location bukkitLocation() {

    return null;
  }
}
