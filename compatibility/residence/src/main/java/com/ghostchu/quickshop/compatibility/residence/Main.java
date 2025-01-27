package com.ghostchu.quickshop.compatibility.residence;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.ghostchu.quickshop.api.event.economy.ShopPurchaseEvent;
import com.ghostchu.quickshop.api.event.modification.ShopAuthorizeCalculateEvent;
import com.ghostchu.quickshop.api.event.modification.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.modification.ShopPreCreateEvent;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.Plugin;

import java.util.Map;

public final class Main extends CompatibilityModule {

  private static final String CREATE_FLAG = "quickshop-create";
  private static final String TRADE_FLAG = "quickshop-trade";
  private boolean whitelist;
  private boolean defaultTrade;

  @Override
  public void init() {

    final Plugin resPlug = getServer().getPluginManager().getPlugin("Residence");
    if(resPlug == null) {
      getLogger().info("Dependency not found: Residence");
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }
    whitelist = getConfig().getBoolean("whitelist-mode");
    defaultTrade = getConfig().getBoolean("trade-default", false);

    FlagPermissions.addFlag(CREATE_FLAG);
    FlagPermissions.addFlag(TRADE_FLAG);
  }

  @EventHandler(ignoreCancelled = true)
  public void onCreation(final ShopCreateEvent event) {

    final Location shopLoc = event.getShop().getLocation();
    final ClaimedResidence residence = Residence.getInstance().getResidenceManager().getByLoc(shopLoc);
    if(residence == null) {
      if(whitelist) {
        event.setCancelled(true, getApi().getTextManager().of(event.getCreator(), "addon.residence.you-cannot-create-shop-in-wildness").forLocale());
      }
      return;
    }
    event.getCreator().getBukkitPlayer().ifPresent(player->{
      if(!playerHas(residence.getPermissions(), player, CREATE_FLAG, false)) {
        if(!playerHas(Residence.getInstance().getWorldFlags().getPerms(shopLoc.getWorld().getName()), player, CREATE_FLAG, false)) {
          event.setCancelled(true, getApi().getTextManager().of(event.getCreator(), "addon.residence.creation-flag-denied").forLocale());
        }
      }
    });
  }

  private boolean playerHas(final FlagPermissions permissions, final Player player, final String name, final boolean def) {

    final Flags internalFlag = Flags.getFlag(name);
    if(internalFlag == null) {
      final Map<String, Boolean> permPlayerMap = permissions.getPlayerFlags(player.getName());
      final Map<String, Boolean> permGlobalMap = permissions.getFlags();
      if(permPlayerMap != null) {
        return permPlayerMap.getOrDefault(name, permGlobalMap.getOrDefault(name, def));
      } else {
        return permGlobalMap.getOrDefault(name, def);
      }
    } else {
      return permissions.playerHas(player, internalFlag, def);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onPreCreation(final ShopPreCreateEvent event) {

    final Location shopLoc = event.getLocation();
    final ClaimedResidence residence = Residence.getInstance().getResidenceManager().getByLoc(shopLoc);
    if(residence == null) {
      if(whitelist) {
        event.setCancelled(true, getApi().getTextManager().of(event.getCreator(), "addon.residence.you-cannot-create-shop-in-wildness").forLocale());
      }
      return;
    }
    event.getCreator().getBukkitPlayer().ifPresent(player->{
      if(!playerHas(residence.getPermissions(), player, CREATE_FLAG, false)) {
        if(!playerHas(Residence.getInstance().getWorldFlags().getPerms(shopLoc.getWorld().getName()), player, CREATE_FLAG, false)) {
          event.setCancelled(true, getApi().getTextManager().of(event.getCreator(), "addon.residence.creation-flag-denied").forLocale());
        }
      }
    });
  }

  @EventHandler(ignoreCancelled = true)
  public void onPurchase(final ShopPurchaseEvent event) {

    final Location shopLoc = event.getShop().getLocation();
    final ClaimedResidence residence = Residence.getInstance().getResidenceManager().getByLoc(shopLoc);
    if(residence == null) {
      return;
    }
    event.getPurchaser().getBukkitPlayer().ifPresent(player->{
      if(!playerHas(residence.getPermissions(), player, TRADE_FLAG, defaultTrade)) {
        if(!playerHas(Residence.getInstance().getWorldFlags().getPerms(shopLoc.getWorld().getName()), player, TRADE_FLAG, defaultTrade)) {
          event.setCancelled(true, getApi().getTextManager().of(player, "addon.residence.trade-flag-denied").forLocale());
        }
      }
    });
  }

  @EventHandler(ignoreCancelled = true)
  public void permissionOverride(final ShopAuthorizeCalculateEvent event) {

    if(!getConfig().getBoolean("allow-permission-override")) {
      return;
    }
    final Location shopLoc = event.getShop().getLocation();
    final ClaimedResidence residence = ResidenceApi.getResidenceManager().getByLoc(shopLoc);
    if(residence == null) {
      return;
    }
    if(residence.getOwnerUUID().equals(event.getAuthorizer())) {
      event.setResult(true);
    }
  }

}
