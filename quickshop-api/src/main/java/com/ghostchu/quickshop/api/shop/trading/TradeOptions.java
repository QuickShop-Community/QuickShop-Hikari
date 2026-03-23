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
          .sendMessages(false)
          .updateSigns(true)
          .build();

  public static final TradeOptions PREVIEW = builder()
          .sendMessages(false)
          .updateSigns(false)
          .callEvents(false)
          .commit(false)
          .allowRollback(false)
          .build();

  private final boolean sendMessages;
  private final boolean updateSigns;
  private final boolean callEvents;
  private final boolean allowRollback;
  private final boolean commit;

  private TradeOptions(final Builder builder) {

    this.sendMessages = builder.sendMessages;
    this.updateSigns = builder.updateSigns;
    this.callEvents = builder.callEvents;
    this.allowRollback = builder.allowRollback;
    this.commit = builder.commit;
  }

  public static Builder builder() {

    return new Builder();
  }

  public static Builder builder(final TradeOptions copy) {

    return new Builder(copy);
  }

  public boolean sendMessages() {

    return sendMessages;
  }

  public boolean updateSigns() {

    return updateSigns;
  }

  public boolean callEvents() {

    return callEvents;
  }

  public boolean allowRollback() {

    return allowRollback;
  }

  public boolean commit() {

    return commit;
  }

  public static final class Builder {

    private boolean sendMessages = true;
    private boolean updateSigns = true;
    private boolean callEvents = true;
    private boolean allowRollback = true;
    private boolean commit = true;

    private Builder() { }

    private Builder(final TradeOptions copy) {

      this.sendMessages = copy.sendMessages;
      this.updateSigns = copy.updateSigns;
      this.callEvents = copy.callEvents;
      this.allowRollback = copy.allowRollback;
      this.commit = copy.commit;
    }

    public Builder sendMessages(final boolean sendMessages) {

      this.sendMessages = sendMessages;
      return this;
    }

    public Builder updateSigns(final boolean updateSigns) {

      this.updateSigns = updateSigns;
      return this;
    }

    public Builder callEvents(final boolean callEvents) {

      this.callEvents = callEvents;
      return this;
    }

    public Builder allowRollback(final boolean allowRollback) {

      this.allowRollback = allowRollback;
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