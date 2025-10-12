package com.ghostchu.quickshop.api.shop.parser;


/*
 * QuickShop-Hikari
 * Copyright (C) 2025 Daniel "creatorfromhell" Vidmar
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

import com.ghostchu.quickshop.api.QuickShopProvider;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

/**
 * ParserContext
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class ParserContext {

  private final QuickShopProvider provider;

  public ParserContext(final QuickShopProvider provider) {
    this.provider = provider;
  }

  public QuickShopProvider provider() {

    return provider;
  }

  public Component translate(final String key, final @Nullable Object... args) {

    return provider.getApiInstance().getTextManager().of(key, args).forLocale();
  }
}