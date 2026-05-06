package com.ghostchu.quickshop.api.shop;

import com.ghostchu.quickshop.api.shop.builder.BuilderProvider;
import com.ghostchu.quickshop.api.shop.builder.ShopBuilder;
import com.ghostchu.quickshop.api.shop.builder.ShopItemBuilder;
import com.ghostchu.quickshop.api.shop.builder.ShopMetaBuilder;
import com.ghostchu.quickshop.api.shop.builder.ShopPermissionBuilder;
import com.ghostchu.quickshop.api.shop.builder.ShopPriceBuilder;

public class ShopBuilderFactory {

  protected BuilderProvider<ShopBuilder<?, ?, ?, ?>> shopBuilder;
  protected BuilderProvider<ShopItemBuilder> shopItemBuilder;
  protected BuilderProvider<ShopMetaBuilder> shopMetaBuilderBuilder;
  protected BuilderProvider<ShopPermissionBuilder> shopPermissionBuilderBuilder;
  protected BuilderProvider<ShopPriceBuilder<?>> shopPriceBuilder;

  public ShopBuilderFactory(final BuilderProvider<ShopBuilder<?, ?, ?, ?>> shopBuilder,
                            final BuilderProvider<ShopItemBuilder> shopItemBuilder,
                            final BuilderProvider<ShopMetaBuilder> shopMetaBuilderBuilder,
                            final BuilderProvider<ShopPermissionBuilder> shopPermissionBuilderBuilder,
                            final BuilderProvider<ShopPriceBuilder<?>> shopPriceBuilder) {

    this.shopBuilder = shopBuilder;
    this.shopItemBuilder = shopItemBuilder;
    this.shopMetaBuilderBuilder = shopMetaBuilderBuilder;
    this.shopPermissionBuilderBuilder = shopPermissionBuilderBuilder;
    this.shopPriceBuilder = shopPriceBuilder;
  }

  public ShopBuilder<?, ?, ?, ?> shopBuilder() {
    return shopBuilder.builder();
  }

  public ShopItemBuilder shopItemBuilder() {
    return shopItemBuilder.builder();
  }

  public ShopMetaBuilder shopMetaBuilder() {
    return shopMetaBuilderBuilder.builder();
  }

  public ShopPermissionBuilder shopPermissionBuilder() {
    return shopPermissionBuilderBuilder.builder();
  }

  public ShopPriceBuilder<?> shopPriceBuilder() {
    return shopPriceBuilder.builder();
  }

  public void setShopBuilder(final BuilderProvider<ShopBuilder<?, ?, ?, ?>> shopBuilder) {

    this.shopBuilder = shopBuilder;
  }

  //setShopItemBuilder(SimpleShopItemBuilder::new)
  public void setShopItemBuilder(final BuilderProvider<ShopItemBuilder> shopItemBuilder) {

    this.shopItemBuilder = shopItemBuilder;
  }

  public void setShopMetaBuilderBuilder(final BuilderProvider<ShopMetaBuilder> shopMetaBuilderBuilder) {

    this.shopMetaBuilderBuilder = shopMetaBuilderBuilder;
  }

  public void setShopPermissionBuilderBuilder(final BuilderProvider<ShopPermissionBuilder> shopPermissionBuilderBuilder) {

    this.shopPermissionBuilderBuilder = shopPermissionBuilderBuilder;
  }

  public void setShopPriceBuilder(final BuilderProvider<ShopPriceBuilder<?>> shopPriceBuilder) {

    this.shopPriceBuilder = shopPriceBuilder;
  }
}