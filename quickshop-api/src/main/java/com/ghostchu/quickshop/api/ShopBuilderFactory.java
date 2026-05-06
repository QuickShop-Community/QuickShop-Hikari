package com.ghostchu.quickshop.api;

import com.ghostchu.quickshop.api.shop.builder.BuilderProvider;
import com.ghostchu.quickshop.api.shop.builder.ShopItemBuilder;
import com.ghostchu.quickshop.api.shop.builder.ShopMetaBuilder;
import com.ghostchu.quickshop.api.shop.builder.ShopPermissionBuilder;
import com.ghostchu.quickshop.api.shop.builder.ShopPriceBuilder;

public class ShopBuilderFactory {

  protected BuilderProvider<ShopItemBuilder> shopItemBuilder;
  protected BuilderProvider<ShopMetaBuilder> shopMetaBuilderBuilder;
  protected BuilderProvider<ShopPermissionBuilder> shopPermissionBuilderBuilder;
  protected BuilderProvider<ShopPriceBuilder<?>> shopPriceBuilder;

  public ShopBuilderFactory(BuilderProvider<ShopItemBuilder> shopItemBuilder,
                            BuilderProvider<ShopMetaBuilder> shopMetaBuilderBuilder,
                            BuilderProvider<ShopPermissionBuilder> shopPermissionBuilderBuilder,
                            BuilderProvider<ShopPriceBuilder<?>> shopPriceBuilder) {

    this.shopItemBuilder = shopItemBuilder;
    this.shopMetaBuilderBuilder = shopMetaBuilderBuilder;
    this.shopPermissionBuilderBuilder = shopPermissionBuilderBuilder;
    this.shopPriceBuilder = shopPriceBuilder;
  }

  public ShopItemBuilder shopItemBuilder() {

  }

  public ShopMetaBuilder shopMetaBuilder() {

  }

  public ShopPermissionBuilder shopPermissionBuilder() {

  }

  public ShopPriceBuilder<?> shopPriceBuilder() {

  }

  //setShopItemBuilder(SimpleShopItemBuilder::new)
  public ShopBuilderFactory setShopItemBuilder(final BuilderProvider<ShopItemBuilder> shopItemBuilder) {

    this.shopItemBuilder = shopItemBuilder;
    return this;
  }

  public ShopBuilderFactory setShopMetaBuilderBuilder(final BuilderProvider<ShopMetaBuilder> shopMetaBuilderBuilder) {

    this.shopMetaBuilderBuilder = shopMetaBuilderBuilder;
    return this;
  }

  public ShopBuilderFactory setShopPermissionBuilderBuilder(final BuilderProvider<ShopPermissionBuilder> shopPermissionBuilderBuilder) {

    this.shopPermissionBuilderBuilder = shopPermissionBuilderBuilder;
    return this;
  }

  public ShopBuilderFactory setShopPriceBuilder(final BuilderProvider<ShopPriceBuilder<?>> shopPriceBuilder) {

    this.shopPriceBuilder = shopPriceBuilder;
    return this;
  }
}