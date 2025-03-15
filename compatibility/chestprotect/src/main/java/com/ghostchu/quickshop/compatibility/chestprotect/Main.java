package com.ghostchu.quickshop.compatibility.chestprotect;

import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.management.ShopCreateEvent;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import me.angeschossen.chestprotect.api.ChestProtectAPI;
import me.angeschossen.chestprotect.api.protection.block.BlockProtection;
import me.angeschossen.chestprotect.api.protection.world.ProtectionWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Optional;

public final class Main extends CompatibilityModule implements Listener {

  @Override
  public void init() {
    // There no init stuffs need to do
  }

  @EventHandler(ignoreCancelled = true)
  public void onPreCreation(final ShopCreateEvent event) {

    if(event.isPhase(Phase.PRE_CANCELLABLE) && event.location().getWorld() != null) {

      final Location loc = event.location();
      final ProtectionWorld world = ChestProtectAPI.getInstance().getProtectionWorld(loc.getWorld());
      if(world != null) {
        final BlockProtection protection = world.getBlockProtection(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        final QUser qUser = event.user();
        final Optional<Player> playerOptional = qUser.getBukkitPlayer();
        if(playerOptional.isPresent()) {
          if(protection != null) {
            final Player player = playerOptional.get();
            if(!player.getUniqueId().equals(protection.getOwner())) {
              event.setCancelled(true, "You can't create a shop on a exists ChestProtect protection");
            }
          }
        }
      }
    }
  }
}
