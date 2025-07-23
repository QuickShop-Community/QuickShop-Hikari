package com.ghostchu.quickshop.compatibility.husktowns;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.economy.ShopPurchaseEvent;
import com.ghostchu.quickshop.api.event.management.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.management.ShopPermissionCheckEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopChunk;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.Util;
import net.william278.husktowns.api.HuskTownsAPI;
import net.william278.husktowns.claim.ClaimWorld;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.events.MemberLeaveEvent;
import net.william278.husktowns.events.TownDisbandEvent;
import net.william278.husktowns.events.UnClaimAllEvent;
import net.william278.husktowns.events.UnClaimEvent;
import net.william278.husktowns.town.Town;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class Main extends CompatibilityModule {

  private boolean ignoreDisabledWorlds;
  private boolean whitelist;
  private HuskTownsAPI huskAPI;
  private boolean deleteWhenLosePermission;
  private boolean deleteWhenLandDeleted;

  @Override
  public void init() {

    huskAPI = HuskTownsAPI.getInstance();
    ignoreDisabledWorlds = getConfig().getBoolean("ignore-disabled-worlds");
    whitelist = getConfig().getBoolean("whitelist-mode");
    deleteWhenLosePermission = getConfig().getBoolean("delete-on-lose-permission");
    deleteWhenLandDeleted = getConfig().getBoolean("delete-shops-in-town-when-deleted");
  }

  @EventHandler(ignoreCancelled = true)
  public void onPreCreation(final ShopCreateEvent event) {

    final Location loc = event.location();
    if(!event.phase().cancellable() || loc.getWorld() == null) {

      return;
    }

    final net.william278.husktowns.claim.World huskWorld = huskAPI.getWorld(loc.getWorld().getName());
    final Optional<ClaimWorld> claimWorld = huskAPI.getClaimWorld(huskWorld);

    if(claimWorld.isEmpty()) {
      if(!ignoreDisabledWorlds) {
        event.setCancelled(true, getApi().getTextManager().of(event.user(), "addon.husktowns.world-not-enabled").forLocale());
        return;
      }
    }

    final Chunk locChunk = loc.getChunk();
    final Optional<TownClaim> claim = huskAPI.getClaimAt(convert(locChunk), huskWorld);
    final UUID id = event.user().getUniqueId();
    if(claim.isPresent() && id != null) {

      final Town town = claim.get().town();

      if(town.getMayor().equals(id) || isMember(id, town)) {
        return;
      }
      event.setCancelled(true, getApi().getTextManager().of(event.user(), "addon.husktowns.creation-denied").forLocale());
    } else {
      if(whitelist) {
        event.setCancelled(true, getApi().getTextManager().of(event.user(), "addon.husktowns.creation-denied").forLocale());
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onMemberLeave(final MemberLeaveEvent event) {

    if(!deleteWhenLosePermission) {
      return;
    }
    deleteShopInTown(event.getTown(), event.getUser().getUuid());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onTownDisband(final TownDisbandEvent event) {

    if(!deleteWhenLandDeleted) {
      return;
    }
    deleteShopInTown(event.getTown());
  }

  private void deleteShopInTown(final Town town) {
    //Getting all shop with world-chunk-shop mapping
    for(final Map.Entry<String, Map<ShopChunk, Map<Location, Shop>>> entry : getApi().getShopManager().getShops().entrySet()) {
      //Matching world
      final World world = getServer().getWorld(entry.getKey());
      if(world != null) {
        //Matching chunk
        for(final Map.Entry<ShopChunk, Map<Location, Shop>> chunkedShopEntry : entry.getValue().entrySet()) {

          final ShopChunk shopChunk = chunkedShopEntry.getKey();
          final net.william278.husktowns.claim.World huskWorld = huskAPI.getWorld(world.getName());
          final Optional<TownClaim> claim = huskAPI.getClaimAt(convert(shopChunk), huskWorld);

          if(claim.isPresent() && claim.get().town().getId() == town.getId()) {
            //Matching Owner and delete it
            final Map<Location, Shop> shops = chunkedShopEntry.getValue();
            for(final Shop shop : shops.values()) {
              recordDeletion(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "HuskTowns", false), shop, "HuskTowns: shop deleted because town/claim deleted");
              Util.mainThreadRun(()->getApi().getShopManager().deleteShop(shop));
            }
          }
        }
      }
    }
  }

  private void deleteShopInTown(final Town town, final UUID target) {
    //Getting all shop with world-chunk-shop mapping
    for(final Map.Entry<String, Map<ShopChunk, Map<Location, Shop>>> entry : getApi().getShopManager().getShops().entrySet()) {
      //Matching world
      final World world = getServer().getWorld(entry.getKey());
      if(world != null) {
        //Matching chunk
        for(final Map.Entry<ShopChunk, Map<Location, Shop>> chunkedShopEntry : entry.getValue().entrySet()) {

          final ShopChunk shopChunk = chunkedShopEntry.getKey();
          final net.william278.husktowns.claim.World huskWorld = huskAPI.getWorld(world.getName());
          final Optional<TownClaim> claim = huskAPI.getClaimAt(convert(shopChunk), huskWorld);

          if(claim.isPresent() && claim.get().town().getId() == town.getId()) {
            //Matching Owner and delete it
            final Map<Location, Shop> shops = chunkedShopEntry.getValue();
            for(final Shop shop : shops.values()) {
              final UUID owner = shop.getOwner().getUniqueIdIfRealPlayer().orElse(null);
              if(owner == null) {
                continue;
              }
              if(target.equals(owner)) {
                recordDeletion(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "HuskTowns", false), shop, "HuskTowns: shop deleted because owner lost permission");
                Util.mainThreadRun(()->getApi().getShopManager().deleteShop(shop));
              }
            }
          }
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onUnClaim(final UnClaimEvent event) {

    deleteShopInTown(event.getTown());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onUnClaimAll(final UnClaimAllEvent event) {

    deleteShopInTown(event.getTown());
  }

  @EventHandler(ignoreCancelled = true)
  public void onTrading(final ShopPurchaseEvent event) {

    if(event.getShop().getLocation().getWorld() == null
       || huskAPI.getClaimWorld(huskAPI.getWorld(event.getShop().getLocation().getWorld().getName())).isEmpty()) {

      if(ignoreDisabledWorlds) {
        return;
      }
      event.setCancelled(true, getApi().getTextManager().of(event.getPurchaser(), "addon.husktowns.world-not-enabled").forLocale());
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void permissionOverride(final ShopPermissionCheckEvent event) {

    if(event.shop().isEmpty()) {
      return;
    }

    final Location shopLoc = event.shop().get().getLocation();
    if(shopLoc.getWorld() == null) {
      return;
    }

    final Chunk locChunk = shopLoc.getChunk();
    final net.william278.husktowns.claim.World huskWorld = huskAPI.getWorld(shopLoc.getWorld().getName());
    final Optional<TownClaim> claim = huskAPI.getClaimAt(convert(locChunk), huskWorld);
    if(claim.isEmpty()) {
      return;
    }

    final Town town = claim.get().town();

    if(town.getMayor().equals(event.playerUUID())) {
      if(event.pluginNamespace().equals(QuickShop.getInstance().getJavaPlugin().getName()) && event.permissionNode().equals(BuiltInShopPermission.DELETE.getRawNode())) {
        event.hasPermission(true);
      }
    }
  }

  private boolean isMember(final UUID uuid, final Town town) {

    for(final UUID member : town.getMembers().keySet()) {

      return member.equals(uuid);
    }
    return false;
  }

  private net.william278.husktowns.claim.Chunk convert(final Chunk chunk) {

    return net.william278.husktowns.claim.Chunk.at(chunk.getX(), chunk.getZ());
  }

  private net.william278.husktowns.claim.Chunk convert(final ShopChunk chunk) {

    return net.william278.husktowns.claim.Chunk.at(chunk.getX(), chunk.getZ());
  }
}
