package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;

import java.util.UUID;

/**
 * Fire when quickshop processing the shop owner name
 */
public class ShopOwnerNameGettingEvent extends AbstractQSEvent {
    private final Shop shop;
    private final UUID owner;
    private Component name;

    public ShopOwnerNameGettingEvent(Shop shop, UUID owner, Component name) {
        this.shop = shop;
        this.owner = owner;
        this.name = name;
    }

    /**
     * Getting the shop owner display name
     *
     * @return The shop owner display name
     */
    public Component getName() {
        return name;
    }

    /**
     * Sets the shop owner display name
     *
     * @param name New shop owner display name, just display, won't change actual shop owner
     */
    public void setName(Component name) {
        this.name = name;
    }

    /**
     * Getting the shop owner unique id
     *
     * @return The shop owner unique id
     */
    public UUID getOwner() {
        return owner;
    }

    /**
     * Getting the shop that trying getting the shop owner name
     *
     * @return The shop
     */
    public Shop getShop() {
        return shop;
    }
}
