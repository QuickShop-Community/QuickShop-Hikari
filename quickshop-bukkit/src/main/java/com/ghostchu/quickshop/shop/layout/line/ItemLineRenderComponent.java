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
 * ItemLineRenderComponent
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class ItemLineRenderComponent implements RenderComponent {

  @Override
  public String placeholder() {

    return "item";
  }

  @Override
  public boolean appliesTo(final @NotNull String line) {

    return line.contains(placeholder()) && !line.contains("item_name") && !line.contains("item_level");
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

    final Component left = plugin.text().of("signs.item-left").forLocale(locale.getLocale());
    final Component right = plugin.text().of("signs.item-right").forLocale(locale.getLocale());

    return left.append(snapshot.itemName()).append(right);
  }

  @Override
  public Component render(final @NotNull Shop shop, @NotNull final ItemStack item, final ProxiedLocale locale) {

    final QuickShop plugin = QuickShop.getInstance();

    if(plugin.getConfig().getBoolean("shop.force-use-item-original-name")
       || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {

      final Component left = plugin.text().of("signs.item-left").forLocale(locale.getLocale());
      final Component right = plugin.text().of("signs.item-right").forLocale(locale.getLocale());
      final Component itemName = Util.getItemStackName(item, locale.getLocale());

      return left.append(itemName).append(right);
    }

    return plugin.text().of("signs.item-left").forLocale(locale.getLocale())
            .append(Util.getItemStackName(item, locale.getLocale())
                            .append(plugin.text().of("signs.item-right").forLocale(locale.getLocale())));
  }
}