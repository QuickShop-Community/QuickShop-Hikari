/*
 * This file is a part of project QuickShop, the name is ShopLoader.java
 *  Copyright (C) PotatoCraft Studio and contributors
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

package org.maxgamer.quickshop.shop;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.EnderChest;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.database.WarpedResultSet;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.api.shop.ShopModerator;
import org.maxgamer.quickshop.api.shop.ShopType;
import org.maxgamer.quickshop.api.shop.inventory.InventoryWrapper;
import org.maxgamer.quickshop.api.shop.inventory.InventoryWrapperManager;
import org.maxgamer.quickshop.shop.inventory.BukkitInventoryWrapper;
import org.maxgamer.quickshop.util.JsonUtil;
import org.maxgamer.quickshop.util.PlayerFinder;
import org.maxgamer.quickshop.util.Timer;
import org.maxgamer.quickshop.util.Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class allow plugin load shops fast and simply.
 */
public class ShopLoader {
//    private final List<Long> loadTimes = new ArrayList<>();
//
//    private final Map<Timer, Double> timeCostCache = new HashMap<>();

    private final QuickShop plugin;
    /* This may contains broken shop, must use null check before load it. */
    private int errors;
    //private final WarningSender warningSender;

    /**
     * The shop load allow plugin load shops fast and simply.
     *
     * @param plugin Plugin main class
     */
    public ShopLoader(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        //this.warningSender = new WarningSender(plugin, 15000);
    }

    public void loadShops() {
        loadShops(null);
    }

