package com.ghostchu.quickshop.compatibility.chestprotect;

import com.ghostchu.quickshop.api.event.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.ShopPreCreateEvent;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import me.angeschossen.chestprotect.api.ChestProtectAPI;
import me.angeschossen.chestprotect.api.protection.block.BlockProtection;
import me.angeschossen.chestprotect.api.protection.world.ProtectionWorld;
import net.kyori.adventure.text.Component;
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
    public void onPreCreation(ShopPreCreateEvent event) {
        Location loc = event.getLocation();
        ProtectionWorld world = ChestProtectAPI.getInstance().getProtectionWorld(loc.getWorld());
        if (world != null) {
            BlockProtection protection = world.getBlockProtection(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            QUser qUser = event.getCreator();
            Optional<Player> playerOptional = qUser.getBukkitPlayer();
            if (playerOptional.isPresent()) {
                if (protection != null) {
                    Player player = playerOptional.get();
                    if (!player.getUniqueId().equals(protection.getOwner())) {
                        event.setCancelled(true, (Component) null);
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPreCreation(ShopCreateEvent event) {
        Location loc = event.getShop().getLocation();
        ProtectionWorld world = ChestProtectAPI.getInstance().getProtectionWorld(loc.getWorld());
        if (world != null) {
            BlockProtection protection = world.getBlockProtection(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            QUser qUser = event.getCreator();
            Optional<Player> playerOptional = qUser.getBukkitPlayer();
            if (playerOptional.isPresent()) {
                if (protection != null) {
                    Player player = playerOptional.get();
                    if (!player.getUniqueId().equals(protection.getOwner())) {
                        event.setCancelled(true, "You can't create a shop on a exists ChestProtect protection");
                    }
                }
            }
        }
    }
}
