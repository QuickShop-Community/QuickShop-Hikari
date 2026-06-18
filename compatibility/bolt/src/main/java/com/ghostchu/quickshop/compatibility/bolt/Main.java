package com.ghostchu.quickshop.compatibility.bolt;

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
