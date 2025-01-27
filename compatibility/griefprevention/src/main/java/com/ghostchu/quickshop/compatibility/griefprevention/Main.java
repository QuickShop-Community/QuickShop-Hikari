package com.ghostchu.quickshop.compatibility.griefprevention;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.economy.ShopPurchaseEvent;
import com.ghostchu.quickshop.api.event.modification.ShopAuthorizeCalculateEvent;
import com.ghostchu.quickshop.api.event.modification.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.modification.ShopPreCreateEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.logging.container.ShopRemoveLog;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.events.ClaimCreatedEvent;
import me.ryanhamshire.GriefPrevention.events.ClaimDeletedEvent;
import me.ryanhamshire.GriefPrevention.events.ClaimExpirationEvent;
import me.ryanhamshire.GriefPrevention.events.ClaimResizeEvent;
import me.ryanhamshire.GriefPrevention.events.TrustChangedEvent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Main extends CompatibilityModule implements Listener {

  private GriefPrevention griefPrevention;
  private final List<Flag> tradeLimits = new ArrayList<>(3);
  private boolean whiteList;
  private boolean deleteOnClaimTrustChanged;
  private boolean deleteOnClaimUnclaimed;
  private boolean deleteOnClaimExpired;
  private boolean deleteOnClaimResized;
  private boolean deleteOnSubClaimCreated;
  private Flag createLimit;

  @Override
  public void init() {

    this.whiteList = getConfig().getBoolean("whitelist-mode");
    this.deleteOnClaimTrustChanged = getConfig().getBoolean("delete-on-claim-trust-changed");
    this.deleteOnClaimUnclaimed = getConfig().getBoolean("delete-on-claim-unclaimed");
    this.deleteOnClaimExpired = getConfig().getBoolean("delete-on-claim-expired");
    this.deleteOnClaimResized = getConfig().getBoolean("delete-on-claim-resized");
    this.deleteOnSubClaimCreated = getConfig().getBoolean("delete-on-subclaim-created");
    this.createLimit = Flag.getFlag(getConfig().getString("create"));
    this.tradeLimits.addAll(toFlags(getConfig().getStringList("trade")));
    this.griefPrevention = (GriefPrevention)Bukkit.getPluginManager().getPlugin("GriefPrevention");
    Log.debug("GPCompat: Started up");
    try {
      getLogger().info("Registering unsafe event listener...");
      Bukkit.getPluginManager().registerEvents(new Listener() {

        // Player can resize the main claim or the subclaim.
        // So we need to call either the handleMainClaimResized or the handleSubClaimResized method.
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onClaimResized(final ClaimResizeEvent event) {

          if(!deleteOnClaimResized) {
            return;
          }
          final Claim oldClaim = event.getFrom();
          final Claim newClaim = event.getTo();
          if(oldClaim.parent == null) {
            handleMainClaimResized(oldClaim, newClaim);
          } else {
            handleSubClaimResized(oldClaim, newClaim);
          }
        }
      }, this);
    } catch(final Throwable e) {
      getLogger().info("Unable to register resize event handler, other listeners will sill working.");
    }
  }

  private List<Flag> toFlags(final List<String> flags) {

    final List<Flag> result = new ArrayList<>(3);
    for(final String flagStr : flags) {
      final Flag flag = Flag.getFlag(flagStr);
      if(flag != null) {
        result.add(flag);
      }
    }
    return result;
  }

  // Since only the main claim expires, we will call the handleMainClaimUnclaimedOrExpired method.
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onClaimExpired(final ClaimExpirationEvent event) {

    if(!deleteOnClaimExpired) {
      return;
    }
    handleMainClaimUnclaimedOrExpired(event.getClaim(), "[SHOP DELETE] GP Integration: Single delete (Claim Expired) #");
  }

  // If it is the main claim, then we will delete all the shops that were inside of it.
  private void handleMainClaimUnclaimedOrExpired(final Claim claim, final String logMessage) {

    final List<Shop> shops = getApi().getShopManager().getAllShops();
    for(final Shop shop : shops) {
      if(claim.contains(shop.getLocation(), false, false)) {
        getApi().logEvent(new ShopRemoveLog(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "GriefPrevention", false), String.format("[%s Integration]Shop %s deleted caused by [System] Claim/SubClaim Unclaimed/Expired: " + logMessage, this.getName(), shop), shop.saveToInfoStorage()));
        getApi().getShopManager().deleteShop(shop);
      }
    }
  }


  // If it is a main claim, then we will remove the shops if the main claim was resized (size was decreased).
  // A shop will be removed if the old claim contains it but the new claim doesn't.
  private void handleMainClaimResized(final Claim oldClaim, final Claim newClaim) {

    final List<Shop> shops = getApi().getShopManager().getAllShops();
    for(final Shop shop : shops) {
      if(oldClaim.contains(shop.getLocation(), false, false) &&
         !newClaim.contains(shop.getLocation(), false, false)) {
        getApi().logEvent(new ShopRemoveLog(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "GriefPrevention", false), String.format("[%s Integration]Shop %s deleted caused by [Single] Claim Resized: ", this.getName(), shop), shop.saveToInfoStorage()));
        getApi().getShopManager().deleteShop(shop);
      }
    }
  }

  // If it is a subclaim, then we will remove the shops in 2 situations.
  // We will never remove the shops of the claim owner.
  // We will remove a shop if the shop was inside the subclaim but now it is outside the subclaim.
  // We will remove a shop if the shop was outside the subclaim but now it is inside the subclaim.
  private void handleSubClaimResized(final Claim oldClaim, final Claim newClaim) {

    handleSubClaimResizedHelper(oldClaim, newClaim);
    handleSubClaimResizedHelper(newClaim, oldClaim);
  }

  private void handleSubClaimResizedHelper(final Claim claimVerifyChunks, final Claim claimVerifyShop) {

    final List<Shop> shops = getApi().getShopManager().getAllShops();
    for(final Shop shop : shops) {
      if(!claimVerifyChunks.getOwnerID().equals(shop.getOwner().getUniqueId()) &&
         claimVerifyChunks.contains(shop.getLocation(), false, false) &&
         !claimVerifyShop.contains(shop.getLocation(), false, false)) {
        getApi().logEvent(new ShopRemoveLog(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "GriefPrevention", false), String.format("[%s Integration]Shop %s deleted caused by [Single] SubClaim Resized: ", this.getName(), shop), shop.saveToInfoStorage()));
        getApi().getShopManager().deleteShop(shop);
      }
    }
  }

  // We will check if the shop belongs to user whose permissions were changed.
  // It will remove a shop if the shop owner no longer has permission to build a shop there.
  // We will not delete the shops of the claim owner.
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onClaimTrustChanged(final TrustChangedEvent event) {

    if(!deleteOnClaimTrustChanged) {
      return;
    }
    if(event.isGiven() && event.getClaimPermission() == null) {
      return;
    }
    if(createLimit.toClaimPermission().isGrantedBy(event.getClaimPermission())) {
      return;
    }
    for(final Claim claim : event.getClaims()) {
      handleClaimTrustChanged(Objects.requireNonNullElse(claim.parent, claim), event);
    }
  }

  // Helper to the Claim Trust Changed Event Handler (to avoid duplicate code above)
  private void handleClaimTrustChanged(final Claim claim, final TrustChangedEvent event) {

    if(event.isGiven()) {
      return;
    }
    final List<Shop> shops = getApi().getShopManager().getAllShops();
    for(final Shop shop : shops) {
      if(claim.getOwnerID().equals(shop.getOwner().getUniqueId())) {
        continue;
      }
      if(event.getIdentifier().equals(shop.getOwner().getUniqueIdIfRealPlayer().orElse(CommonUtil.getNilUniqueId()).toString())) {
        getApi().logEvent(new ShopRemoveLog(QUserImpl.createFullFilled(event.getChanger()), String.format("[%s Integration]Shop %s deleted caused by [Single] Claim/SubClaim Trust Changed", this.getName(), shop), shop.saveToInfoStorage()));
        getApi().getShopManager().deleteShop(shop);
      } else if(event.getIdentifier().contains(shop.getOwner().getUniqueIdIfRealPlayer().orElse(CommonUtil.getNilUniqueId()).toString())) {
        getApi().logEvent(new ShopRemoveLog(QUserImpl.createFullFilled(event.getChanger()), String.format("[%s Integration]Shop %s deleted caused by [Group] Claim/SubClaim Trust Changed", this.getName(), shop), shop.saveToInfoStorage()));
        getApi().getShopManager().deleteShop(shop);
      } else if("all".equals(event.getIdentifier()) || "public".equals(event.getIdentifier())) {
        getApi().logEvent(new ShopRemoveLog(QUserImpl.createFullFilled(event.getChanger()), String.format("[%s Integration]Shop %s deleted caused by [All/Public] Claim/SubClaim Trust Changed", this.getName(), shop), shop.saveToInfoStorage()));
        getApi().getShopManager().deleteShop(shop);
      }
    }
  }

  // Player can unclaim the main claim or the subclaim.
  // So we need to call either the handleMainClaimUnclaimedOrExpired or the handleSubClaimUnclaimed method.
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onClaimUnclaimed(final ClaimDeletedEvent event) {

    if(!deleteOnClaimUnclaimed) {
      return;
    }
    if(event.getClaim().parent == null) {
      handleMainClaimUnclaimedOrExpired(event.getClaim(), "[SHOP DELETE] GP Integration: Single delete (Claim Unclaimed) #");
    } else {
      handleSubClaimUnclaimed(event.getClaim());
    }
  }

  // If it is a subclaim, then we will not remove the shops of the main claim owner.
  // But we will remove all the others.
  private void handleSubClaimUnclaimed(final Claim subClaim) {

    final List<Shop> shops = getApi().getShopManager().getAllShops();
    for(final Shop shop : shops) {
      if(!subClaim.getOwnerID().equals(shop.getOwner().getUniqueId()) &&
         subClaim.contains(shop.getLocation(), false, false)) {
        getApi().logEvent(new ShopRemoveLog(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "GriefPrevention", false), String.format("[%s Integration]Shop %s deleted caused by [Single] SubClaim Unclaimed", this.getName(), shop), shop.saveToInfoStorage()));
        getApi().getShopManager().deleteShop(shop);
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onCreation(final ShopCreateEvent event) {

    event.getCreator().getBukkitPlayer().ifPresent(p->{
      if(checkPermission(p, event.getShop().getLocation(), Collections.singletonList(createLimit))) {
        return;
      }
      event.setCancelled(true, getApi().getTextManager().of(event.getCreator(), "addon.griefprevention.creation-denied").forLocale());
    });
  }

  private boolean checkPermission(@NotNull final Player player, @NotNull final Location location, final List<Flag> limits) {
    if (player.hasPermission("griefprevention.ignoreclaims")) {
      return true;
    }

    if(!griefPrevention.claimsEnabledForWorld(location.getWorld())) {
      return true;
    }
    final Claim claim = griefPrevention.dataStore.getClaimAt(location, false, false, griefPrevention.dataStore.getPlayerData(player.getUniqueId()).lastClaim);
    if(claim == null) {
      return !whiteList;
    }
    for(final Flag flag : limits) {
      if(!flag.check(claim, player)) {
        return false;
      }
    }
    return true;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPreCreation(final ShopPreCreateEvent event) {

    event.getCreator().getBukkitPlayer().ifPresent(p->{
      if(checkPermission(p, event.getLocation(), Collections.singletonList(createLimit))) {
        return;
      }
      event.setCancelled(true, getApi().getTextManager().of(event.getCreator(), "addon.griefprevention.creation-denied").forLocale());
    });

  }

  // Player can create subclaims inside a claim.
  // So if a subclaim is created that will contain, initially, shops from others players, then we will remove them.
  // Because they won't have, initially, permission to create a shop in that subclaim.
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onSubClaimCreated(final ClaimCreatedEvent event) {

    if(!deleteOnSubClaimCreated) {
      return;
    }
    if(event.getClaim().parent == null) {
      return;
    }

    final List<Shop> shops = getApi().getShopManager().getAllShops();
    for(final Shop shop : shops) {
      if(!event.getClaim().getOwnerID().equals(shop.getOwner().getUniqueId()) &&
         event.getClaim().contains(shop.getLocation(), false, false)) {
        getApi().logEvent(new ShopRemoveLog(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "GriefPrevention", false), String.format("[%s Integration]Shop %s deleted caused by [Single] SubClaim Created", this.getName(), shop), shop.saveToInfoStorage()));
        getApi().getShopManager().deleteShop(shop);
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onTrading(final ShopPurchaseEvent event) {

    event.getPurchaser().getBukkitPlayer().ifPresent(p->{

      if(tradeLimits.isEmpty()) {
        return;
      }

      if(checkPermission(p, event.getShop().getLocation(), tradeLimits)) {
        return;
      }
      event.setCancelled(true, getApi().getTextManager().of(event.getPurchaser(), "addon.griefprevention.trade-denied").forLocale());
    });
  }

  @EventHandler(ignoreCancelled = true)
  public void permissionOverride(final ShopAuthorizeCalculateEvent event) {

    Log.debug("GP-Compat: Starting override permission...");
    final Location shopLoc = event.getShop().getLocation();
    if(!griefPrevention.claimsEnabledForWorld(shopLoc.getWorld())) {
      final String worldName = shopLoc.getWorld() == null ? "Null World" : shopLoc.getWorld().getName();
      Log.debug("GP-Compat: World " + worldName + " not enabled for claims");
      return;
    }
    final Claim claim = griefPrevention.dataStore.getClaimAt(shopLoc, false, false, null);
    if(claim == null) {
      Log.debug("GP-Compat: Shop " + shopLoc + " position had no claim(s) exists.");
      return;
    }
    if(Objects.equals(event.getAuthorizer(), claim.getOwnerID())) {
      if(event.getNamespace().equals(QuickShop.getInstance().getJavaPlugin()) && event.getPermission().equals(BuiltInShopPermission.DELETE.getRawNode())) {
        event.setResult(true);
        Log.debug("GP-Compat: Shop " + shopLoc + "'s override request was approved.");
      }
    } else {
      Log.debug("GP-Compat: Shop " + shopLoc + "'s requested authorizer " + event.getAuthorizer() + " are not match with claim owner " + claim.getOwnerID());
    }
  }


  enum Flag {

    BUILD {
      @Override
      boolean check(final Claim claim, final Player player) {

        return claim.allowBuild(player, Material.CHEST) == null;
      }

      @Override
      ClaimPermission toClaimPermission() {

        return ClaimPermission.Build;
      }
    }, INVENTORY {
      @Override
      boolean check(final Claim claim, final Player player) {

        return claim.allowContainers(player) == null;
      }

      @Override
      ClaimPermission toClaimPermission() {

        return ClaimPermission.Inventory;
      }
    }, ACCESS {
      @Override
      boolean check(final Claim claim, final Player player) {

        return claim.allowAccess(player) == null;
      }

      @Override
      ClaimPermission toClaimPermission() {

        return ClaimPermission.Access;
      }
    };

    public static Flag getFlag(final String flag) {

      for(final Flag value : Flag.values()) {
        if(value.name().equalsIgnoreCase(flag)) {
          return value;
        }
      }
      return null;
    }

    abstract boolean check(Claim claim, Player player);

    abstract ClaimPermission toClaimPermission();
  }

}
