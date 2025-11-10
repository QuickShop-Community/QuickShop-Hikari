package com.ghostchu.quickshop.shop.interaction.interactions;
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

import com.ghostchu.quickshop.api.shop.interaction.InteractionClick;
import com.ghostchu.quickshop.api.shop.interaction.InteractionType;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

/**
 * SneakingRightClickShop
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class SneakingRightClickContainer implements InteractionType {

  /**
   * Retrieves the identifier associated with this type of interaction.
   *
   * @return The identifier for this type of interaction.
   */
  @Override
  public String identifier() {

    return "SNEAKING_RIGHT_CLICK_CONTAINER";
  }

  /**
   * Checks if this type of interaction applies to the given PlayerInteractEvent.
   *
   * @param event The PlayerInteractEvent to check against.
   *
   * @return True if this interaction type applies to the event, false otherwise.
   */
  @Override
  public boolean applies(final @NotNull PlayerInteractEvent event, final @NotNull InteractionClick click) {

    return click == InteractionClick.CONTAINER && event.getAction() == Action.RIGHT_CLICK_BLOCK
           && event.getPlayer().isSneaking();
  }
}