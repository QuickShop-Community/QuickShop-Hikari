package com.ghostchu.quickshop.api.event;

import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import com.ghostchu.quickshop.api.shop.Shop;

import java.util.UUID;

@AllArgsConstructor
public class ShopOwnerNameGettingEvent extends AbstractQSEvent {
    private final Shop shop;
    private final UUID owner;
    private Component name;

    /**
     * Getting the shop that trying getting the shop owner name
     *
     * @return The shop
     */
    public Shop getShop() {
        return shop;
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
}
