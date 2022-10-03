package com.ghostchu.quickshop.compatibility.worldguard;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.ShopAuthorizeCalculateEvent;
import com.ghostchu.quickshop.api.event.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.ShopPreCreateEvent;
import com.ghostchu.quickshop.api.event.ShopPurchaseEvent;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.logging.Level;

public final class Main extends CompatibilityModule implements Listener {
    private StateFlag createFlag;
    private StateFlag tradeFlag;

    @Override
    public void onLoad() {
        saveDefaultConfig();
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            // create a flag with the name "my-custom-flag", defaulting to true
            StateFlag createFlag = new StateFlag("quickshophikari-create", getConfig().getBoolean("create.default-allow", false));
            StateFlag tradeFlag = new StateFlag("quickshophikari-trade", getConfig().getBoolean("trade.default-allow", true));
            registry.register(createFlag);
            registry.register(tradeFlag);
            this.createFlag = createFlag;
            this.tradeFlag = tradeFlag;
        } catch (FlagConflictException e) {
            // some other plugin registered a flag by the same name already.
            // you can use the existing flag, but this may cause conflicts - be sure to check type
            Flag<?> existing = registry.get("quickshophikari-create");
            if (existing instanceof StateFlag createFlag) {
                this.createFlag = createFlag;
            } else {
                getLogger().log(Level.WARNING, "Could not register flags! CONFLICT!", e);
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
            existing = registry.get("quickshophikari-reade");
            if (existing instanceof StateFlag tradeFlag) {
                this.tradeFlag = tradeFlag;
            } else {
                getLogger().log(Level.WARNING, "Could not register flags! CONFLICT!", e);
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        }
        super.onLoad();
    }


    @Override
    public void init() {

    }

    @EventHandler(ignoreCancelled = true)
    public void permissionOverride(ShopAuthorizeCalculateEvent event) {
        Location shopLoc = event.getShop().getLocation();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(BukkitAdapter.adapt(shopLoc.getWorld()));
        if (manager == null) {
            return;
        }
        ApplicableRegionSet set = manager.getApplicableRegions(BlockVector3.at(shopLoc.getX(), shopLoc.getY(), shopLoc.getZ()));
        for (ProtectedRegion region : set.getRegions()) {
            if (region.getOwners().contains(event.getAuthorizer())) {
                if (event.getNamespace().equals(QuickShop.getInstance()) && event.getPermission().equals(BuiltInShopPermission.DELETE.getRawNode())) {
                    event.setResult(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void preCreation(ShopPreCreateEvent event) {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(event.getPlayer());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        if (!query.testState(BukkitAdapter.adapt(event.getLocation()), localPlayer, this.createFlag)) {
            event.setCancelled(true, getApi().getTextManager().of(event.getPlayer(),"addon.worldguard.creation-flag-test-failed").forLocale());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void preCreation(ShopCreateEvent event) {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(event.getPlayer());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        if (!query.testState(BukkitAdapter.adapt(event.getShop().getLocation()), localPlayer, this.createFlag)) {
            event.setCancelled(true, getApi().getTextManager().of(event.getPlayer(),"addon.worldguard.creation-flag-test-failed").forLocale());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void preCreation(ShopPurchaseEvent event) {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(event.getPlayer());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        if (!query.testState(BukkitAdapter.adapt(event.getShop().getLocation()), localPlayer, this.tradeFlag)) {
            event.setCancelled(true, getApi().getTextManager().of(event.getPlayer(),"addon.worldguard.trade-flag-test-failed").forLocale());
        }
    }
}
