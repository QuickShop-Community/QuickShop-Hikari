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
import com.ghostchu.quickshop.util.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * PriceLineRenderComponent
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class PriceLineRenderComponent implements RenderComponent {

  @Override
  public String placeholder() {

    return "price";
  }

  @Override
  public boolean appliesTo(final @NotNull String line) {

    return line.contains(placeholder()) && !line.contains("price_amount") && !line.contains("price_alone");
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

    if (snapshot.stackingShop()) {
      return plugin.text()
              .of("signs.stack-price", snapshot.formattedPrice(), snapshot.itemAmount(), snapshot.itemName())
              .forLocale(locale.getLocale());
    }

    return plugin.text().of("signs.price", snapshot.formattedPrice()).forLocale(locale.getLocale());
  }

  @Override
  public Component render(final @NotNull Shop shop, @NotNull final ItemStack item, final ProxiedLocale locale) {

    final QuickShop plugin = QuickShop.getInstance();

    if(shop.isStackingShop()) {

      return plugin.text().of("signs.stack-price",
                              plugin.getShopManager().format(shop.getPrice(), shop),
                              item.getAmount(),
                              Util.getItemStackName(item)).forLocale(locale.getLocale());
    }

    return plugin.text().of("signs.price", plugin.getShopManager().format(shop.getPrice(), shop)).forLocale(locale.getLocale());
  }
}
