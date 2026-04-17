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

import com.ghostchu.quickshop.QuickShop;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

/**
 * Pagination
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public final class Pagination<I> {

  private final PaginationOptions<I> options;
  private int page;
  private int totalPages;

  public Pagination(final PaginationOptions<I> options) {
    this.options = options;

    this.totalPages = (options.entries().size() + options.maxPerPage() - 1) / options.maxPerPage();

    if(options.currentPage() < 1) {
      this.page = 1;
    } else if(options.currentPage() > totalPages) {
      this.page = totalPages;
    } else  {
      this.page = options.currentPage();
    }
  }

  public void print(final @Nullable CommandSender sender, final @Nullable Object[] headerArgs, final @Nullable Object[] footerArgs) {

    if(sender == null) return;

    if(headerArgs != null) {

      printHeader(sender, headerArgs);
    }

    printEntries(sender);

    if(footerArgs != null) {
      printFooter(sender, footerArgs);
    }
  }

  public void printHeader(final @Nullable CommandSender sender, @Nullable final Object... args) {

    QuickShop.getInstance().text().of(sender, options().headerLanguageKey(), args).send();
  }

  public void printFooter(final @Nullable CommandSender sender, @Nullable final Object... args) {

    QuickShop.getInstance().text().of(sender, options().footerLanguageKey(), args).send();
  }

  public void printEntries(final @Nullable CommandSender sender) {

    if(sender == null) return;

    final int start = (page - 1) * options.maxPerPage();
    final int end = Math.min(start + options.maxPerPage(), options.entries().size());
    for(int i = start; i < end; i++) {

      options.entryConsumer().accept(options.entries().get(i));
    }
  }

  public PaginationOptions<I> options() {

    return options;
  }

  public int page() {

    return page;
  }

  public int totalPages() {

    return totalPages;
  }

  public int previousPage() {
    if(page <= 1) {

      return totalPages;
    }
    return page - 1;
  }

  public int nextPage() {
    if(page >= totalPages) {

      return 1;
    }
    return page + 1;
  }
}