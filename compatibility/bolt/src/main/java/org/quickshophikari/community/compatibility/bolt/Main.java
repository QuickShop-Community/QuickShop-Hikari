package org.quickshophikari.community.compatibility.bolt;

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

import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.management.ShopCreateEvent;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.popcraft.bolt.BoltAPI;

import java.util.Optional;

/**
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public final class Main extends CompatibilityModule implements Listener {

  @Override
  public void init() {
    // There no init stuffs need to do
  }

  @EventHandler(ignoreCancelled = true)
  public void onPreCreation(final ShopCreateEvent event) {

    if(event.isPhase(Phase.PRE_CANCELLABLE) && event.location().getWorld() != null) {

      final Optional<Player> player = event.user().getBukkitPlayer();
      if (player.isEmpty()) {
        return;
      }

      final Location loc = event.location();
      BoltAPI bolt = Bukkit.getServer().getServicesManager().load(BoltAPI.class);
      if (bolt == null) {
        return;
      }

      if(bolt.isProtected(loc.getBlock()) && !bolt.canAccess(loc.getBlock(), player.get())) {
        event.setCancelled(true, "You can't create a shop on a protected block");
      }
    }
  }
}
