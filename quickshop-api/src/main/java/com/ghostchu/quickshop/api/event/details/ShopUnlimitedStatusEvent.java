package com.ghostchu.quickshop.api.event.details;
/*
 * QuickShop-Hikari
 * Copyright (C) 2024 Daniel "creatorfromhell" Vidmar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.ghostchu.quickshop.api.event.AbstractQSEvent;
import com.ghostchu.quickshop.api.event.QSCancellable;
import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ShopUnlimitedStatusEvent
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class ShopUnlimitedStatusEvent extends AbstractQSEvent implements QSCancellable {

  @NotNull
  private final Shop shop;

  private boolean unlimited;

  private boolean cancelled;

  private @Nullable Component cancelReason;

  /**
   * Call when shop was renaming.
   *
   * @param shop The shop bought from
   */
  public ShopUnlimitedStatusEvent(@NotNull final Shop shop, final boolean unlimited) {

    this.shop = shop;
    this.unlimited = unlimited;
  }

  @Override
  public @Nullable Component getCancelReason() {

    return this.cancelReason;
  }

  @Override
  public void setCancelled(final boolean cancel, @Nullable final Component reason) {

    this.cancelled = cancel;
    this.cancelReason = reason;
  }

  /**
   * The new unlimited status of the shop.
   *
   * @return true if the shop is changing to unlimited status, false otherwise
   */
  public boolean isUnlimited() {

    return unlimited;
  }

  /**
   * Set whether the shop has unlimited status.
   *
   * @param unlimited true if the shop should have unlimited status, false if not
   */
  public void setUnlimited(final boolean unlimited) {

    this.unlimited = unlimited;
  }

  /**
   * Getting the shops that clicked
   *
   * @return Clicked shop
   */
  public @NotNull Shop getShop() {

    return this.shop;
  }

  @Override
  public boolean isCancelled() {

    return this.cancelled;
  }
}
