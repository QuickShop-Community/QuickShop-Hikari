/*
 *  This file is a part of project QuickShop, the name is ShopProtectionListener.java
 *  Copyright (C) Ghost_chu and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.ghostchu.quickshop.listener;

import com.ghostchu.quickshop.Cache;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.shop.datatype.HopperPersistentData;
import com.ghostchu.quickshop.shop.datatype.HopperPersistentDataType;
import com.ghostchu.quickshop.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.logging.container.ShopRemoveLog;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.logging.Level;

public class ShopProtectionListener extends AbstractProtectionListener {

    private boolean hopperProtect;
    private boolean hopperOwnerExclude;

    public ShopProtectionListener(@NotNull QuickShop plugin, @Nullable Cache cache) {
        super(plugin, cache);
        init();
    }

    private void init() {
        this.hopperProtect = plugin.getConfig().getBoolean("protect.hopper",true);
        this.hopperOwnerExclude = plugin.getConfig().getBoolean("protect.hopper-owner-exclude",false);
        scanAndFixPaperListener();
    }

    @Override
    public ReloadResult reloadModule() {
        init();
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldLoad(WorldLoadEvent event) {
        scanAndFixPaperListener();
    }

    public void scanAndFixPaperListener() {
        if (!plugin.getConfig().getBoolean("protect.hopper")) {
            return;
        }
        if (!Util.isClassAvailable("com.destroystokyo.paper.PaperWorldConfig")) {
            return;
        }
        Log.debug("QuickShop is scanning all worlds settings about disableHopperMoveEvents disabled worlds");
        plugin.getServer().getWorlds().forEach(world -> {
            if (plugin.getShopManager().getShopsInWorld(world).isEmpty()) {
                return;
            }
            try {
                Field worldServerF = world.getClass().getDeclaredField("world");
                worldServerF.setAccessible(true);
                Object worldServer = worldServerF.get(world);
                Object paperWorldConfig = worldServer.getClass().getSuperclass().getDeclaredField("paperConfig").get(worldServer);
                boolean disableHopperMoveEvents = paperWorldConfig.getClass().getDeclaredField("disableHopperMoveEvents").getBoolean(paperWorldConfig);
                if (disableHopperMoveEvents) {
                    plugin.getLogger()
                            .warning("World " + world.getName()
                                    + " have shops and Hopper protection is enabled. But we detected" +
                                    " \"disableHopperMoveEvents\" options in \"paper.yml\" is activated, so QuickShop already automatic disabled it.");
                    plugin.getLogger()
                            .warning("If you still want keep enable disableHopperMoveEvents enables " +
                                    "in this world, please disable Hopper protection or make sure no shops in this world.");
                    paperWorldConfig.getClass().getDeclaredField("disableHopperMoveEvents").setBoolean(paperWorldConfig, false);
                    File serverRoot = plugin.getDataFolder().getParentFile().getParentFile();
                    File paperConfigYaml = new File(serverRoot, "paper.yml");
                    if (paperConfigYaml.exists()) {
                        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(paperConfigYaml);
                        ConfigurationSection worldsSection =
                                Objects.requireNonNull(yamlConfiguration.getConfigurationSection("world-settings"));
                        ConfigurationSection worldSection;
                        if (Objects.requireNonNull(worldsSection).getConfigurationSection(world.getName()) == null) {
                            worldSection = worldsSection.getConfigurationSection("default");
                        } else {
                            worldSection = worldsSection.getConfigurationSection(world.getName());
                        }
                        Objects.requireNonNull(worldSection).set("hopper.disable-move-event", false);
                        Objects.requireNonNull(worldSection).set("hopper.disable-move-event-quickshop-tips", "QuickShop automatic disabled this due it will allow other players steal items from shop. This notice only shown when have shops in current world and hopper protection is on and also disable-move-event turned on.");
                        yamlConfiguration.save(paperConfigYaml);
                    }
                }
            } catch (Exception ex) {
                plugin.getLogger().log(Level.WARNING, "Failed to automatic disable disable-move-event for world [" + world.getName() + "], please disable it by yourself or player can steal items from shops.", ex);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e) {
        for (int i = 0, a = e.blockList().size(); i < a; i++) {
            final Block b = e.blockList().get(i);
            Shop shop = getShopNature(b.getLocation(), true);
            if (shop == null) {
                shop = getShopNextTo(b.getLocation());
            }
            if (shop != null) {
                if (plugin.getConfig().getBoolean("protect.explode")) {
                    e.setCancelled(true);
                } else {
                    plugin.logEvent(new ShopRemoveLog(Util.getNilUniqueId(), "BlockBreak(explode)", shop.saveToInfoStorage()));
                    shop.delete();
                }
            }
        }
    }

    /**
     * Gets the shop a sign is attached to
     *
     * @param loc The location of the sign
     * @return The shop
     */
    @Nullable
    private Shop getShopNextTo(@NotNull Location loc) {
        final Block b = Util.getAttached(loc.getBlock());
        // Util.getAttached(b)
        if (b == null) {
            return null;
        }

        return getShopNature(b.getLocation(), false);
    }

    /*
     * Handles shops breaking through explosions
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent e) {

        for (int i = 0, a = e.blockList().size(); i < a; i++) {
            final Block b = e.blockList().get(i);
            final Shop shop = getShopNature(b.getLocation(), true);

            if (shop == null) {
                continue;
            }
            if (plugin.getConfig().getBoolean("protect.explode")) {
                e.setCancelled(true);
            } else {
                plugin.logEvent(new ShopRemoveLog(Util.getNilUniqueId(), "BlockBreak(explode)", shop.saveToInfoStorage()));
                shop.delete();
            }
        }
    }
    private final NamespacedKey hopperKey = new NamespacedKey(QuickShop.getInstance(), "hopper-persistent-data");
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlaceHopper(BlockPlaceEvent e) {
        if(e.getBlockPlaced().getState() instanceof Hopper hopper){
            hopper.getPersistentDataContainer().set(hopperKey, HopperPersistentDataType.INSTANCE, new HopperPersistentData(e.getPlayer().getUniqueId()));
            hopper.update();
        }
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onInventoryMove(InventoryMoveItemEvent event) {
        if (!this.hopperProtect) {
            return;
        }
        final Location loc = event.getSource().getLocation();

        if (loc == null) {
            return;
        }
        final Shop shop = getShopRedstone(loc, true);

        if (shop == null) {
            return;
        }
        if(this.hopperOwnerExclude) {
            if(event.getDestination().getHolder() instanceof Hopper hopper){
                HopperPersistentData hopperPersistentData = hopper.getPersistentDataContainer().get(hopperKey, HopperPersistentDataType.INSTANCE);
                if (hopperPersistentData != null) {
                    if (shop.playerAuthorize(hopperPersistentData.getPlayer(), BuiltInShopPermission.ACCESS_INVENTORY)) {
                        return;
                    }
                }
            }
        }
        event.setCancelled(true);
    }
}
