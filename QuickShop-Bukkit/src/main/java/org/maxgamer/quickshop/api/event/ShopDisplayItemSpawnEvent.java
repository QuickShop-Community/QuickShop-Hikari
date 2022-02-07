/*
 * This file is a part of project QuickShop, the name is ShopDisplayItemSpawnEvent.java
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

import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.api.shop.AbstractDisplayItem;
import org.maxgamer.quickshop.api.shop.DisplayType;
import org.maxgamer.quickshop.api.shop.Shop;

/**
 * This event is called before the shop display item created
 */
public class ShopDisplayItemSpawnEvent extends AbstractQSEvent implements Cancellable {

    @NotNull
    private final DisplayType displayType;

    @NotNull
    private final ItemStack itemStack;

    @NotNull
    private final Shop shop;

    private boolean cancelled;

    /**
     * This event is called before the shop display item created
     *
     * @param shop      Target shop
     * @param itemStack The ItemStack for spawning the displayItem
     */
    @Deprecated
    public ShopDisplayItemSpawnEvent(@NotNull Shop shop, @NotNull ItemStack itemStack) {
        this(shop, itemStack, AbstractDisplayItem.getNowUsing());
    }

    /**
     * This event is called before the shop display item created
     *
     * @param shop        Target shop
     * @param displayType The displayType
     * @param itemStack   Target ItemStack
     */
    public ShopDisplayItemSpawnEvent(
            @NotNull Shop shop, @NotNull ItemStack itemStack, @NotNull DisplayType displayType) {
        this.shop = shop;
        this.itemStack = itemStack;
        this.displayType = displayType;
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
     * Gets the current display type
     *
     * @return DisplayType
     */
    public @NotNull DisplayType getDisplayType() {
        return this.displayType;
    }

    /**
     * Gets the ItemStack used for display
     *
     * @return The display ItemStack
     */
    public @NotNull ItemStack getItemStack() {
        return this.itemStack;
    }

    /**
     * Gets the shop
     *
     * @return the shop
     */
    public @NotNull Shop getShop() {
        return this.shop;
    }
}
