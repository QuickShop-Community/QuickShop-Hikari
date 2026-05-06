package com.ghostchu.quickshop.api.shop.builder;

import com.ghostchu.quickshop.api.shop.components.ShopItem;
import com.ghostchu.quickshop.api.shop.components.ShopPrice;

public interface ShopPriceBuilder<T> {

  ShopPrice<T> build();
}