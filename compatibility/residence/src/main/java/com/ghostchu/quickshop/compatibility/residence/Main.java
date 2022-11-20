package com.ghostchu.quickshop.compatibility.residence;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.ghostchu.quickshop.api.event.ShopAuthorizeCalculateEvent;
import com.ghostchu.quickshop.api.event.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.ShopPreCreateEvent;
import com.ghostchu.quickshop.api.event.ShopPurchaseEvent;
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

    @Override
    public void init() {
        Plugin resPlug = getServer().getPluginManager().getPlugin("Residence");
        if (resPlug == null) {
            getLogger().info("Dependency not found: Residence");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        whitelist = getConfig().getBoolean("whitelist-mode");
        FlagPermissions.addFlag(CREATE_FLAG);
        FlagPermissions.addFlag(TRADE_FLAG);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreation(ShopCreateEvent event) {
        Location shopLoc = event.getShop().getLocation();
        ClaimedResidence residence = Residence.getInstance().getResidenceManager().getByLoc(shopLoc);
        if (residence == null) {
            if (whitelist) {
                event.setCancelled(true, getApi().getTextManager().of(event.getPlayer(), "addon.residence.you-cannot-create-shop-in-wildness").forLocale());
            }
            return;
        }
        if (!playerHas(residence.getPermissions(), event.getPlayer(), CREATE_FLAG, false)) {
            if (!playerHas(Residence.getInstance().getWorldFlags().getPerms(shopLoc.getWorld().getName()), event.getPlayer(), CREATE_FLAG, false)) {
                event.setCancelled(true, getApi().getTextManager().of(event.getPlayer(), "addon.residence.creation-flag-denied").forLocale());
            }
        }
    }

    private boolean playerHas(FlagPermissions permissions, Player player, String name, boolean def) {
        Flags internalFlag = Flags.getFlag(name);
        if (internalFlag == null) {
            Map<String, Boolean> permPlayerMap = permissions.getPlayerFlags(player.getName());
            Map<String, Boolean> permGlobalMap = permissions.getFlags();
            if (permPlayerMap != null) {
                return permPlayerMap.getOrDefault(name, permGlobalMap.getOrDefault(name, def));
            } else {
                return permGlobalMap.getOrDefault(name, def);
            }
        } else {
            return permissions.playerHas(player, internalFlag, def);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPreCreation(ShopPreCreateEvent event) {
        Location shopLoc = event.getLocation();
        ClaimedResidence residence = Residence.getInstance().getResidenceManager().getByLoc(shopLoc);
        if (residence == null) {
            if (whitelist) {
                event.setCancelled(true, getApi().getTextManager().of(event.getPlayer(), "addon.residence.you-cannot-create-shop-in-wildness").forLocale());
            }
            return;
        }
        if (!playerHas(residence.getPermissions(), event.getPlayer(), CREATE_FLAG, false)) {
            if (!playerHas(Residence.getInstance().getWorldFlags().getPerms(shopLoc.getWorld().getName()), event.getPlayer(), CREATE_FLAG, false)) {
                event.setCancelled(true, getApi().getTextManager().of(event.getPlayer(), "addon.residence.creation-flag-denied").forLocale());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPurchase(ShopPurchaseEvent event) {
        Location shopLoc = event.getShop().getLocation();
        ClaimedResidence residence = Residence.getInstance().getResidenceManager().getByLoc(shopLoc);
        if (residence == null) {
            return;
        }
        if (!playerHas(residence.getPermissions(), event.getPlayer(), TRADE_FLAG, false)) {
            if (!playerHas(Residence.getInstance().getWorldFlags().getPerms(shopLoc.getWorld().getName()), event.getPlayer(), TRADE_FLAG, false)) {
                event.setCancelled(true, getApi().getTextManager().of(event.getPlayer(), "addon.residence.trade-flag-denied").forLocale());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void permissionOverride(ShopAuthorizeCalculateEvent event) {
        if (!getConfig().getBoolean("allow-permission-override")) {
            return;
        }
        Location shopLoc = event.getShop().getLocation();
        ClaimedResidence residence = ResidenceApi.getResidenceManager().getByLoc(shopLoc);
        if (residence == null) {
            return;
        }
        if (residence.getOwnerUUID().equals(event.getAuthorizer())) {
            event.setResult(true);
        }
    }

}
