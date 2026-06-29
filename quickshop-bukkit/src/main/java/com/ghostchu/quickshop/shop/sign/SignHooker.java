package com.ghostchu.quickshop.shop.sign;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.listener.AbstractQSListener;
import com.ghostchu.quickshop.util.logger.Log;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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

    if(!shop.isLoaded()) {
      return;
    }
    Log.debug("Updating per-player packet sign: Player=" + player.getName() + ", Location=" + location + ", Shop=" + shop.getShopId());


    final Location loc = shop.bukkitLocation().clone();
    final CompletableFuture<List<Component>> textCompletable = shop.getSignTextAsync(plugin.getTextManager().findRelativeLanguages(player));

    textCompletable.thenAccept(lines ->QuickShop.folia().getScheduler().runAtLocation(loc, (consumer)->{

      if(!shop.isValid()) {

        return;
      }
      for(final Sign sign : shop.getSigns()) {

        plugin.platform().sendSignTextChange(player, sign, sign.getSide(Side.FRONT).isGlowingText(), sign.getSide(Side.FRONT).getColor(), lines);
      }
    }));
  }

  public void updatePerPlayerShopSignBroadcast(final Location location, final Shop shop) {

    final World world = shop.bukkitLocation().getWorld();
    if(world == null) {
      return;
    }
    QuickShop.folia().getScheduler().runAtLocationLater(shop.bukkitLocation(), ()->{
      final Collection<Player> nearbyPlayers = getPlayersTrackingChunk(world, shop.bukkitLocation());
      for(final Player nearbyPlayer : nearbyPlayers) {
        updatePerPlayerShopSign(nearbyPlayer, location, shop);
      }
    }, 1);
  }

  private static final boolean CAN_USE_PLAYERS_SEEING_CHUNK;

  // getPlayersSeeingChunk was added in 1.20.6, so check for the existence of the method and fallback to a worse method on earlier versions.
  static {

    boolean exists = false;
    try {
      //noinspection ConstantValue
      exists = World.class.getMethod("getPlayersSeeingChunk", int.class, int.class) != null;
    } catch (final ReflectiveOperationException ignored) {}

    CAN_USE_PLAYERS_SEEING_CHUNK = exists;
  }

  private Collection<Player> getPlayersTrackingChunk(final World world, final Location locationForChunk) {

    if (CAN_USE_PLAYERS_SEEING_CHUNK) {
      return world.getPlayersSeeingChunk(locationForChunk.getBlockX() >> 4, locationForChunk.getBlockZ() >> 4);
    } else {
      final int viewDistanceBlocks = plugin.getJavaPlugin().getServer().getViewDistance() * 16;
      return world.getNearbyEntitiesByType(Player.class, locationForChunk, viewDistanceBlocks, world.getMaxHeight(), viewDistanceBlocks);
    }
  }
}
