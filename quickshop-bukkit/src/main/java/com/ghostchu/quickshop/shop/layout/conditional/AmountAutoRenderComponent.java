package com.ghostchu.quickshop.shop.layout.conditional;

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
import com.ghostchu.quickshop.api.shop.layout.ConditionalRenderComponent;
import com.ghostchu.quickshop.api.shop.layout.RenderComponent;
import com.ghostchu.quickshop.shop.layout.inline.AmountRenderComponent;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * AmountRenderComponent
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class AmountAutoRenderComponent extends AmountRenderComponent implements ConditionalRenderComponent {

  @Override
  public String placeholder() {

    return "<amount_auto>";
  }

  @Override
  public boolean fullLine() {

    return false;
  }

  @Override
  public Component renderSnapshot(final @NotNull SignRenderSnapshot snapshot, final ProxiedLocale locale) {

    final QuickShop plugin = QuickShop.getInstance();

    final String baseKey = (snapshot.stackingShop())? snapshot.stackTradingKey() : snapshot.tradingKey();
    final String finalKey = (snapshot.overrideShopTypeText())? snapshot.shopStateKey() : baseKey;

    if(snapshot.remainingStock() == 0) {
      return (snapshot.overrideShopTypeText())? plugin.text().of(snapshot.shopStateKey()).forLocale(locale.getLocale())
                                              : plugin.text().of(snapshot.outOfStockKey()).forLocale(locale.getLocale());
    }

    if(snapshot.remainingStock() == -1) {
      return QuickShop.getInstance().text().of(finalKey, plugin.text().of("signs.unlimited").forLocale(locale.getLocale())).forLocale(locale.getLocale());
    }
    return Component.text(snapshot.itemAmount());
  }

  @Override
  public Component render(final @NotNull Shop shop, final @NotNull ItemStack item, final ProxiedLocale locale) {

    final QuickShop plugin = QuickShop.getInstance();

    final String tradingStringKey = (shop.isStackingShop()? shop.shopType().stackTradingTranslationKey() : shop.shopType().tradingTranslationKey());
    final String finalTradingStringKey = (shop.shopState().overrideShopTypeText())? shop.shopState().translationKey() : tradingStringKey;
    final String noRemainingStringKey = shop.shopType().outOfStockTranslationKey();
    final int remainingStock = shop.shopType().remainingStock(shop);

    if(remainingStock == 0) {
      if(shop.shopState().overrideShopTypeText()) {
        return plugin.text().of(shop.shopState().translationKey()).forLocale(locale.getLocale());
      }
      return plugin.text().of(noRemainingStringKey).forLocale(locale.getLocale());
    }

    if(remainingStock == -1) {
      return QuickShop.getInstance().text().of(finalTradingStringKey, plugin.text().of("signs.unlimited").forLocale(locale.getLocale())).forLocale(locale.getLocale());
    }

    return super.render(shop, item, locale);
  }

  /**
   * Determines if the specified shop represents a full-line replacement. A full-line replacement
   * indicates that no other rendering components will be processed for the same line, and rendering
   * will proceed to the next line.
   *
   * @param shop the shop instance to evaluate, must not be null
   *
   * @return {@code true} if the shop is a full-line replacement, {@code false} otherwise
   */
  @Override
  public boolean isFullLine(final Shop shop) {

    return shop.shopType().remainingStock(shop) <= 0;
  }

  /**
   * Determines if a specific render component for a shop sign is designated as a full-line
   * replacement. If it is a full-line replacement, this indicates that no other renderers will be
   * processed on the same line, and rendering proceeds to the next line.
   *
   * @param snapshot the {@link SignRenderSnapshot} containing data about the shop's current state, must
   *             not be null
   *
   * @return {@code true} if the render component is a full-line replacement, {@code false}
   * otherwise
   */
  @Override
  public boolean isFullLine(final SignRenderSnapshot snapshot) {

    return snapshot.remainingStock() <= 0;
  }
}