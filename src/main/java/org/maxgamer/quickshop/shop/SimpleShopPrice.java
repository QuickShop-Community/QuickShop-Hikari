package org.maxgamer.quickshop.shop;

import lombok.AllArgsConstructor;
import org.maxgamer.quickshop.api.shop.ShopPrice;

@AllArgsConstructor
public final class SimpleShopPrice implements ShopPrice {
    private final double selling;
    private final double buying;

    @Override
    public double getSellingPrice() {
        return this.selling;
    }

    @Override
    public double getBuyingPrice() {
        return this.buying;
    }
}
