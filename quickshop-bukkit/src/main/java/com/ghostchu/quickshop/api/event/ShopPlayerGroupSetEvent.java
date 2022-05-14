package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.Shop;
import org.jetbrains.annotations.NotNull;

public class ShopPlayerGroupSetEvent extends AbstractQSEvent {
    private final Shop shop;
    private final String oldGroup;
    private final String newGroup;

    public ShopPlayerGroupSetEvent(@NotNull Shop shop, @NotNull String oldGroup, @NotNull String newGroup) {
        this.shop = shop;
        this.oldGroup = oldGroup;
        this.newGroup = newGroup;
    }

    @NotNull
    public Shop getShop() {
        return shop;
    }

    @NotNull
    public String getNewGroup() {
        return newGroup;
    }

    @NotNull
    public String getOldGroup() {
        return oldGroup;
    }
}
