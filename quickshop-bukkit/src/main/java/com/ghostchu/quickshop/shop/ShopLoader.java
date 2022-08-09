package com.ghostchu.quickshop.shop;

import cc.carm.lib.easysql.api.SQLQuery;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.database.bean.DataRecord;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopType;
import com.ghostchu.quickshop.database.DatabaseIOUtil;
import com.ghostchu.quickshop.database.SimpleDatabaseHelperV2;
import com.ghostchu.quickshop.util.JsonUtil;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Timer;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.google.common.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
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

    private boolean hasBackupCreated = false;


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

        try (SQLQuery warpRS = plugin.getDatabaseHelper().selectAllShops()) {
            ResultSet rs = warpRS.getResultSet();
            Timer timer = new Timer();
            timer.start();
            boolean deleteCorruptShops = plugin.getConfig().getBoolean("debug.delete-corrupt-shops", false);
            this.plugin.getLogger().info("Loading shops from the database...");
            while (rs.next()) {
                ++total;
                long shopId = rs.getLong("shop");
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                int z = rs.getInt("z");
                String world = rs.getString("world");
                if (worldName != null) {
                    if (!worldName.equals(world)) {
                        continue;
                    }
                }
                if (world == null) {
                    continue;
                }
                if (Bukkit.getWorld(world) == null) {
                    ++loadAfterWorldLoaded;
                    continue;
                }
                long dataId = plugin.getDatabaseHelper().locateShopDataId(shopId);
                if (dataId < 1) {
                    if (deleteCorruptShops) {
                        plugin.getDatabaseHelper().removeShopMap(world, x, y, z);
                    }
                    continue;
                }
                DataRecord record = plugin.getDatabaseHelper().getDataRecord(dataId);
                if (record == null) {
                    if (deleteCorruptShops) {
                        plugin.getDatabaseHelper().removeShopMap(world, x, y, z);
                    }
                    continue;
                }

                if (record.getInventorySymbolLink() != null && !record.getInventoryWrapper().isEmpty() && plugin.getInventoryWrapperRegistry().get(record.getInventoryWrapper()) == null) {
                    Log.debug("InventoryWrapperProvider not exists! Shop won't be loaded!");
                    continue;
                }

                DataRawDatabaseInfo rawInfo = new DataRawDatabaseInfo(record);

                Shop shop;
                try {
                    shop = new ContainerShop(plugin,
                            shopId,
                            new Location(Bukkit.getWorld(world), x, y, z),
                            rawInfo.getPrice(),
                            rawInfo.getItem(),
                            rawInfo.getOwner(),
                            rawInfo.isUnlimited(),
                            rawInfo.getType(),
                            rawInfo.getExtra(),
                            rawInfo.getCurrency(),
                            rawInfo.isHologram(),
                            rawInfo.getTaxAccount(),
                            rawInfo.getInvWrapper(),
                            rawInfo.getInvSymbolLink(),
                            rawInfo.getName(),
                            rawInfo.getPermissions());
                } catch (Exception e) {
                    if (e instanceof IllegalStateException) {
                        plugin.getLogger().log(Level.WARNING, "Failed to load the shop, skipping...", e);
                    }
                    exceptionHandler(e, null);
                    if (deleteCorruptShops && hasBackupCreated) {
                        plugin.getLogger().warning(MsgUtil.fillArgs("Deleting shop at world={0} x={1} y={2} z={3} caused by corrupted.", world, String.valueOf(x), String.valueOf(y), String.valueOf(z)));
                        plugin.getDatabaseHelper().removeShopMap(world, x, y, z);
                    }
                    continue;
                }
                if (rawInfo.needUpdate) {
                    shop.setDirty();
                }
                if (shopNullCheck(shop)) {
                    if (deleteCorruptShops && hasBackupCreated) {
                        plugin.getLogger().warning("Deleting shop " + shop + " caused by corrupted.");
                        plugin.getDatabaseHelper().removeShopMap(world, x, y, z);
                    } else {
                        Log.debug("Trouble database loading debug: " + rawInfo);
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

    public boolean backupToFile() {
        if (hasBackupCreated) return true;
        File file = new File(QuickShop.getInstance().getDataFolder(), "auto-backup-" + System.currentTimeMillis() + ".zip");
        DatabaseIOUtil databaseIOUtil = new DatabaseIOUtil((SimpleDatabaseHelperV2) plugin.getDatabaseHelper());
        try {
            databaseIOUtil.exportTables(file);
            hasBackupCreated = true;
            return true;
        } catch (SQLException | IOException e) {
            return false;
        }
    }
//
//    public synchronized void recoverFromFile(@NotNull String fileContent) {
//        plugin.getLogger().info("Processing the shop data...");
//        String[] shopsPlain = fileContent.split("\n");
//        plugin.getLogger().info("Recovering shops...");
//        Gson gson = JsonUtil.getGson();
//        int total = shopsPlain.length;
//        List<DataRawDatabaseInfo> list = new ArrayList<>(total);
//        for (String s : shopsPlain) {
//            String shopStr = s.trim();
//            try {
//                list.add(gson.fromJson(shopStr, DataRawDatabaseInfo.class));
//            } catch (JsonSyntaxException jsonSyntaxException) {
//                plugin.getLogger().log(Level.WARNING, "Failed to read shop Json " + shopStr, jsonSyntaxException);
//            }
//        }
//        plugin.getLogger().info("Processed " + total + "/" + total + " - [ Valid " + list.size() + "]");
//        // Load to RAM
//        Util.mainThreadRun(() -> {
//            plugin.getLogger().info("Loading recovered shops...");
//            for (ShopRawDatabaseInfo rawDatabaseInfo : list) {
//                ShopDatabaseInfo data = new ShopDatabaseInfo(rawDatabaseInfo);
//                ContainerShop shop =
//                        new ContainerShop(plugin,
//                                data.getLocation(),
//                                data.getPrice(),
//                                data.getItem(),
//                                data.getOwner(),
//                                data.isUnlimited(),
//                                data.getType(),
//                                data.getExtra(),
//                                data.getCurrency(),
//                                data.isDisableDisplay(),
//                                data.getTaxAccount(),
//                                data.getInventoryWrapperProvider(),
//                                data.getSymbolLink(),
//                                data.getShopName(),
//                                data.getPlayerGroup());
//                if (shopNullCheck(shop)) {
//                    continue;
//                }
//                try {
//                    long dataId = plugin.getDatabaseHelper().createData(shop);
//                    long shopId = plugin.getDatabaseHelper().createShop(dataId);
//                    plugin.getDatabaseHelper().createShopMap(shopId, shop.getLocation());
//                } catch (SQLException e) {
//                    plugin.getLogger().warning("Failed to recover: " + rawDatabaseInfo);
//                }
//            }
//            plugin.getLogger().info("Finished!");
//        });
//    }

    @Getter
    @Setter
    public static class DataRawDatabaseInfo {
        private UUID owner;

        private String name;
        private ShopType type;
        private String currency;

        private double price;
        private boolean unlimited;

        private boolean hologram;
        private UUID taxAccount;
        private Map<UUID, String> permissions;
        private YamlConfiguration extra;
        private String invWrapper;
        private String invSymbolLink;
        private long createTime;
        private ItemStack item;

        private boolean needUpdate = false;


        @SuppressWarnings("UnstableApiUsage")
        DataRawDatabaseInfo(@NotNull DataRecord record) {
            this.owner = record.getOwner();
            this.price = record.getPrice();
            this.type = ShopType.fromID(record.getType());
            this.unlimited = record.isUnlimited();
            String extraStr = record.getExtra();
            this.name = record.getName();
            //handle old shops
            if (extraStr == null) {
                extraStr = "";
                needUpdate = true;
            }
            this.currency = record.getCurrency();
            this.hologram = record.isHologram();
            this.taxAccount = record.getTaxAccount();
            this.invSymbolLink = record.getInventorySymbolLink();
            this.invWrapper = record.getInventoryWrapper();
            String permissionJson = record.getPermissions();
            if (!StringUtils.isEmpty(permissionJson) && MsgUtil.isJson(permissionJson)) {
                Type type = new TypeToken<Map<UUID, String>>() {
                }.getType();
                this.permissions = new HashMap<>(JsonUtil.getGson().fromJson(permissionJson, type));
            } else {
                this.permissions = new HashMap<>();
            }
            this.item = deserializeItem(record.getItem());
            this.extra = deserializeExtra(extraStr);
        }

        private @Nullable ItemStack deserializeItem(@NotNull String itemConfig) {
            try {
                return Util.deserialize(itemConfig);
            } catch (InvalidConfigurationException e) {
                QuickShop.getInstance().getLogger().log(Level.WARNING, "Failed load shop data, because target config can't deserialize the ItemStack", e);
                Log.debug("Failed to load data to the ItemStack: " + itemConfig);
                return null;
            }
        }

        private @NotNull YamlConfiguration deserializeExtra(@NotNull String extraString) {
            YamlConfiguration yamlConfiguration = new YamlConfiguration();
            try {
                yamlConfiguration.loadFromString(extraString);
            } catch (InvalidConfigurationException e) {
                yamlConfiguration = new YamlConfiguration();
                needUpdate = true;
            }
            return yamlConfiguration;
        }


        @Override
        public String toString() {
            return JsonUtil.getGson().toJson(this);
        }
    }

    @Getter
    @Setter
    public static class ShopDatabaseInfo {
        private int shopId;
        private int dataId;

        ShopDatabaseInfo(ResultSet origin) {
            try {
                this.shopId = origin.getInt("id");
                this.dataId = origin.getInt("data");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    @Getter
    @Setter
    public static class ShopMappingInfo {
        private int shopId;
        private String world;
        private int x;
        private int y;
        private int z;

        ShopMappingInfo(ResultSet origin) {
            try {
                this.shopId = origin.getInt("shop");
                this.x = origin.getInt("x");
                this.y = origin.getInt("y");
                this.z = origin.getInt("z");
                this.world = origin.getString("world");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
