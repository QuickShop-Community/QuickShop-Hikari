package com.ghostchu.quickshop.shop.builder;

import com.ghostchu.quickshop.api.shop.ModernShop;
import com.ghostchu.quickshop.api.shop.builder.ShopItemBuilder;
import com.ghostchu.quickshop.api.shop.components.ShopItem;
import com.ghostchu.quickshop.shop.components.SimpleShopItem;
import org.bukkit.inventory.ItemStack;

public class SimpleShopItemBuilder implements ShopItemBuilder {

  protected ItemStack item;
  protected boolean disableDisplay;

  @Override
  public ItemStack item() {

    return item;
  }

  @Override
  public ShopItemBuilder item(final ItemStack item) {

    this.item = item;
    return this;
  }

  @Override
  public boolean isDisableDisplay() {

    return disableDisplay;
  }

  @Override
  public ShopItemBuilder disableDisplay(final boolean disabled) {

    this.disableDisplay = disabled;
    return this;
  }

  @Override
  public ShopItem build(final ModernShop<?, ?, ?, ?> shop) {

    return new SimpleShopItem(shop, this.item, this.disableDisplay);
  }
}