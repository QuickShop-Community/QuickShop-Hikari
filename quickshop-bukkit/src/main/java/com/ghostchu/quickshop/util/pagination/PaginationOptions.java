package com.ghostchu.quickshop.util.pagination;

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

import com.ghostchu.quickshop.api.shop.trading.TradeOptions;

import java.util.List;
import java.util.function.Consumer;

/**
 * PaginationOptions
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public final class PaginationOptions<I> {

  private final List<I> entries;
  private final String headerLanguageKey;
  private final String footerLanguageKey;
  private final String command;
  private final int maxPerPage;
  private final int currentPage;
  private final Consumer<I> entryConsumer;

  private PaginationOptions(final Builder<I> builder) {

    this.entries = builder.entries;
    this.headerLanguageKey = builder.headerLanguageKey;
    this.footerLanguageKey = builder.footerLanguageKey;
    this.command = builder.command;
    this.maxPerPage = builder.maxPerPage;
    this.currentPage = builder.currentPage;
    this.entryConsumer = builder.entryConsumer;
  }

  public static Builder builder() {

    return new Builder();
  }

  public static Builder builder(final PaginationOptions copy) {

    return new Builder(copy);
  }

  public List<I> entries() {

    return entries;
  }

  public String headerLanguageKey() {

    return headerLanguageKey;
  }

  public String footerLanguageKey() {

    return footerLanguageKey;
  }

  public String command() {

    return command;
  }

  public int maxPerPage() {

    return maxPerPage;
  }

  public int currentPage() {

    return currentPage;
  }

  public Consumer<I> entryConsumer() {

    return entryConsumer;
  }

  public static final class Builder<I> {

    private List<I> entries;
    private String headerLanguageKey;
    private String footerLanguageKey;
    private String command;
    private int maxPerPage;
    private int currentPage;
    private Consumer<I> entryConsumer;

    private Builder() { }

    public Builder(final PaginationOptions<I> copy) {

      this.entries = copy.entries;
      this.headerLanguageKey = copy.headerLanguageKey;
      this.footerLanguageKey = copy.footerLanguageKey;
      this.command = copy.command;
      this.maxPerPage = copy.maxPerPage;
      this.currentPage = copy.currentPage;
      this.entryConsumer = copy.entryConsumer;
    }

    public Builder<I> setEntries(final List<I> entries) {

      this.entries = entries;
      return this;
    }

    public Builder<I> setHeaderLanguageKey(final String headerLanguageKey) {

      this.headerLanguageKey = headerLanguageKey;
      return this;
    }

    public Builder<I> setFooterLanguageKey(final String footerLanguageKey) {

      this.footerLanguageKey = footerLanguageKey;
      return this;
    }

    public Builder<I> setCommand(final String command) {

      this.command = command;
      return this;
    }

    public Builder<I> setMaxPerPage(final int maxPerPage) {

      this.maxPerPage = maxPerPage;
      return this;
    }

    public Builder<I> setCurrentPage(final int currentPage) {

      this.currentPage = currentPage;
      return this;
    }

    public Builder<I> setEntryConsumer(final Consumer<I> entryConsumer) {

      this.entryConsumer = entryConsumer;
      return this;
    }

    public PaginationOptions<I> build() {

      return new PaginationOptions<>(this);
    }
  }
}