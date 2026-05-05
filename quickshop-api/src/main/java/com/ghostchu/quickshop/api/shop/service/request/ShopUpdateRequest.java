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

import com.ghostchu.quickshop.api.shop.ModernShop;
import com.ghostchu.quickshop.api.shop.service.ShopUpdateOptions;
import org.bukkit.command.CommandSender;

/**
 * ShopUpdateRequest
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class ShopUpdateRequest {

  protected final ShopUpdateOptions options;
  protected final CommandSender actor;
  protected final ModernShop<?, ?, ?, ?> shop;

  public ShopUpdateRequest(final ShopUpdateOptions options,
                           final CommandSender actor,
                           final ModernShop<?, ?, ?, ?> shop) {

    this.options = options;
    this.actor = actor;
    this.shop = shop;
  }

  public ShopUpdateRequest(final Builder builder) {
    this.options = builder.options;
    this.actor = builder.actor;
    this.shop = builder.shop;
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

  public ModernShop<?, ?, ?, ?> shop() {

    return shop;
  }

  public static final class Builder {

    private ShopUpdateOptions options;
    private CommandSender actor;
    private ModernShop<?, ?, ?, ?> shop;

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

    public Builder shop(final ModernShop<?, ?, ?, ?> shop) {
      this.shop = shop;
      return this;
    }

    public ShopUpdateRequest build() {
      return new ShopUpdateRequest(this);
    }
  }
}