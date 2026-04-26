package com.ghostchu.quickshop.shop.sign;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.listener.AbstractQSListener;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SignHooker extends AbstractQSListener {

  public SignHooker(final QuickShop plugin) {
    super(plugin);
  }

  @EventHandler
  public void onPlayerChunkLoadEvent(final PlayerChunkLoadEvent event) {
    final Player player = event.getPlayer();
    final Chunk chunk = event.getChunk();

    final Map<Location, Shop> shops = plugin.getShopManager().getShops(player.getWorld().getName(), chunk.getX(), chunk.getZ());
    if (shops != null) {
      shops.forEach((loc, shop)->updatePerPlayerShopSign(player, loc, shop));
    }
  }

  public void updatePerPlayerShopSign(final Player player, final Location location, final Shop shop) {

    Util.ensureThread(false);
    if(!shop.isLoaded()) {
      return;
    }
    Log.debug("Updating per-player packet sign: Player=" + player.getName() + ", Location=" + location + ", Shop=" + shop.getShopId());
    final List<Component> lines = shop.getSignText(plugin.getTextManager().findRelativeLanguages(player));
    for(final Sign sign : shop.getSigns()) {

      plugin.platform().sendSignTextChange(player, sign, plugin.getConfig().getBoolean("shop.sign-glowing"), lines);
    }
  }

  public void updatePerPlayerShopSignBroadcast(final Location location, final Shop shop) {

    final World world = shop.getLocation().getWorld();
    if(world == null) {
      return;
    }
    QuickShop.folia().getScheduler().runAtLocationLater(shop.getLocation(), ()->{
      final Collection<Player> nearbyPlayers = world.getPlayersSeeingChunk(shop.getLocation().getBlockX() >> 4, shop.getLocation().getBlockZ() >> 4);
      for(final Player nearbyPlayer : nearbyPlayers) {
        updatePerPlayerShopSign(nearbyPlayer, location, shop);
      }
    }, 1);
  }
}
