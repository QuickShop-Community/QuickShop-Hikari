/*
 * This file is a part of project QuickShop, the name is ShopPurchaseEvent.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.api.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.api.shop.inventory.InventoryWrapper;

import java.util.UUID;

/**
 * Calling when purchaser purchased a shop
 */
public class ShopPurchaseEvent extends AbstractQSEvent implements Cancellable {

    @NotNull
    private final Shop shop;

    @NotNull
    private final UUID purchaser;

    @Nullable
    @Deprecated
    private final Player player;

    @NotNull
    private final InventoryWrapper purchaserInventory;

    private final int amount;
    private double total;

    private boolean cancelled;

    /**
     * Builds a new shop purchase event
     * Will called when purchase starting
     * For recording purchase, please listen to ShopSuccessPurchaseEvent.
     *
     * @param shop               The shop bought from
     * @param purchaser          The player buying, may offline if purchase by plugin
     * @param purchaserInventory The purchaseing target inventory, *MAY NOT A PLAYER INVENTORY IF PLUGIN PURCHASE THIS*
     * @param amount             The amount they're buying
     * @param total              The total balance in this purchase
     */
    public ShopPurchaseEvent(@NotNull Shop shop, @NotNull UUID purchaser, @NotNull InventoryWrapper purchaserInventory, int amount, double total) {
        this.shop = shop;
        this.purchaser = purchaser;
        this.purchaserInventory = purchaserInventory;
        this.amount = amount * shop.getItem().getAmount();
        this.total = total;
        this.player = Bukkit.getPlayer(purchaser);
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Gets the shop
     *
     * @return the shop
     */
    public @NotNull Shop getShop() {
        return this.shop;
    }

    /**
     * Gets the purchaser, that maybe is a online/offline/virtual player.
     *
     * @return The purchaser uuid
     */
    public @NotNull UUID getPurchaser() {
        return this.purchaser;
    }

    /**
     * Gets the purchaser
     *
     * @return Player or null if purchaser is offline/virtual player.
     * @deprecated Purchaser may is a online/offline/virtual player.
     */
    @Deprecated
    public @Nullable Player getPlayer() {
        return this.player;
    }

    /**
     * Gets the inventory of purchaser (the item will put to)
     *
     * @return The inventory
     */
    public @NotNull InventoryWrapper getPurchaserInventory() {
        return this.purchaserInventory;
    }

    /**
     * Gets the item stack amounts
     *
     * @return Item stack amounts
     */
    public int getAmount() {
        return this.amount;
    }

    /**
     * Gets the total money trans for
     *
     * @return Total money
     */
    public double getTotal() {
        return this.total;
    }

    /**
     * Sets new total money trans for
     *
     * @param total Total money
     */
    public void setTotal(double total) {
        this.total = total;
    }
}
