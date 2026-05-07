package com.ghostchu.quickshop.shop.builder;

import com.ghostchu.quickshop.api.shop.ModernShop;
import com.ghostchu.quickshop.api.shop.builder.ShopPriceBuilder;
import com.ghostchu.quickshop.api.shop.components.ShopPrice;
import com.ghostchu.quickshop.shop.components.SimpleShopPrice;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleShopPriceBuilder implements ShopPriceBuilder<Double> {

  protected Double price;
  protected String currency;

  @Override
  public Double price() {

    return price;
  }

  @Override
  public ShopPriceBuilder<Double> price(final @NotNull Double price) {

    this.price = price;
    return this;
  }

  @Override
  public @Nullable String currency() {

    return this.currency;
  }

  @Override
  public ShopPriceBuilder<Double> currency(final @Nullable String currency) {

    this.currency = currency;
    return this;
  }

  @Override
  public ShopPrice<Double> build(final ModernShop<?, ?, ?, ?> shop) {

    return new SimpleShopPrice(shop, this.currency, this.price);
  }
}