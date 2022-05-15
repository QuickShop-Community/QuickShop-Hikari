/*
 *  This file is a part of project QuickShop, the name is ShopDisplayItemSpawnEvent.java
 *  Copyright (C) Ghost_chu and contributors
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

package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.AbstractDisplayItem;
import com.ghostchu.quickshop.api.shop.DisplayType;
import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This event is called before the shop display item created
 */
public class ShopDisplayItemSpawnEvent extends AbstractQSEvent implements QSCancellable {

    @NotNull
    private final DisplayType displayType;

    @NotNull
    private final ItemStack itemStack;

    @NotNull
    private final Shop shop;

    private boolean cancelled;
    private @Nullable Component cancelReason;

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
    public void setCancelled(boolean cancel, @Nullable Component reason) {
        this.cancelled = cancel;
        this.cancelReason = reason;
    }

    @Override
    public @Nullable Component getCancelReason() {
        return this.cancelReason;
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
