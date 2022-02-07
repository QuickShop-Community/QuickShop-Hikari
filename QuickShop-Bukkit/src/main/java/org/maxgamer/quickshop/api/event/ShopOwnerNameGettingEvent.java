package org.maxgamer.quickshop.api.event;

import lombok.AllArgsConstructor;
import org.maxgamer.quickshop.api.shop.Shop;

import java.util.UUID;

@AllArgsConstructor
public class ShopOwnerNameGettingEvent extends AbstractQSEvent {
    private final Shop shop;
    private final UUID owner;
    private String name;

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
    public String getName() {
        return name;
    }

    /**
     * Sets the shop owner display name
     *
     * @param name New shop owner display name, just display, won't change actual shop owner
     */
    public void setName(String name) {
        this.name = name;
    }
}
