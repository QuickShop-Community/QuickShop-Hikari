package org.maxgamer.quickshop.api.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.maxgamer.quickshop.api.shop.Shop;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class ShopInventoryCalculateEvent extends AbstractQSEvent {
    private final Shop shop;
    private final int space;
    private final int stock;

    /**
     * Gets the shop that inventory has been calculated
     *
     * @return The shop
     */
    public Shop getShop() {
        return shop;
    }

    /**
     * Getting the inventory space
     *
     * @return The inventory space (-1 if not get calculated)
     */
    public int getSpace() {
        return space;
    }

    /**
     * Getting the inventory stock
     *
     * @return The inventory stock (-1 if not get calculated)
     */
    public int getStock() {
        return stock;
    }
}
