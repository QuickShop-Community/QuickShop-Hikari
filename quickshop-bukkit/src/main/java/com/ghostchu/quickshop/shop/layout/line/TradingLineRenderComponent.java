package com.ghostchu.quickshop.shop.layout.line;

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
import com.ghostchu.quickshop.api.localization.text.ProxiedLocale;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.SignRenderSnapshot;
import com.ghostchu.quickshop.api.shop.layout.RenderComponent;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * TradingLineRenderComponent
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class TradingLineRenderComponent implements RenderComponent {

  @Override
  public String placeholder() {

    return "trading";
  }

  @Override
  public boolean fullLine() {

    return true;
  }

  @Override
  public boolean supportsSnapshot() {

    return true;
  }

  @Override
  public Component renderSnapshot(final @NotNull SignRenderSnapshot snapshot, final ProxiedLocale locale) {

    final QuickShop plugin = QuickShop.getInstance();

    final String baseKey = snapshot.stackingShop() ? snapshot.stackTradingKey() : snapshot.tradingKey();
    final String finalKey = snapshot.overrideShopTypeText() ? snapshot.shopStateKey() : baseKey;

    return switch (snapshot.remainingStock()) {
      case -1 -> plugin.text()
              .of(finalKey, plugin.text().of("signs.unlimited").forLocale(locale.getLocale()))
              .forLocale(locale.getLocale());

      case 0 -> snapshot.overrideShopTypeText()
                ? plugin.text().of(snapshot.shopStateKey()).forLocale(locale.getLocale())
                : plugin.text().of(snapshot.outOfStockKey()).forLocale(locale.getLocale());

      default -> plugin.text()
              .of(finalKey, Component.text(snapshot.remainingStock()))
              .forLocale(locale.getLocale());
    };
  }

  @Override
  public Component render(final @NotNull Shop shop, @NotNull final ItemStack item, final ProxiedLocale locale) {

    final QuickShop plugin = QuickShop.getInstance();

    final String tradingStringKey = (shop.isStackingShop()? shop.shopType().stackTradingTranslationKey() : shop.shopType().tradingTranslationKey());
    final String noRemainingStringKey = shop.shopType().outOfStockTranslationKey();
    final int shopRemaining = shop.shopType().remainingStock(shop);

    final String finalTradingStringKey = (shop.shopState().overrideShopTypeText())? shop.shopState().translationKey() : tradingStringKey;

    final Component trading = switch(shopRemaining) {
      //Unlimited
      case -1 -> plugin.text().of(finalTradingStringKey, plugin.text().of("signs.unlimited").forLocale(locale.getLocale())).forLocale(locale.getLocale());
      //No remaining
      case 0 -> {
        if(shop.shopState().overrideShopTypeText()) {
          yield plugin.text().of(shop.shopState().translationKey()).forLocale(locale.getLocale());
        }
        yield plugin.text().of(noRemainingStringKey).forLocale(locale.getLocale());
      }
      //Has remaining
      default -> plugin.text().of(finalTradingStringKey, Component.text(shopRemaining)).forLocale(locale.getLocale());
    };
    return trading;
  }
}
