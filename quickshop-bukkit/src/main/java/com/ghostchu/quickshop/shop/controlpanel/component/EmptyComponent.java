package com.ghostchu.quickshop.shop.controlpanel.component;
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

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.shop.ControlComponent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.MsgUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * EmptyComponent
 *
 * @author creatorfromhell
 * @since 6.2.0.9
 */
public class EmptyComponent implements ControlComponent {

  /**
   * Retrieves an identifier.
   *
   * @return A string representing the identifier.
   */
  @Override
  public String identifier() {

    return "empty";
  }

  /**
   * Checks if the provided Player can interact with the given Shop.
   *
   * @param sender The Player object requesting interaction.
   * @param shop   The Shop object to check interaction with.
   *
   * @return True if the Player can interact with the Shop, false otherwise.
   */
  @Override
  public boolean applies(final @NotNull QuickShopAPI plugin, final @NotNull Player sender, final @NotNull Shop shop) {

    return !shop.isUnlimited() && ((QuickShop)plugin).perm().hasPermission(sender, "quickshop.empty");
  }

  /**
   * Generates a Component based on the player and shop provided.
   *
   * @param sender The Player object representing who triggered the generation.
   * @param shop   The Shop object representing the shop for which components are being generated.
   *
   * @return A Component object based on the provided player and shop.
   */
  @Override
  public Component generate(final @NotNull QuickShopAPI plugin, final @NotNull Player sender, final @NotNull Shop shop) {

    final Component text = ((QuickShop)plugin).text().of(sender, "controlpanel.empty", shop.getPrice()).forLocale();
    final Component hoverText = ((QuickShop)plugin).text().of(sender, "controlpanel.empty-hover").forLocale();
    final String clickCommand = MsgUtil.fillArgs("/{0} {1} {2}", ((QuickShop)plugin).getMainCommand(), ((QuickShop)plugin).getCommandPrefix("silentempty"), shop.getRuntimeRandomUniqueId().toString());

    return text.hoverEvent(HoverEvent.showText(hoverText))
            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, clickCommand));
  }
}