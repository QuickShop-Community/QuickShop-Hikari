package com.ghostchu.quickshop.compatibility.lands;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.economy.ShopPurchaseEvent;
import com.ghostchu.quickshop.api.event.modification.ShopAuthorizeCalculateEvent;
import com.ghostchu.quickshop.api.event.modification.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.modification.ShopPreCreateEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopChunk;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.Util;
import me.angeschossen.lands.api.events.LandDeleteEvent;
import me.angeschossen.lands.api.events.LandUntrustPlayerEvent;
import me.angeschossen.lands.api.events.PlayerLeaveLandEvent;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.Land;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Map;
import java.util.UUID;

public final class Main extends CompatibilityModule {

  private boolean ignoreDisabledWorlds;
  private boolean whitelist;
  private LandsIntegration landsIntegration;
  private boolean deleteWhenLosePermission;
  private boolean deleteWhenLandDeleted;

  @Override
  public void init() {

    landsIntegration = new me.angeschossen.lands.api.integration.LandsIntegration(this);
    ignoreDisabledWorlds = getConfig().getBoolean("ignore-disabled-worlds");
    whitelist = getConfig().getBoolean("whitelist-mode");
    deleteWhenLosePermission = getConfig().getBoolean("delete-on-lose-permission");
    deleteWhenLandDeleted = getConfig().getBoolean("delete-shops-in-land-when-land-deleted");
  }

  @EventHandler(ignoreCancelled = true)
  public void onCreation(final ShopCreateEvent event) {

    if(landsIntegration.getLandWorld(event.getShop().getLocation().getWorld()) == null) {
      if(!ignoreDisabledWorlds) {
        event.setCancelled(true, getApi().getTextManager().of(event.getCreator(), "addon.lands.world-not-enabled").forLocale());
        return;
      }
    }
    final UUID playerUUID = event.getCreator().getUniqueIdIfRealPlayer().orElse(null);
    if(playerUUID == null) {
      return;
    }
    final Location loc = event.getShop().getLocation();
    final Chunk locChunk = loc.getChunk();
    final Land land = landsIntegration.getLand(loc.getWorld(), locChunk.getX(), locChunk.getZ());
    if(land != null) {
      if(land.getOwnerUID().equals(playerUUID) || land.isTrusted(playerUUID)) {
        return;
      }
      event.setCancelled(true, getApi().getTextManager().of(event.getCreator(), "addon.lands.creation-denied").forLocale());
    } else {
      if(whitelist) {
        event.setCancelled(true, getApi().getTextManager().of(event.getCreator(), "addon.lands.creation-denied").forLocale());
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onLandsMember(final PlayerLeaveLandEvent event) {

    if(!deleteWhenLosePermission) {
      return;
    }
    deleteShopInLand(event.getLand(), event.getLandPlayer().getUID());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onLandsDeleted(final LandDeleteEvent event) {

    if(!deleteWhenLandDeleted) {
      return;
    }
    deleteShopInLand(event.getLand(), event.getLandPlayer().getUID());
  }

  private void deleteShopInLand(final Land land, final UUID target) {
    //Getting all shop with world-chunk-shop mapping
    for(final Map.Entry<String, Map<ShopChunk, Map<Location, Shop>>> entry : getApi().getShopManager().getShops().entrySet()) {
      //Matching world
      final World world = getServer().getWorld(entry.getKey());
      if(world != null) {
        //Matching chunk
        for(final Map.Entry<ShopChunk, Map<Location, Shop>> chunkedShopEntry : entry.getValue().entrySet()) {
          final ShopChunk shopChunk = chunkedShopEntry.getKey();
          if(land.hasChunk(world, shopChunk.getX(), shopChunk.getZ())) {
            //Matching Owner and delete it
            final Map<Location, Shop> shops = chunkedShopEntry.getValue();
            for(final Shop shop : shops.values()) {
              final UUID owner = shop.getOwner().getUniqueIdIfRealPlayer().orElse(null);
              if(owner == null) {
                continue;
              }
              if(target.equals(owner)) {
                recordDeletion(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "Lands", false), shop, "Lands: shop deleted because owner lost permission");
                Util.mainThreadRun(()->getApi().getShopManager().deleteShop(shop));
              }
            }
          }
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onLandsPermissionChanges(final LandUntrustPlayerEvent event) {

    if(!deleteWhenLosePermission) {
      return;
    }
    deleteShopInLand(event.getLand(), event.getTargetUID());
  }

  @EventHandler(ignoreCancelled = true)
  public void onPreCreation(final ShopPreCreateEvent event) {

    if(landsIntegration.getLandWorld(event.getLocation().getWorld()) == null) {
      if(!ignoreDisabledWorlds) {
        event.setCancelled(true, getApi().getTextManager().of(event.getCreator(), "addon.lands.world-not-enabled").forLocale());
        return;
      }
    }
    final Location loc = event.getLocation();
    final Chunk locChunk = loc.getChunk();
    final Land land = landsIntegration.getLand(loc.getWorld(), locChunk.getX(), locChunk.getZ());
    if(land != null) {
      if(land.getOwnerUID().equals(event.getCreator().getUniqueId()) || land.isTrusted(event.getCreator().getUniqueId())) {
        return;
      }
      event.setCancelled(true, getApi().getTextManager().of(event.getCreator(), "addon.lands.creation-denied").forLocale());
    } else {
      if(whitelist) {
        event.setCancelled(true, getApi().getTextManager().of(event.getCreator(), "addon.lands.creation-denied").forLocale());
      }
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onTrading(final ShopPurchaseEvent event) {

    if(landsIntegration.getLandWorld(event.getShop().getLocation().getWorld()) == null) {
      if(ignoreDisabledWorlds) {
        return;
      }
      event.setCancelled(true, getApi().getTextManager().of(event.getPurchaser(), "addon.lands.world-not-enabled").forLocale());
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void permissionOverride(final ShopAuthorizeCalculateEvent event) {

    final Location shopLoc = event.getShop().getLocation();
    final Chunk locChunk = shopLoc.getChunk();
    final Land land = landsIntegration.getLand(shopLoc.getWorld(), locChunk.getX(), locChunk.getZ());
    if(land == null) {
      return;
    }
    if(land.getOwnerUID().equals(event.getAuthorizer())) {
      if(event.getNamespace().equals(QuickShop.getInstance().getJavaPlugin()) && event.getPermission().equals(BuiltInShopPermission.DELETE.getRawNode())) {
        event.setResult(true);
      }
    }
  }

}
