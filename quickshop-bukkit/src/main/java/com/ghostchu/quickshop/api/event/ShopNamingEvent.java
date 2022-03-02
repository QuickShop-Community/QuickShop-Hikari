/*
 *  This file is a part of project QuickShop, the name is ShopClickEvent.java
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

import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Calling when shop clicked
 */
public class ShopNamingEvent extends AbstractQSEvent implements QSCancellable {

    @NotNull
    private final Shop shop;

    @Nullable
    private String name;

    private boolean cancelled;

    private @Nullable Component cancelReason;

    /**
     * Call when shop was renaming.
     *
     * @param shop The shop bought from
     */
    public ShopNamingEvent(@NotNull Shop shop,@NotNull String name) {
        this.shop = shop;
        this.name = name;
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
     * Getting the shops that clicked
     *
     * @return Clicked shop
     */
    public @NotNull Shop getShop() {
        return this.shop;
    }

    /**
     * Gets the new shop name
     * @return The new shop name, null if removing
     */
    @Nullable
    public String getName() {
        return name;
    }
}
