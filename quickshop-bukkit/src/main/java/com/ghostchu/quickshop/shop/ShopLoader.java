/*
 *  This file is a part of project QuickShop, the name is ShopLoader.java
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

package com.ghostchu.quickshop.shop;

import cc.carm.lib.easysql.api.SQLQuery;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopModerator;
import com.ghostchu.quickshop.api.shop.ShopType;
import com.ghostchu.quickshop.util.JsonUtil;
import com.ghostchu.quickshop.util.Timer;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.enginehub.squirrelid.Profile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private final QuickShop plugin;
    /* This may contains broken shop, must use null check before load it. */
    private int errors;

    /**
     * The shop load allow plugin load shops fast and simply.
     *
     * @param plugin Plugin main class
     */
    public ShopLoader(@NotNull QuickShop plugin) {
        this.plugin = plugin;
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
        this.plugin.getLogger().info("Fetching shops from the database...If plugin stuck there, check your database connection.");
        int loadAfterChunkLoaded = 0;
        int loadAfterWorldLoaded = 0;
        int loaded = 0;
        int total = 0;
        int valid = 0;
        List<Shop> pendingLoading = new ArrayList<>();

        try (SQLQuery warpRS = plugin.getDatabaseHelper().selectAllShops(); ResultSet rs = warpRS.getResultSet()) {
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
                if (data.getInventoryWrapperProvider() != null && !data.getInventoryWrapperProvider().isEmpty() && plugin.getInventoryWrapperRegistry().get(data.getInventoryWrapperProvider()) == null) {
                    Log.debug("InventoryWrapperProvider not exists! Shop won't be loaded!");
                    continue;
                }
                Shop shop;
                try {
                    shop = new ContainerShop(plugin,
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
                            data.getSymbolLink(),
                            data.getShopName());
                } catch (Exception e) {
                    if (e instanceof IllegalStateException) {
                        plugin.getLogger().log(Level.WARNING, "Failed to load the shop, skipping...", e);
                    }
                    exceptionHandler(e, null);
                    continue;
                }
                if (data.needUpdate.get()) {
                    shop.setDirty();
                }
                if (shopNullCheck(shop)) {
                    if (deleteCorruptShops) {
                        plugin.getLogger().warning("Deleting shop " + shop + " caused by corrupted.");
                        plugin.getDatabaseHelper().removeShop(origin.getWorld(), origin.getX(), origin.getY(), origin.getZ());
                    } else {
                        Log.debug("Trouble database loading debug: " + data);
                        Log.debug("Somethings gone wrong, skipping the loading...");
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
                        Log.debug("Target block can't be a shop, removing it from the memory...");
                        valid--;
                        plugin.getShopManager().removeShop(shop); // Remove from Mem
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
            Log.debug("Shop object is null");
            return true;
        }
        if (shop.getItem() == null) {
            Log.debug("Shop itemStack is null");
            return true;
        }
        if (shop.getItem().getType() == Material.AIR) {
            Log.debug("Shop itemStack type can't be AIR");
            return true;
        }
        if (shop.getLocation() == null) {
            Log.debug("Shop location is null");
            return true;
        }
        if (shop.getOwner() == null) {
            Log.debug("Shop owner is null");
            return true;
        }
        if (plugin.getServer().getOfflinePlayer(shop.getOwner()).getName() == null) {
            Log.debug("Shop owner not exist on this server, did you have reset the playerdata?");
        }
        return false;
    }

//    @NotNull
//    private YamlConfiguration extraUpgrade(@NotNull String extraString) {
//        if (!StringUtils.isEmpty(extraString) && !"QuickShop: {}".equalsIgnoreCase(extraString)) {
//            Log.debug("Extra API -> Upgrading -> " + extraString.replace("\n", ""));
//        }
//        YamlConfiguration yamlConfiguration = new YamlConfiguration();
//        JsonConfiguration jsonConfiguration = new JsonConfiguration();
//        try {
//            jsonConfiguration.loadFromString(extraString);
//        } catch (InvalidConfigurationException e) {
//            plugin.getLogger().log(Level.WARNING, "Cannot upgrade extra data: " + extraString, e);
//        }
//        for (String key : jsonConfiguration.getKeys(true)) {
//            yamlConfiguration.set(key, jsonConfiguration.get(key));
//        }
//        return yamlConfiguration;
//    }


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
            } catch (JsonSyntaxException jsonSyntaxException) {
                plugin.getLogger().log(Level.WARNING, "Failed to read shop Json " + shopStr, jsonSyntaxException);
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
                                data.getSymbolLink(),
                                data.getShopName());
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
        try (SQLQuery warpRS = plugin.getDatabaseHelper().selectAllShops(); ResultSet rs = warpRS.getResultSet()) {
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
    public static class ShopRawDatabaseInfo {
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

        private String shopName;

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
            this.symbolLink = rs.getString("inventorySymbolLink");
            this.inventoryWrapperProvider = rs.getString("inventoryWrapperName");
            this.shopName = rs.getString("name");
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

        private String symbolLink;

        private String shopName;

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
                this.symbolLink = origin.getSymbolLink();
                this.shopName = origin.getShopName();
            } catch (Exception ex) {
                exceptionHandler(ex, this.location);
            }
        }


        private @Nullable ItemStack deserializeItem(@NotNull String itemConfig) {
            try {
                return Util.deserialize(itemConfig);
            } catch (InvalidConfigurationException e) {
                plugin.getLogger().log(Level.WARNING, "Failed load shop data, because target config can't deserialize the ItemStack", e);
                Log.debug("Failed to load data to the ItemStack: " + itemConfig);
                return null;
            }
        }

        private @NotNull ShopModerator deserializeModerator(@NotNull String moderatorJson, @NotNull AtomicBoolean needUpdate) {
            ShopModerator shopModerator;
            if (Util.isUUID(moderatorJson)) {
                Log.debug("Updating old shop data... for " + moderatorJson);
                shopModerator = new SimpleShopModerator(UUID.fromString(moderatorJson)); // New one
                needUpdate.set(true);
            } else {
                try {
                    shopModerator = SimpleShopModerator.deserialize(moderatorJson);
                } catch (JsonSyntaxException ex) {
                    Log.debug("Updating old shop data... for " + moderatorJson);
                    Profile profile = plugin.getPlayerFinder().find(moderatorJson);
                    UUID uuid;
                    if (profile == null) {
                        Log.debug("Failed to find the player: " + moderatorJson);
                        uuid = Bukkit.getOfflinePlayer(moderatorJson).getUniqueId();
                    } else {
                        uuid = profile.getUniqueId();
                    }
                    shopModerator = new SimpleShopModerator(uuid); // New one
                    needUpdate.set(true);
                }
            }
            return shopModerator;
        }

        private @NotNull YamlConfiguration deserializeExtra(@NotNull String extraString, @NotNull AtomicBoolean needUpdate) {
            YamlConfiguration yamlConfiguration = new YamlConfiguration();
            try {
                yamlConfiguration.loadFromString(extraString);
            } catch (InvalidConfigurationException e) {
                yamlConfiguration = new YamlConfiguration();
                needUpdate.set(true);
            }
            return yamlConfiguration;
        }

    }

}
