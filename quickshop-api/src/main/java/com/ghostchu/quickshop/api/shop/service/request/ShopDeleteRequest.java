package com.ghostchu.quickshop.api.shop.service.request;

/*
 * QuickShop-Hikari
 * Copyright (C) 2026 Daniel "creatorfromhell" Vidmar
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

import com.ghostchu.quickshop.api.shop.service.ShopRequest;
import com.ghostchu.quickshop.api.shop.service.ShopUpdateOptions;
import org.bukkit.command.CommandSender;

/**
 * ShopDeleteRequest
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class ShopDeleteRequest implements ShopRequest {

  protected final ShopUpdateOptions options;
  protected final CommandSender actor;
  protected final long shopId;

  public ShopDeleteRequest(final ShopUpdateOptions options,
                           final CommandSender actor,
                           final long shopId) {

    this.options = options;
    this.actor = actor;
    this.shopId = shopId;
  }

  public ShopDeleteRequest(final Builder builder) {
    this.options = builder.options;
    this.actor = builder.actor;
    this.shopId = builder.shopId;
  }

  public static Builder builder() {

    return new Builder();
  }

  public ShopUpdateOptions options() {

    return options;
  }

  public CommandSender actor() {

    return actor;
  }

  @Override
  public long shopId() {

    return shopId;
  }

  public static final class Builder {

    private ShopUpdateOptions options;
    private CommandSender actor;
    private long shopId;

    private Builder() {
    }
    public Builder options(final ShopUpdateOptions options) {
      this.options = options;
      return this;
    }

    public Builder actor(final CommandSender actor) {
      this.actor = actor;
      return this;
    }

    public Builder shopId(final long shopId) {
      this.shopId = shopId;
      return this;
    }

    public ShopDeleteRequest build() {
      return new ShopDeleteRequest(this);
    }
  }
}