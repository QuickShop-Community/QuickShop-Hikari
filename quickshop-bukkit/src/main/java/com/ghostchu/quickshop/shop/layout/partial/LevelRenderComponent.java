package com.ghostchu.quickshop.shop.layout.partial;

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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * LevelRenderComponent
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class LevelRenderComponent implements RenderComponent {

  @Override
  public String placeholder() {

    return "item_level";
  }

  @Override
  public boolean fullLine() {

    return false;
  }

  @Override
  public boolean supportsSnapshot() {

    return true;
  }

  @Override
  public Component renderSnapshot(final @NotNull SignRenderSnapshot snapshot, final ProxiedLocale locale) {

    return snapshot.levelLine();
  }

  @Override
  public Component render(final @NotNull Shop shop, @NotNull final ItemStack item, final ProxiedLocale locale) {

    final QuickShop plugin = QuickShop.getInstance();

    final PotionEffect effect = Util.getFirstPotionEffect(item);
    if(effect != null) {

      return plugin.text().of("signs.item-duration", Util.getPotionDuration(effect)).forLocale(locale.getLocale());
    }

    final Map.Entry<Enchantment, Integer> enchantment = Util.getFirstEnchantment(item);
    if(enchantment != null) {

      return plugin.text().of("signs.item-level", enchantment.getValue()).forLocale(locale.getLocale());
    }

    //TODO: Parser API? Tying rendering to specific parts, or adding custom rendering logic
    if(item.getItemMeta() instanceof final FireworkMeta fireworkMeta) {

      return plugin.text().of("signs.item-duration", fireworkMeta.getPower()).forLocale(locale.getLocale());
    }

    return Component.empty();
  }
}