package com.ghostchu.quickshop.compatibility.ultimateclaims;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.management.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.management.ShopPermissionCheckEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopChunk;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.Util;
import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.api.events.ClaimChunkUnclaimEvent;
import com.songoda.ultimateclaims.api.events.ClaimDeleteEvent;
import com.songoda.ultimateclaims.api.events.ClaimMemberLeaveEvent;
import com.songoda.ultimateclaims.api.events.ClaimPlayerBanEvent;
import com.songoda.ultimateclaims.api.events.ClaimPlayerKickEvent;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.claim.ClaimManager;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public final class Main extends CompatibilityModule {

  private boolean whitelist;
  private ClaimManager claimManager;
  private boolean deleteWhenLosePermission;
  private boolean deleteWhenClaimDeleted;
  private boolean deleteWhenChunkUnclaimed;

  @Override
  public void init() {

    claimManager = getPlugin(UltimateClaims.class).getClaimManager();
    whitelist = getConfig().getBoolean("whitelist-mode");
    deleteWhenLosePermission = getConfig().getBoolean("delete-on-lose-permission");
    deleteWhenClaimDeleted = getConfig().getBoolean("delete-shops-in-claim-when-deleted");
    deleteWhenChunkUnclaimed = getConfig().getBoolean("delete-shops-in-chunk-when-unclaimed");
  }

  @EventHandler(ignoreCancelled = true)
  public void onPreCreation(final ShopCreateEvent event) {

    final Location loc = event.location();
    if(!event.phase().cancellable() || loc.getWorld() == null) {

      return;
    }

    final Chunk locChunk = loc.getChunk();
    final Claim claim = claimManager.getClaim(locChunk);
    if(claim != null && event.phase().cancellable()) {

      if(claim.getPowerCell().hasLocation() && claim.getPowerCell().getLocation().equals(loc)) {
        event.setCancelled(true, "powercell");
        return;
      }
    }

    final UUID id = event.user().getUniqueId();
    if(claim != null && id != null) {

      if(claim.getMember(id) != null) {
        return;
      }

      event.setCancelled(true, getApi().getTextManager().of(event.user(), "addon.ultimateclaims.creation-denied").forLocale());
    } else {
      if(whitelist) {
        event.setCancelled(true, getApi().getTextManager().of(event.user(), "addon.ultimateclaims.creation-denied").forLocale());
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onMemberLeave(final ClaimMemberLeaveEvent event) {

    if(!deleteWhenLosePermission) {
      return;
    }
    deleteShopInClaim(event.getClaim(), event.getPlayer().getUniqueId());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onMemberKick(final ClaimPlayerKickEvent event) {

    if(!deleteWhenLosePermission) {
      return;
    }
    deleteShopInClaim(event.getClaim(), event.getPlayer().getUniqueId());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onMemberBan(final ClaimPlayerBanEvent event) {

    if(!deleteWhenLosePermission) {
      return;
    }
    deleteShopInClaim(event.getClaim(), event.getBannedPlayer().getUniqueId());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onClaimUnclaim(final ClaimChunkUnclaimEvent event) {

    if(!deleteWhenChunkUnclaimed) {
      return;
    }
    deleteShopInClaim(event.getClaim(), event.getChunk());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onClaimDelete(final ClaimDeleteEvent event) {

    if(!deleteWhenClaimDeleted) {
      return;
    }
    deleteShopInClaim(event.getClaim());
  }

  private void deleteShopInClaim(final Claim claim, final Chunk chunk) {

    for(final Shop shop : Collections.unmodifiableMap(getApi().getShopManager().getShops(chunk)).values()) {

      recordDeletion(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "UltimateClaims", false), shop, "UltimateClaims: shop deleted because claim/claimchunk deleted");
      Util.mainThreadRun(()->getApi().getShopManager().deleteShop(shop));
    }
  }

  private void deleteShopInClaim(final Claim claim) {
    //Getting all shop with world-chunk-shop mapping
    for(final Map.Entry<String, Map<ShopChunk, Map<Location, Shop>>> entry : getApi().getShopManager().getShops().entrySet()) {
      //Matching world
      final World world = getServer().getWorld(entry.getKey());
      if(world != null) {
        //Matching chunk
        for(final Map.Entry<ShopChunk, Map<Location, Shop>> chunkedShopEntry : entry.getValue().entrySet()) {

          final ShopChunk shopChunk = chunkedShopEntry.getKey();
          if(claim.containsChunk(convert(world, shopChunk))) {
            //Matching Owner and delete it
            final Map<Location, Shop> shops = chunkedShopEntry.getValue();
            for(final Shop shop : shops.values()) {
              recordDeletion(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "UltimateClaims", false), shop, "UltimateClaims: shop deleted because claim/claimchunk deleted");
              Util.mainThreadRun(()->getApi().getShopManager().deleteShop(shop));
            }
          }
        }
      }
    }
  }

  private void deleteShopInClaim(final Claim claim, final UUID target) {
    //Getting all shop with world-chunk-shop mapping
    for(final Map.Entry<String, Map<ShopChunk, Map<Location, Shop>>> entry : getApi().getShopManager().getShops().entrySet()) {
      //Matching world
      final World world = getServer().getWorld(entry.getKey());
      if(world != null) {
        //Matching chunk
        for(final Map.Entry<ShopChunk, Map<Location, Shop>> chunkedShopEntry : entry.getValue().entrySet()) {

          final ShopChunk shopChunk = chunkedShopEntry.getKey();

          if(claim.containsChunk(convert(world, shopChunk))) {
            //Matching Owner and delete it
            final Map<Location, Shop> shops = chunkedShopEntry.getValue();
            for(final Shop shop : shops.values()) {
              final UUID owner = shop.getOwner().getUniqueIdIfRealPlayer().orElse(null);
              if(owner == null) {
                continue;
              }
              if(target.equals(owner)) {
                recordDeletion(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "UltimateClaims", false), shop, "UltimateClaims: shop deleted because owner lost permission");
                Util.mainThreadRun(()->getApi().getShopManager().deleteShop(shop));
              }
            }
          }
        }
      }
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void permissionOverride(final ShopPermissionCheckEvent event) {

    if(event.shop().isEmpty()) {
      return;
    }

    final Location shopLoc = event.shop().get().bukkitLocation();
    if(shopLoc.getWorld() == null) {
      return;
    }

    final Chunk locChunk = shopLoc.getChunk();
    final Claim claim = claimManager.getClaim(locChunk);
    if(claim == null) {
      return;
    }

    if(claim.getOwner().getUniqueId().equals(event.playerUUID())) {
      if(event.pluginNamespace().equals(QuickShop.getInstance().getJavaPlugin().getName()) && event.permissionNode().equals(BuiltInShopPermission.DELETE.getRawNode())) {
        event.hasPermission(true);
      }
    }
  }

  private Chunk convert(final World world, final ShopChunk chunk) {

    return world.getChunkAt(chunk.getX(), chunk.getZ());
  }
}