    /**
     * Load all shops in the specified world
     *
     * @param worldName The world name, null if load all shops
     */
    public void loadShops(@Nullable String worldName) {
        //boolean backupedDatabaseInDeleteProcess = false;
        this.plugin.getLogger().info("Fetching shops from the database...If plugin stuck there, check your database connection.");
        int loadAfterChunkLoaded = 0;
        int loadAfterWorldLoaded = 0;
        int loaded = 0;
        int total = 0;
        int valid = 0;
        List<Shop> pendingLoading = new ArrayList<>();

        try (WarpedResultSet warpRS = plugin.getDatabaseHelper().selectAllShops(); ResultSet rs = warpRS.getResultSet()) {
            Timer timer = new Timer();
            timer.start();
            boolean deleteCorruptShops = plugin.getConfig().getBoolean("debug.delete-corrupt-shops", false);
            this.plugin.getLogger().info("Loading shops from the database...");
            while (rs.next()) {
                ++total;
                ShopRawDatabaseInfo origin = new ShopRawDatabaseInfo(rs);
                if (worldName != null && !origin.getWorld().equals(worldName)) {
                    continue;
                }
                ShopDatabaseInfo data;
                try {
                    data = new ShopDatabaseInfo(origin);
                } catch (Exception e) {
                    exceptionHandler(e, null);
                    continue;
                }
                //World unloaded and not found
                if (data.getWorld() == null) {
                    ++loadAfterWorldLoaded;
                    continue;
                }
                Shop shop =
                        new ContainerShop(plugin,
                                data.getLocation(),
                                data.getPrice(),
                                data.getItem(),
                                data.getModerators(),
                                data.isUnlimited(),
                                data.getType(),
                                data.getExtra(),
                                data.getCurrency(),
                                data.isDisableDisplay(),
                                data.getTaxAccount(),
                                data.getInventoryWrapperProvider(),
                                data.getInventory());
                if (data.needUpdate.get()) {
                    shop.setDirty();
                }
                if (shopNullCheck(shop)) {
                    if (deleteCorruptShops) {
                        plugin.getLogger().warning("Deleting shop " + shop + " caused by corrupted.");
                        plugin.getDatabaseHelper().removeShop(origin.getWorld(), origin.getX(), origin.getY(), origin.getZ());
                    } else {
                        Util.debugLog("Trouble database loading debug: " + data);
                        Util.debugLog("Somethings gone wrong, skipping the loading...");
                    }
                    continue;
                }
                ++valid;

                Location shopLocation = shop.getLocation();
                //World unloaded but found
                if (!shopLocation.isWorldLoaded()) {
                    ++loadAfterWorldLoaded;
                    continue;
                }
                // Load to RAM
                plugin.getShopManager().loadShop(shopLocation.getWorld().getName(), shop);

                if (Util.isLoaded(shopLocation)) {
                    // Load to World
                    if (!Util.canBeShop(shopLocation.getBlock())) {
                        Util.debugLog("Target block can't be a shop, removing it from the memory...");
                        // shop.delete();
                        plugin.getShopManager().removeShop(shop); // Remove from Mem
                        //TODO: Only remove from memory, so if it actually is a bug, user won't lost all shops.
                        //TODO: Old shop will be deleted when in same location creating new shop.
                    } else {
                        pendingLoading.add(shop);
                        ++loaded;
                    }
                } else {
                    loadAfterChunkLoaded++;
                }
            }
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (Shop shop : pendingLoading) {
                    try {
                        shop.onLoad();
                    } catch (IllegalStateException exception) {
                        exceptionHandler(exception, shop.getLocation());
                    }
                    shop.update();
                }
            }, 1);
            this.plugin.getLogger().info(">> Shop Loader Information");
            this.plugin.getLogger().info("Total           shops: " + total);
            this.plugin.getLogger().info("Valid           shops: " + valid);
            this.plugin.getLogger().info("Pending              : " + loaded);
            this.plugin.getLogger().info("Waiting worlds loaded: " + loadAfterWorldLoaded);
            this.plugin.getLogger().info("Waiting chunks loaded: " + loadAfterChunkLoaded);
            this.plugin.getLogger().info("Done! Used " + timer.stopAndGetTimePassed() + "ms to loaded shops in database.");
        } catch (Exception e) {
            exceptionHandler(e, null);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private boolean shopNullCheck(@Nullable Shop shop) {
        if (shop == null) {
            Util.debugLog("Shop object is null");
            return true;
        }
        if (shop.getItem() == null) {
            Util.debugLog("Shop itemStack is null");
            return true;
        }
        if (shop.getItem().getType() == Material.AIR) {
            Util.debugLog("Shop itemStack type can't be AIR");
            return true;
        }
        if (shop.getLocation() == null) {
            Util.debugLog("Shop location is null");
            return true;
        }
        if (shop.getOwner() == null) {
            Util.debugLog("Shop owner is null");
            return true;
        }
        if (plugin.getServer().getOfflinePlayer(shop.getOwner()).getName() == null) {
            Util.debugLog("Shop owner not exist on this server, did you have reset the playerdata?");
        }
        if (shop.getInventoryWrapperProvider() == null || shop.getInventoryWrapperProvider().isEmpty()) {
            Util.debugLog("InventoryWrapperProvider not exists. The data not successfully upgrading?");
            return true;
        }
        if (shop.getInventory() == null) {
            Util.debugLog("InventoryWrapper invalid.");
            return true;
        }
        return false;
    }

    @NotNull
    private YamlConfiguration extraUpgrade(@NotNull String extraString) {
        if (!StringUtils.isEmpty(extraString) && !"QuickShop: {}".equalsIgnoreCase(extraString)) {
            Util.debugLog("Extra API -> Upgrading -> " + extraString.replaceAll("\n", ""));
        }
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        JsonConfiguration jsonConfiguration = new JsonConfiguration();
        try {
            jsonConfiguration.loadFromString(extraString);
        } catch (InvalidConfigurationException e) {
            plugin.getLogger().log(Level.WARNING, "Cannot upgrade extra data: " + extraString, e);
        }
        for (String key : jsonConfiguration.getKeys(true)) {
            yamlConfiguration.set(key, jsonConfiguration.get(key));
        }
        return yamlConfiguration;
    }

    private @NotNull YamlConfiguration deserializeExtra(@NotNull String extraString, @NotNull AtomicBoolean needUpdate) {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        try {
            if (extraString.startsWith("{")) {
                yamlConfiguration = extraUpgrade(extraString);
                needUpdate.set(true);
            } else {
                yamlConfiguration.loadFromString(extraString);
            }
        } catch (InvalidConfigurationException e) {
            yamlConfiguration = extraUpgrade(extraString);
            needUpdate.set(true);
        }
        return yamlConfiguration;
    }

    private void exceptionHandler(@NotNull Exception ex, @Nullable Location shopLocation) {
        errors++;
        Logger logger = plugin.getLogger();
        logger.warning("##########FAILED TO LOAD SHOP##########");
        logger.warning("  >> Error Info:");
        String err = ex.getMessage();
        if (err == null) {
            err = "null";
        }
        logger.warning(err);
        logger.warning("  >> Error Trace");
        ex.printStackTrace();
        logger.warning("  >> Target Location Info");
        logger.warning("Location: " + ((shopLocation == null) ? "NULL" : shopLocation.toString()));
        logger.warning(
                "Block: " + ((shopLocation == null) ? "NULL" : shopLocation.getBlock().getType().name()));
        logger.warning("#######################################");
        if (errors > 10) {
            logger.severe(
                    "QuickShop detected too many errors when loading shops, you should backup your shop database and ask the developer for help");
        }
    }

    public synchronized void recoverFromFile(@NotNull String fileContent) {
        plugin.getLogger().info("Processing the shop data...");
        String[] shopsPlain = fileContent.split("\n");
        plugin.getLogger().info("Recovering shops...");
        Gson gson = JsonUtil.getGson();
        int total = shopsPlain.length;
        List<ShopRawDatabaseInfo> list = new ArrayList<>(total);
        for (String s : shopsPlain) {
            String shopStr = s.trim();
            try {
                list.add(gson.fromJson(shopStr, ShopRawDatabaseInfo.class));
            } catch (JsonSyntaxException ignore) {
            }
        }
        plugin.getLogger().info("Processed " + total + "/" + total + " - [ Valid " + list.size() + "]");
        // Load to RAM
        Util.mainThreadRun(() -> {
            plugin.getLogger().info("Loading recovered shops...");
            for (ShopRawDatabaseInfo rawDatabaseInfo : list) {
                ShopDatabaseInfo data = new ShopDatabaseInfo(rawDatabaseInfo);
                Shop shop =
                        new ContainerShop(plugin,
                                data.getLocation(),
                                data.getPrice(),
                                data.getItem(),
                                data.getModerators(),
                                data.isUnlimited(),
                                data.getType(),
                                data.getExtra(),
                                data.getCurrency(),
                                data.isDisableDisplay(),
                                data.getTaxAccount(),
                                data.getInventoryWrapperProvider(),
                                data.getInventory());
                if (shopNullCheck(shop)) {
                    continue;
                }
                plugin.getDatabaseHelper().createShop(shop, null, null);
                plugin.getShopManager().loadShop(data.getWorld().getName(), shop);
                shop.update();
                if (Util.isLoaded(shop.getLocation()) && !shop.isLoaded()) {
                    shop.onLoad();
                }
            }
            plugin.getLogger().info("Finished!");
        });
    }

    @NotNull
    public List<ShopRawDatabaseInfo> getOriginShopsInDatabase() {
        errors = 0;
        List<ShopRawDatabaseInfo> shopRawDatabaseInfoList = new ArrayList<>();
        try (WarpedResultSet warpRS = plugin.getDatabaseHelper().selectAllShops(); ResultSet rs = warpRS.getResultSet()) {
            // this.plugin.getLogger().info("Getting shops from the database...");
            while (rs.next()) {
                ShopRawDatabaseInfo origin = new ShopRawDatabaseInfo(rs);
                shopRawDatabaseInfoList.add(origin);
            }
        } catch (SQLException e) {
            exceptionHandler(e, null);
            return Collections.emptyList();
        }
        return shopRawDatabaseInfoList;
    }

    @Getter
    @Setter
    static public class ShopRawDatabaseInfo {
        private String item;

        private String moderators;

        private double price;

        private int type;

        private boolean unlimited;

        private String world;

        private int x;

        private int y;

        private int z;

        private String extra;

        private String currency;

        private boolean disableDisplay;

        private String taxAccount;

        private String inventoryWrapperProvider;

        private String symbolLink;

        ShopRawDatabaseInfo(ResultSet rs) throws SQLException {
            this.x = rs.getInt("x");
            this.y = rs.getInt("y");
            this.z = rs.getInt("z");
            this.world = rs.getString("world");
            this.item = rs.getString("itemConfig");
            this.moderators = rs.getString("owner");
            this.price = rs.getDouble("price");
            this.type = rs.getInt("type");
            this.unlimited = rs.getBoolean("unlimited");
            this.extra = rs.getString("extra");
            //handle old shops
            if (extra == null) {
                extra = "";
            }
            this.currency = rs.getString("currency");
            this.disableDisplay = rs.getInt("disableDisplay") != 0;
            this.taxAccount = rs.getString("taxAccount");
            this.symbolLink = rs.getString("symbollink");
            this.inventoryWrapperProvider = rs.getString("inventorywrapper");
        }

        @Override
        public String toString() {
            return JsonUtil.getGson().toJson(this);
        }
    }

    @Getter
    @Setter
    public class ShopDatabaseInfo {
        private ItemStack item;

        private Location location;

        private ShopModerator moderators;

        private double price;

        private ShopType type;

        private boolean unlimited;

        private World world;

        private int x;

        private int y;

        private int z;

        private YamlConfiguration extra;

        private AtomicBoolean needUpdate = new AtomicBoolean(false);

        private String currency;

        private UUID taxAccount;

        private boolean disableDisplay;

        private String inventoryWrapperProvider;

        private InventoryWrapper inventory;

        ShopDatabaseInfo(ShopRawDatabaseInfo origin) {
            try {
                this.x = origin.getX();
                this.y = origin.getY();
                this.z = origin.getZ();
                this.world = plugin.getServer().getWorld(origin.getWorld());
                this.location = new Location(world, x, y, z);
                this.price = origin.getPrice();
                this.unlimited = origin.isUnlimited();
                this.moderators = deserializeModerator(origin.getModerators(), needUpdate);
                this.type = ShopType.fromID(origin.getType());
                this.item = deserializeItem(origin.getItem());
                this.extra = deserializeExtra(origin.getExtra(), needUpdate);
                this.currency = origin.getCurrency();
                this.disableDisplay = origin.isDisableDisplay();
                this.taxAccount = origin.getTaxAccount() != null ? UUID.fromString(origin.getTaxAccount()) : null;
                this.inventoryWrapperProvider = origin.getInventoryWrapperProvider();
                this.inventory = locateInventory(origin.getSymbolLink());
            } catch (Exception ex) {
                exceptionHandler(ex, this.location);
            }
        }

        private @NotNull InventoryWrapper locateInventory(@Nullable String symbolLink) {
            if (symbolLink == null || symbolLink.isEmpty()) {
                // Upgrading data
                Util.debugLog("Upgrading old shop data: " + this);
                BlockState block = getLocation().getBlock().getState();
                if (block instanceof BlockInventoryHolder) {
                    this.inventoryWrapperProvider = plugin.getInventoryWrapperRegistry().find(plugin.getInventoryWrapperManager());
                    this.inventory = new BukkitInventoryWrapper(((BlockInventoryHolder) block).getInventory());
                    return this.inventory;
                } else {
                    if (block instanceof EnderChest) {
                        throw new IllegalStateException("Failed to load ender chest shop: You need install QuickShop EnderChest addon to make it works.");
                    }
                    throw new IllegalArgumentException("Failed to load shop: Target block not a Container, Skipping...");
                }
            }
            InventoryWrapperManager manager = plugin.getInventoryWrapperRegistry().get(getInventoryWrapperProvider());
            if (manager == null) {
                throw new IllegalStateException("Failed load shop data, the InventoryWrapper provider " + getInventoryWrapperProvider() + " invalid or failed to load!");
            }
            try {
                return manager.locate(symbolLink);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Failed load shop data, the InventoryWrapper provider " + getInventoryWrapperProvider() + " returns error: " + e.getMessage());
            }
        }

        private @Nullable ItemStack deserializeItem(@NotNull String itemConfig) {
            try {
                return Util.deserialize(itemConfig);
            } catch (InvalidConfigurationException e) {
                plugin.getLogger().log(Level.WARNING, "Failed load shop data, because target config can't deserialize the ItemStack", e);
                Util.debugLog("Failed to load data to the ItemStack: " + itemConfig);
                return null;
            }
        }

        private @Nullable ShopModerator deserializeModerator(@NotNull String moderatorJson, AtomicBoolean needUpdate) {
            ShopModerator shopModerator;
            if (Util.isUUID(moderatorJson)) {
                Util.debugLog("Updating old shop data... for " + moderatorJson);
                shopModerator = new SimpleShopModerator(UUID.fromString(moderatorJson)); // New one
                needUpdate.set(true);
            } else {
                try {
                    shopModerator = SimpleShopModerator.deserialize(moderatorJson);
                } catch (JsonSyntaxException ex) {
                    Util.debugLog("Updating old shop data... for " + moderatorJson);
                    moderatorJson = PlayerFinder.findUUIDByName(moderatorJson).toString();
                    shopModerator = new SimpleShopModerator(UUID.fromString(moderatorJson)); // New one
                    needUpdate.set(true);
                }
            }
            return shopModerator;
        }

    }

}
