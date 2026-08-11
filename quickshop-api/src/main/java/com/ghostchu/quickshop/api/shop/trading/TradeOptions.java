package com.ghostchu.quickshop.api.shop.trading;

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

/**
 * TradeOptions
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public final class TradeOptions {

  public static final TradeOptions DEFAULT = builder().build();

  public static final TradeOptions SILENT = builder()
          .updateSigns(true)
          .build();

  public static final TradeOptions PREVIEW = builder()
          .updateSigns(false)
          .commit(false)
          .build();

  private final boolean updateSigns;
  private final boolean commit;

  private TradeOptions(final Builder builder) {

    this.updateSigns = builder.updateSigns;
    this.commit = builder.commit;
  }

  public static Builder builder() {

    return new Builder();
  }

  public static Builder builder(final TradeOptions copy) {

    return new Builder(copy);
  }

  public boolean updateSigns() {

    return updateSigns;
  }

  public boolean commit() {

    return commit;
  }

  public static final class Builder {

    private boolean updateSigns = true;
    private boolean commit = true;

    private Builder() { }

    private Builder(final TradeOptions copy) {

      this.updateSigns = copy.updateSigns;
      this.commit = copy.commit;
    }

    public Builder updateSigns(final boolean updateSigns) {

      this.updateSigns = updateSigns;
      return this;
    }

    public Builder commit(final boolean commit) {

      this.commit = commit;
      return this;
    }

    public TradeOptions build() {

      return new TradeOptions(this);
    }
  }
}