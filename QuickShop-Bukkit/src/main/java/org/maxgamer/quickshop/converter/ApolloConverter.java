package org.maxgamer.quickshop.converter;

import cc.carm.lib.easysql.EasySQL;
import cc.carm.lib.easysql.api.SQLManager;
import cc.carm.lib.easysql.hikari.HikariConfig;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.h2.Driver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.database.SimpleDatabaseHelper;
import org.maxgamer.quickshop.shop.InteractionController;
import org.maxgamer.quickshop.shop.inventory.BukkitInventoryWrapperManager;
import org.maxgamer.quickshop.util.JsonUtil;
import org.maxgamer.quickshop.util.Util;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApolloConverter {
    private final QuickShop plugin;
    private final Logger logger = Logger.getLogger("ApolloConverter");

    public ApolloConverter(QuickShop plugin) {
        this.plugin = plugin;
    }

    public void upgrade() {
        int selectedVersion = plugin.getConfig().getInt("config-version");
        if (selectedVersion >= 157) {
            Util.debugLog("Skipping configuration upgrading...");
            return;
        }

        logger.info("Warning! High Risk Operation alert!");
        logger.info("!! BACKUP YOUR DATA BEFORE CONTINUE !!");
        logger.info("");
        logger.info("QuickShop need upgrade your database and configuration to continue.");
        logger.info("If you don't backup your data, You have huge chance will lose all your data.");
        logger.info("If you hadn't backup your data yet, please close the server and backup immediately.");
        logger.info("**Include your map, datafolder, plugins, database, EVERYTHING!**");
        logger.info("");
        logger.info("When you get ready, create a file named 'continue.txt' in the quickshop data folder.");

        while(true){
            File file = new File(plugin.getDataFolder(), "continue.txt");
            if(file.exists())break;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }





        logger.info("You configuration version at " + selectedVersion + "...");
        logger.info("You are on " + (156 - selectedVersion) + " version(s) behind...");
        logger.info("We will execute legacy upgrade script to make sure you configuration up-to-date...");
        upgradeLegacyConfig(selectedVersion);
        logger.info("Converting configuration to Apollo...");
        convertApolloConfig();
        logger.info("Converting database to Apollo...");
        convertDatabase();
    }

    private void convertDatabase() {
        logger.info("Convert -> Getting database...");
        ConfigurationSection dbCfg = plugin.getConfig().getConfigurationSection("database");
        if(dbCfg == null){
            logger.warning("No database configuration found!");
            return;
        }
        String dbPrefix = dbCfg.getString("prefix");
        if (dbPrefix == null || "none".equals(dbPrefix)) {
            dbPrefix = "";
        }

        if (plugin.getConfig().getBoolean("database.mysql")) {
            logger.info("Convert -> MySQL database upgrade...");
            migrateMySQL(dbPrefix);
        }else{
            logger.info("Convert -> SQLite database upgrade...");
            migrateSQLite(dbPrefix);
        }
    }

    @Nullable
    private SQLManager getLiveDatabase() {
        HikariConfig config = new HikariConfig();
        config.addDataSourceProperty("connection-timeout", "60000");
        config.addDataSourceProperty("validation-timeout", "3000");
        config.addDataSourceProperty("idle-timeout", "60000");
        config.addDataSourceProperty("login-timeout", "5");
        config.addDataSourceProperty("maxLifeTime", "60000");
        config.addDataSourceProperty("maximum-pool-size", "8");
        config.addDataSourceProperty("minimum-idle", "10");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useUnicode", "true");
        config.addDataSourceProperty("characterEncoding", "utf8");

        try {
            ConfigurationSection dbCfg = plugin.getConfig().getConfigurationSection("database");
            if (Objects.requireNonNull(dbCfg).getBoolean("mysql")) {
                // MySQL database - Required database be created first.
                String dbPrefix = dbCfg.getString("prefix");
                if (dbPrefix == null || "none".equals(dbPrefix)) {
                    dbPrefix = "";
                }
                String user = dbCfg.getString("user");
                String pass = dbCfg.getString("password");
                String host = dbCfg.getString("host");
                String port = dbCfg.getString("port");
                String database = dbCfg.getString("database");
                boolean useSSL = dbCfg.getBoolean("usessl");
                config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + useSSL);
                config.setUsername(user);
                config.setPassword(pass);
                return EasySQL.createManager(config);
            } else {
                // SQLite database - Doing this handles file creation
                Driver.load();
                config.setJdbcUrl("jdbc:h2:" + new File(plugin.getDataFolder(), "shops").getCanonicalFile().getAbsolutePath() + ";DB_CLOSE_DELAY=-1;MODE=MYSQL");
                SQLManager manager = EasySQL.createManager(config);
                manager.executeSQL("SET MODE=MYSQL"); // Switch to MySQL mode
                return manager;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed connect to live database!", e);
            return null;
        }
    }

    @SuppressWarnings("SqlResolve")
    private void migrateSQLite(String prefix) {
        File sqliteFile = new File(plugin.getDataFolder(), "shops.db");
        if (!sqliteFile.exists()) return; // Skip!
        logger.info("Detecting SQLite JDBC driver...");
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            logger.log(Level.WARNING, "Couldn't found SQLite JDBC driver! Upgrade failed, Skipping...", e);
            return;
        }
        logger.info("Please standby, connecting to SQLite database...");
        List<ShopStorageUnit> shopStorageUnits;
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteFile);
             ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM " + prefix + "shops")) {
            logger.info("Reading shops data...");
            shopStorageUnits = pullShops(resultSet);
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Couldn't connect to SQLite database! Upgrade failed, Skipping...", e);
            return;
        }

        SQLManager sqlManager = getLiveDatabase();
        try {
            SimpleDatabaseHelper helper = new SimpleDatabaseHelper(plugin, sqlManager);
            logger.info("Migrating SQLite data to live database...");
            // Empty the remote database exists tables...
            logger.info("Dropping live database old tables...");
            logger.info("Dropping shops...");
            sqlManager.executeSQL("DROP TABLE IF EXISTS " + prefix + "shops");
            logger.info("Dropping messages...");
            sqlManager.executeSQL("DROP TABLE IF EXISTS " + prefix + "messages");
            logger.info("Dropping external_cache...");
            sqlManager.executeSQL("DROP TABLE IF EXISTS " + prefix + "external_cache");
            logger.info("Creating shops table...");
            helper.createShopsTable();
            logger.info("Copying data to live database...");
            AtomicInteger failCount = new AtomicInteger(0);
            for (int i = 0; i < shopStorageUnits.size(); i++) {
                ShopStorageUnit unit = shopStorageUnits.get(i);
                logger.info("Copying " + i+1 + "/" + shopStorageUnits.size() + "...");
                sqlManager.createInsert(prefix + "shops")
                        .setColumnNames("owner", "price", "itemConfig", "x", "y", "z", "world", "unlimited",
                                "type", "extra", "currency", "disableDisplay", "taxAccount", "inventorySymbolLink", "inventoryWrapperName")
                        .setParams(unit.getOwner(), unit.getPrice(), unit.getItemConfig(), unit.getX(), unit.getY(), unit.getZ(), unit.getWorld(), unit.getUnlimited(),
                                unit.getType(), unit.getExtra(), unit.getCurrency(), unit.getDisableDisplay(), unit.getTaxAccount(), unit.getInventorySymbolLink(), unit.getInventoryWrapperName())
                        .execute((exception, sqlAction) -> {
                            logger.log(Level.WARNING, "Couldn't copy data to live database! SQL:" + sqlAction, exception);
                            failCount.incrementAndGet();
                        });
            }
            logger.info("Completed. Total " + failCount.get() + " failed.");
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Couldn't create database helper!", e);
        }
    }

    @SuppressWarnings("SqlResolve")
    private void migrateMySQL(String prefix) {
        logger.info("Please standby, connecting to MySQL database...");
        List<ShopStorageUnit> shopStorageUnits;
        try (Connection connection = getLiveDatabase().getConnection();
             ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM " + prefix + "shops")) {
            logger.info("Reading shops data...");
            shopStorageUnits = pullShops(resultSet);
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Couldn't connect to MySQL database! Upgrade failed, Skipping...", e);
            return;
        }

        SQLManager sqlManager = getLiveDatabase();
        try {
            SimpleDatabaseHelper helper = new SimpleDatabaseHelper(plugin, sqlManager);
            logger.info("Migrating MySQL data to live database...");
            // Empty the remote database exists tables...
            logger.info("Dropping live database old tables...");
            logger.info("Dropping shops...");
            sqlManager.executeSQL("DROP TABLE IF EXISTS " + prefix + "shops");
            logger.info("Dropping messages...");
            sqlManager.executeSQL("DROP TABLE IF EXISTS " + prefix + "messages");
            logger.info("Dropping external_cache...");
            sqlManager.executeSQL("DROP TABLE IF EXISTS " + prefix + "external_cache");
            logger.info("Creating shops table...");
            helper.createShopsTable();
            logger.info("Copying data to live database...");
            AtomicInteger failCount = new AtomicInteger(1);
            for (int i = 0; i < shopStorageUnits.size(); i++) {
                ShopStorageUnit unit = shopStorageUnits.get(i);
                logger.info("Copying " + i + "/" + shopStorageUnits.size() + "...");
                sqlManager.createInsert(prefix + "shops")
                        .setColumnNames("owner", "price", "itemConfig", "x", "y", "z", "world", "unlimited",
                                "type", "extra", "currency", "disableDisplay", "taxAccount", "inventorySymbolLink", "inventoryWrapperName")
                        .setParams(unit.getOwner(), unit.getPrice(), unit.getItemConfig(), unit.getX(), unit.getY(), unit.getZ(), unit.getWorld(), unit.getUnlimited(),
                                unit.getType(), unit.getExtra(), unit.getCurrency(), unit.getDisableDisplay(), unit.getTaxAccount(), unit.getInventorySymbolLink(), unit.getInventoryWrapperName())
                        .execute((exception, sqlAction) -> {
                            logger.log(Level.WARNING, "Couldn't copy data to live database! SQL:" + sqlAction, exception);
                            failCount.incrementAndGet();
                        });
            }
            logger.info("Completed. Total " + failCount.get() + " failed.");
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Couldn't create database helper!", e);
        }
    }

    @NotNull
    private List<ShopStorageUnit> pullShops(ResultSet resultSet) throws SQLException {
        List<ShopStorageUnit> units = new ArrayList<>();
        int count = 0;
        while (resultSet.next()) {
            ++count;
            logger.info("Pulling #" +count+ " shop...");
            ShopStorageUnit.ShopStorageUnitBuilder builder = ShopStorageUnit.builder();
            builder.owner(resultSet.getString("owner"));
            builder.price(resultSet.getDouble("price"));
            builder.itemConfig(resultSet.getString("itemConfig"));
            builder.x(resultSet.getInt("x"));
            builder.y(resultSet.getInt("y"));
            builder.z(resultSet.getInt("z"));
            builder.world(resultSet.getString("world"));
            builder.unlimited(resultSet.getInt("unlimited"));
            builder.type(resultSet.getInt("type"));
            try {
                builder.currency(resultSet.getString("currency"));
            } catch (SQLException ignored) {
            }
            try {
                builder.extra(resultSet.getString("extra"));
            } catch (SQLException ignored) {
                builder.currency("QuickShop: {}");
            }
            try {
                builder.disableDisplay(resultSet.getInt("disableDisplay"));
            } catch (SQLException ignored) {
                builder.disableDisplay(0);
            }
            try {
                builder.taxAccount(resultSet.getString("taxAccount"));
            } catch (SQLException ignored) {
            }
            units.add(builder.build());
        }
        return units;

    }

    private void convertApolloConfig() {
        logger.info("Convert -> InteractionController");
        legacyInteractConfig();
        logger.info("Convert -> PriceLimiter");
        legacyPriceLimiter();
        logger.info("Convert -> Removing old configs...");
        removeOldConfig();
    }

    private void legacyPriceLimiter() {
        boolean wholeNumbersOnly = plugin.getConfig().getBoolean("shop.whole-number-prices-only");
        double globalMin = plugin.getConfig().getDouble("shop.minimum-price", 0.01d);
        globalMin = plugin.getConfig().getBoolean("allow-free-shop", false) ? 0.0d : globalMin;
        double globalMax = plugin.getConfig().getDouble("shop.maximum-price", -1d);
        List<String> oldRules = plugin.getConfig().getStringList("shop.price-restriction");
        File file = new File(plugin.getDataFolder(), "price-restriction.yml");
        if (!file.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed to create price-restriction.yml, upgrade failed, skipping...", e);
                return;
            }
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("version", 1);
        config.set("whole-number-only", wholeNumbersOnly);
        config.set("undefined.min", globalMin);
        config.set("undefined.max", globalMax);
        config.set("rules.example", null);
        if (oldRules.isEmpty())
            return;
        config.set("enable", true);
        for (String rule : oldRules) {
            String[] split = rule.split(" ");
            if (split.length != 3)
                continue;
            try {
                Material item = Material.matchMaterial(split[0]);
                if (item == null) continue;
                double min = Double.parseDouble(split[1]);
                double max = Double.parseDouble(split[2]);
                config.set("rules.auto-upgrade-" + item.name() + ".materials", ImmutableList.of(item.name()));
                config.set("rules.auto-upgrade-" + item.name() + ".currency", ImmutableList.of("*"));
                config.set("rules.auto-upgrade-" + item.name() + ".min", min);
                config.set("rules.auto-upgrade-" + item.name() + ".min", max);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to parse rule: " + rule + ", skipping...", e);
            }
        }
        try {
            config.save(file);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to save price-restriction.yml, upgrade failed, skipping...", e);
        }
    }

    private void removeOldConfig() {
        plugin.getConfig().set("shop.enchance-display-protect", null);
        plugin.getConfig().set("shop.enchance-shop-protect", null);
        plugin.getConfig().set("shop.interact", null);
        plugin.getConfig().set("shop.whole-number-prices-only", null);
        plugin.getConfig().set("shop.minimum-price", null);
        plugin.getConfig().set("shop.maximum-price", null);
        plugin.getConfig().set("shop.price-restriction", null);
        plugin.getConfig().set("database.queue", null);
        plugin.getConfig().set("database.queue-commit-interval", null);
        plugin.getConfig().set("database.auto-fix-encoding-issue-in-database", null);
        plugin.getConfig().set("config-version",157);
    }

    private void legacyInteractConfig() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("shop.interact");
        if (section == null) {
            logger.warning("No legacy shop.interact section found, upgrade failed, skipping...");
            return;
        }
        int interactMode = section.getInt("interact-mode", 0);
        boolean sneakToTrade = section.getBoolean("sneak-to-trade", false);
        boolean sneakToControl = section.getBoolean("sneak-to-control", false);
        boolean swapClickBehavior = section.getBoolean("swap-click-behavior", false);

        logger.info("interact -> interactMode: " + interactMode);
        logger.info("interact -> sneakToTrade: " + sneakToTrade);
        logger.info("interact -> sneakToControl: " + sneakToControl);
        logger.info("interact -> swapClickBehavior: " + swapClickBehavior);

        logger.info("Migrating interact...");

        File file = new File(plugin.getDataFolder(), "interaction.yml");
        if (!file.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed to create interaction.yml, skipping...", e);
                return;
            }
        }

        InteractionController.InteractionBehavior STANDING_LEFT_CLICK_SIGN = InteractionController.InteractionBehavior.TRADE_INTERACTION;
        InteractionController.InteractionBehavior STANDING_RIGHT_CLICK_SIGN = InteractionController.InteractionBehavior.CONTROL_PANEL;
        InteractionController.InteractionBehavior STANDING_LEFT_CLICK_SHOPBLOCK = InteractionController.InteractionBehavior.TRADE_INTERACTION;
        InteractionController.InteractionBehavior STANDING_RIGHT_CLICK_SHOPBLOCK = InteractionController.InteractionBehavior.TRADE_INTERACTION;
        InteractionController.InteractionBehavior SNEAKING_LEFT_CLICK_SIGN = InteractionController.InteractionBehavior.NONE;
        InteractionController.InteractionBehavior SNEAKING_RIGHT_CLICK_SIGN = InteractionController.InteractionBehavior.NONE;
        InteractionController.InteractionBehavior SNEAKING_LEFT_CLICK_SHOPBLOCK = InteractionController.InteractionBehavior.NONE;
        InteractionController.InteractionBehavior SNEAKING_RIGHT_CLICK_SHOPBLOCK = InteractionController.InteractionBehavior.NONE;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("version", 1);

        switch (interactMode) {
            case 0 -> {
                if (sneakToControl) {
                    STANDING_RIGHT_CLICK_SIGN = InteractionController.InteractionBehavior.NONE;
                    SNEAKING_RIGHT_CLICK_SIGN = InteractionController.InteractionBehavior.CONTROL_PANEL;
                }
                if (sneakToTrade) {
                    STANDING_LEFT_CLICK_SHOPBLOCK = InteractionController.InteractionBehavior.NONE;
                    STANDING_LEFT_CLICK_SIGN = InteractionController.InteractionBehavior.NONE;
                    SNEAKING_LEFT_CLICK_SHOPBLOCK = InteractionController.InteractionBehavior.TRADE_INTERACTION;
                    SNEAKING_LEFT_CLICK_SIGN = InteractionController.InteractionBehavior.TRADE_INTERACTION;
                }
            }
            case 1 -> {
                if (sneakToControl) {
                    SNEAKING_RIGHT_CLICK_SIGN = InteractionController.InteractionBehavior.CONTROL_PANEL;
                }
                if (sneakToTrade) {
                    SNEAKING_LEFT_CLICK_SIGN = InteractionController.InteractionBehavior.TRADE_INTERACTION;
                    SNEAKING_LEFT_CLICK_SHOPBLOCK = InteractionController.InteractionBehavior.TRADE_INTERACTION;
                }
            }
            case 2 -> {
                // disable sneak, so nothing
            }
            default -> logger.warning("Unknown interact mode, skipping...");
        }

        if (swapClickBehavior) {
            InteractionController.InteractionBehavior tmp;
            // CLICK_SIGN
            tmp = SNEAKING_LEFT_CLICK_SIGN;
            SNEAKING_LEFT_CLICK_SIGN = SNEAKING_RIGHT_CLICK_SIGN;
            SNEAKING_RIGHT_CLICK_SIGN = tmp;
        }

        config.set("STANDING_LEFT_CLICK_SIGN", STANDING_LEFT_CLICK_SIGN.name());
        config.set("STANDING_RIGHT_CLICK_SIGN", STANDING_RIGHT_CLICK_SIGN.name());
        config.set("STANDING_LEFT_CLICK_SHOPBLOCK", STANDING_LEFT_CLICK_SHOPBLOCK.name());
        config.set("STANDING_RIGHT_CLICK_SHOPBLOCK", STANDING_RIGHT_CLICK_SHOPBLOCK.name());
        config.set("SNEAKING_LEFT_CLICK_SIGN", SNEAKING_LEFT_CLICK_SIGN.name());
        config.set("SNEAKING_RIGHT_CLICK_SIGN", SNEAKING_RIGHT_CLICK_SIGN.name());
        config.set("SNEAKING_LEFT_CLICK_SHOPBLOCK", SNEAKING_LEFT_CLICK_SHOPBLOCK.name());
        config.set("SNEAKING_RIGHT_CLICK_SHOPBLOCK", SNEAKING_RIGHT_CLICK_SHOPBLOCK.name());

        try {
            config.save(file);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to save interaction configuration, upgrade failed, skipping...", e);
        }
        logger.info("Successfully upgrade interaction configuration to Apollo - version 1");
    }

    private void upgradeLegacyConfig(int selectedVersion) {
        plugin.getLogger().info("[ApolloConverter] Legacy upgrade script executing...");
        if (selectedVersion == 1) {
            plugin.getConfig().set("disabled-metrics", false);
            plugin.getConfig().set("config-version", 2);
            selectedVersion = 2;
        }
        if (selectedVersion == 2) {
            plugin.getConfig().set("protect.minecart", true);
            plugin.getConfig().set("protect.entity", true);
            plugin.getConfig().set("protect.redstone", true);
            plugin.getConfig().set("protect.structuregrow", true);
            plugin.getConfig().set("protect.explode", true);
            plugin.getConfig().set("protect.hopper", true);
            plugin.getConfig().set("config-version", 3);
            selectedVersion = 3;
        }
        if (selectedVersion == 3) {
            plugin.getConfig().set("shop.alternate-currency-symbol", '$');
            plugin.getConfig().set("config-version", 4);
            selectedVersion = 4;
        }
        if (selectedVersion == 4) {
            plugin.getConfig().set("updater", true);
            plugin.getConfig().set("config-version", 5);
            selectedVersion = 5;
        }
        if (selectedVersion == 5) {
            plugin.getConfig().set("config-version", 6);
            selectedVersion = 6;
        }
        if (selectedVersion == 6) {
            plugin.getConfig().set("shop.sneak-to-control", false);
            plugin.getConfig().set("config-version", 7);
            selectedVersion = 7;
        }
        if (selectedVersion == 7) {
            plugin.getConfig().set("database.prefix", "none");
            plugin.getConfig().set("config-version", 8);
            selectedVersion = 8;
        }
        if (selectedVersion == 8) {
            plugin.getConfig().set("limits.old-algorithm", false);
            plugin.getConfig().set("plugin.ProtocolLib", false);
            plugin.getConfig().set("shop.ignore-unlimited", false);
            plugin.getConfig().set("config-version", 9);
            selectedVersion = 9;
        }
        if (selectedVersion == 9) {
            plugin.getConfig().set("shop.enable-enderchest", true);
            plugin.getConfig().set("config-version", 10);
            selectedVersion = 10;
        }
        if (selectedVersion == 10) {
            plugin.getConfig().set("shop.pay-player-from-unlimited-shop-owner", null); // Removed
            plugin.getConfig().set("config-version", 11);
            selectedVersion = 11;
        }
        if (selectedVersion == 11) {
            plugin.getConfig().set("shop.enable-enderchest", null); // Removed
            plugin.getConfig().set("plugin.OpenInv", true);
            List<String> shoppable = plugin.getConfig().getStringList("shop-blocks");
            shoppable.add("ENDER_CHEST");
            plugin.getConfig().set("shop-blocks", shoppable);
            plugin.getConfig().set("config-version", 12);
            selectedVersion = 12;
        }
        if (selectedVersion == 12) {
            plugin.getConfig().set("plugin.ProtocolLib", null); // Removed
            plugin.getConfig().set("plugin.BKCommonLib", null); // Removed
            plugin.getConfig().set("database.use-varchar", null); // Removed
            plugin.getConfig().set("database.reconnect", null); // Removed
            plugin.getConfig().set("display-items-check-ticks", 1200);
            plugin.getConfig().set("shop.bypass-owner-check", null); // Removed
            plugin.getConfig().set("config-version", 13);
            selectedVersion = 13;
        }
        if (selectedVersion == 13) {
            plugin.getConfig().set("config-version", 14);
            selectedVersion = 14;
        }
        if (selectedVersion == 14) {
            plugin.getConfig().set("plugin.AreaShop", null);
            plugin.getConfig().set("shop.special-region-only", null);
            plugin.getConfig().set("config-version", 15);
            selectedVersion = 15;
        }
        if (selectedVersion == 15) {
            plugin.getConfig().set("ongoingfee", null);
            plugin.getConfig().set("shop.display-item-show-name", false);
            plugin.getConfig().set("shop.auto-fetch-shop-messages", true);
            plugin.getConfig().set("config-version", 16);
            selectedVersion = 16;
        }
        if (selectedVersion == 16) {
            plugin.getConfig().set("config-version", 17);
            selectedVersion = 17;
        }
        if (selectedVersion == 17) {
            plugin.getConfig().set("ignore-cancel-chat-event", false);
            plugin.getConfig().set("float", null);
            plugin.getConfig().set("config-version", 18);
            selectedVersion = 18;
        }
        if (selectedVersion == 18) {
            plugin.getConfig().set("shop.disable-vault-format", false);
            plugin.getConfig().set("config-version", 19);
            selectedVersion = 19;
        }
        if (selectedVersion == 19) {
            plugin.getConfig().set("shop.allow-shop-without-space-for-sign", true);
            plugin.getConfig().set("config-version", 20);
            selectedVersion = 20;
        }
        if (selectedVersion == 20) {
            plugin.getConfig().set("shop.maximum-price", -1);
            plugin.getConfig().set("config-version", 21);
            selectedVersion = 21;
        }
        if (selectedVersion == 21) {
            plugin.getConfig().set("shop.sign-material", "OAK_WALL_SIGN");
            plugin.getConfig().set("config-version", 22);
            selectedVersion = 22;
        }
        if (selectedVersion == 22) {
            plugin.getConfig().set("include-offlineplayer-list", "false");
            plugin.getConfig().set("config-version", 23);
            selectedVersion = 23;
        }
        if (selectedVersion == 23) {
            plugin.getConfig().set("lockette.enable", null);
            plugin.getConfig().set("lockette.item", null);
            plugin.getConfig().set("lockette.lore", null);
            plugin.getConfig().set("lockette.displayname", null);
            plugin.getConfig().set("float", null);
            plugin.getConfig().set("lockette.enable", true);
            plugin.getConfig().set("shop.blacklist-world", Lists.newArrayList("disabled_world_name"));
            plugin.getConfig().set("config-version", 24);
            selectedVersion = 24;
        }
        if (selectedVersion == 24) {
            plugin.getConfig().set("config-version", 25);
            selectedVersion = 25;
        }
        if (selectedVersion == 25) {
            String language = plugin.getConfig().getString("language");
            if (language == null || language.isEmpty() || "default".equals(language)) {
                plugin.getConfig().set("language", "en");
            }
            plugin.getConfig().set("config-version", 26);
            selectedVersion = 26;
        }
        if (selectedVersion == 26) {
            plugin.getConfig().set("database.usessl", false);
            plugin.getConfig().set("config-version", 27);
            selectedVersion = 27;
        }
        if (selectedVersion == 27) {
            plugin.getConfig().set("queue.enable", true);
            plugin.getConfig().set("queue.shops-per-tick", 20);
            plugin.getConfig().set("config-version", 28);
            selectedVersion = 28;
        }
        if (selectedVersion == 28) {
            plugin.getConfig().set("database.queue", true);
            plugin.getConfig().set("config-version", 29);
            selectedVersion = 29;
        }
        if (selectedVersion == 29) {
            plugin.getConfig().set("plugin.Multiverse-Core", null);
            plugin.getConfig().set("shop.protection-checking", true);
            plugin.getConfig().set("config-version", 30);
            selectedVersion = 30;
        }
        if (selectedVersion == 30) {
            plugin.getConfig().set("auto-report-errors", true);
            plugin.getConfig().set("config-version", 31);
            selectedVersion = 31;
        }
        if (selectedVersion == 31) {
            plugin.getConfig().set("shop.display-type", 0);
            plugin.getConfig().set("config-version", 32);
            selectedVersion = 32;
        }
        if (selectedVersion == 32) {
            plugin.getConfig().set("effect.sound.ontabcomplete", true);
            plugin.getConfig().set("effect.sound.oncommand", true);
            plugin.getConfig().set("effect.sound.ononclick", true);
            plugin.getConfig().set("config-version", 33);
            selectedVersion = 33;
        }
        if (selectedVersion == 33) {
            plugin.getConfig().set("matcher.item.damage", true);
            plugin.getConfig().set("matcher.item.displayname", true);
            plugin.getConfig().set("matcher.item.lores", true);
            plugin.getConfig().set("matcher.item.enchs", true);
            plugin.getConfig().set("matcher.item.potions", true);
            plugin.getConfig().set("matcher.item.attributes", true);
            plugin.getConfig().set("matcher.item.itemflags", true);
            plugin.getConfig().set("matcher.item.custommodeldata", true);
            plugin.getConfig().set("config-version", 34);
            selectedVersion = 34;
        }
        if (selectedVersion == 34) {
            if (plugin.getConfig().getInt("shop.display-items-check-ticks") == 1200) {
                plugin.getConfig().set("shop.display-items-check-ticks", 6000);
            }
            plugin.getConfig().set("config-version", 35);
            selectedVersion = 35;
        }
        if (selectedVersion == 35) {
            plugin.getConfig().set("queue", null); // Close it for everyone
            plugin.getConfig().set("config-version", 36);
            selectedVersion = 36;
        }
        if (selectedVersion == 36) {
            plugin.getConfig().set("economy-type", 0); // Close it for everyone
            plugin.getConfig().set("config-version", 37);
            selectedVersion = 37;
        }
        if (selectedVersion == 37) {
            plugin.getConfig().set("shop.ignore-cancel-chat-event", true);
            plugin.getConfig().set("config-version", 38);
            selectedVersion = 38;
        }
        if (selectedVersion == 38) {
            plugin.getConfig().set("protect.inventorymove", true);
            plugin.getConfig().set("protect.spread", true);
            plugin.getConfig().set("protect.fromto", true);
            plugin.getConfig().set("protect.minecart", null);
            plugin.getConfig().set("protect.hopper", null);
            plugin.getConfig().set("config-version", 39);
            selectedVersion = 39;
        }
        if (selectedVersion == 39) {
            plugin.getConfig().set("update-sign-when-inventory-moving", true);
            plugin.getConfig().set("config-version", 40);
            selectedVersion = 40;
        }
        if (selectedVersion == 40) {
            plugin.getConfig().set("allow-economy-loan", false);
            plugin.getConfig().set("config-version", 41);
            selectedVersion = 41;
        }
        if (selectedVersion == 41) {
            plugin.getConfig().set("send-display-item-protection-alert", true);
            plugin.getConfig().set("config-version", 42);
            selectedVersion = 42;
        }
        if (selectedVersion == 42) {
            plugin.getConfig().set("config-version", 43);
            selectedVersion = 43;
        }
        if (selectedVersion == 43) {
            plugin.getConfig().set("config-version", 44);
            selectedVersion = 44;
        }
        if (selectedVersion == 44) {
            plugin.getConfig().set("matcher.item.repaircost", false);
            plugin.getConfig().set("config-version", 45);
            selectedVersion = 45;
        }
        if (selectedVersion == 45) {
            plugin.getConfig().set("shop.display-item-use-name", true);
            plugin.getConfig().set("config-version", 46);
            selectedVersion = 46;
        }
        if (selectedVersion == 46) {
            plugin.getConfig().set("shop.max-shops-checks-in-once", 100);
            plugin.getConfig().set("config-version", 47);
            selectedVersion = 47;
        }
        if (selectedVersion == 47) {
            plugin.getConfig().set("config-version", 48);
            selectedVersion = 48;
        }
        if (selectedVersion == 48) {
            plugin.getConfig().set("permission-type", null);
            plugin.getConfig().set("shop.use-protection-checking-filter", null);
            plugin.getConfig().set("shop.protection-checking-filter", null);
            plugin.getConfig().set("config-version", 49);
            selectedVersion = 49;
        }
        if (selectedVersion == 49 || selectedVersion == 50) {
            plugin.getConfig().set("shop.enchance-display-protect", false);
            plugin.getConfig().set("shop.enchance-shop-protect", false);
            plugin.getConfig().set("protect", null);
            plugin.getConfig().set("config-version", 51);
            selectedVersion = 51;
        }
        if (selectedVersion < 60) { // Ahhh fuck versions
            plugin.getConfig().set("config-version", 60);
            selectedVersion = 60;
        }
        if (selectedVersion == 60) { // Ahhh fuck versions
            plugin.getConfig().set("shop.strict-matches-check", null);
            plugin.getConfig().set("shop.display-auto-despawn", true);
            plugin.getConfig().set("shop.display-despawn-range", 10);
            plugin.getConfig().set("shop.display-check-time", 10);
            plugin.getConfig().set("config-version", 61);
            selectedVersion = 61;
        }
        if (selectedVersion == 61) { // Ahhh fuck versions
            plugin.getConfig().set("shop.word-for-sell-all-items", "all");
            plugin.getConfig().set("plugin.PlaceHolderAPI", true);
            plugin.getConfig().set("config-version", 62);
            selectedVersion = 62;
        }
        if (selectedVersion == 62) { // Ahhh fuck versions
            plugin.getConfig().set("shop.display-auto-despawn", false);
            plugin.getConfig().set("shop.word-for-trade-all-items", plugin.getConfig().getString("shop.word-for-sell-all-items"));

            plugin.getConfig().set("config-version", 63);
            selectedVersion = 63;
        }
        if (selectedVersion == 63) { // Ahhh fuck versions
            plugin.getConfig().set("shop.ongoing-fee.enable", false);
            plugin.getConfig().set("shop.ongoing-fee.ticks", 42000);
            plugin.getConfig().set("shop.ongoing-fee.cost-per-shop", 2);
            plugin.getConfig().set("shop.ongoing-fee.ignore-unlimited", true);
            plugin.getConfig().set("config-version", 64);
            selectedVersion = 64;
        }
        if (selectedVersion == 64) {
            plugin.getConfig().set("shop.allow-free-shop", false);
            plugin.getConfig().set("config-version", 65);
            selectedVersion = 65;
        }
        if (selectedVersion == 65) {
            plugin.getConfig().set("shop.minimum-price", 0.01);
            plugin.getConfig().set("config-version", 66);
            selectedVersion = 66;
        }
        if (selectedVersion == 66) {
            plugin.getConfig().set("use-decimal-format", false);
            plugin.getConfig().set("decimal-format", "#,###.##");
            plugin.getConfig().set("shop.show-owner-uuid-in-controlpanel-if-op", false);
            plugin.getConfig().set("config-version", 67);
            selectedVersion = 67;
        }
        if (selectedVersion == 67) {
            plugin.getConfig().set("disable-debuglogger", false);
            plugin.getConfig().set("matcher.use-bukkit-matcher", null);
            plugin.getConfig().set("config-version", 68);
            selectedVersion = 68;
        }
        if (selectedVersion == 68) {
            plugin.getConfig().set("shop.blacklist-lores", Lists.newArrayList("SoulBound"));
            plugin.getConfig().set("config-version", 69);
            selectedVersion = 69;
        }
        if (selectedVersion == 69) {
            plugin.getConfig().set("shop.display-item-use-name", false);
            plugin.getConfig().set("config-version", 70);
            selectedVersion = 70;
        }
        if (selectedVersion == 70) {
            plugin.getConfig().set("cachingpool.enable", false);
            plugin.getConfig().set("cachingpool.maxsize", 100000000);
            plugin.getConfig().set("config-version", 71);
            selectedVersion = 71;
        }
        if (selectedVersion == 71) {
            if (Objects.equals(plugin.getConfig().getString("language"), "en")) {
                plugin.getConfig().set("language", "en-US");
            }
            plugin.getConfig().set("server-platform", 0);
            plugin.getConfig().set("config-version", 72);
            selectedVersion = 72;
        }
        if (selectedVersion == 72) {
            if (plugin.getConfig().getBoolean("use-deciaml-format")) {
                plugin.getConfig().set("use-decimal-format", plugin.getConfig().getBoolean("use-deciaml-format"));
            } else {
                plugin.getConfig().set("use-decimal-format", false);
            }
            plugin.getConfig().set("use-deciaml-format", null);

            plugin.getConfig().set("shop.force-load-downgrade-items.enable", false);
            plugin.getConfig().set("shop.force-load-downgrade-items.method", 0);
            plugin.getConfig().set("config-version", 73);
            selectedVersion = 73;
        }
        if (selectedVersion == 73) {
            plugin.getConfig().set("mixedeconomy.deposit", "eco give {0} {1}");
            plugin.getConfig().set("mixedeconomy.withdraw", "eco take {0} {1}");
            plugin.getConfig().set("config-version", 74);
            selectedVersion = 74;
        }
        if (selectedVersion == 74) {
            String langUtilsLanguage = plugin.getConfig().getString("langutils-language", "en_us");
            plugin.getConfig().set("langutils-language", null);
            if ("en_us".equals(langUtilsLanguage)) {
                langUtilsLanguage = "default";
            }
            plugin.getConfig().set("game-language", langUtilsLanguage);
            plugin.getConfig().set("maximum-digits-in-price", -1);
            plugin.getConfig().set("config-version", 75);
            selectedVersion = 75;
        }
        if (selectedVersion == 75) {
            plugin.getConfig().set("langutils-language", null);
            if (plugin.getConfig().get("game-language") == null) {
                plugin.getConfig().set("game-language", "default");
            }
            plugin.getConfig().set("config-version", 76);
            selectedVersion = 76;
        }
        if (selectedVersion == 76) {
            plugin.getConfig().set("database.auto-fix-encoding-issue-in-database", false);
            plugin.getConfig().set("send-shop-protection-alert", false);
            plugin.getConfig().set("send-display-item-protection-alert", false);
            plugin.getConfig().set("shop.use-fast-shop-search-algorithm", false);
            plugin.getConfig().set("config-version", 77);
            selectedVersion = 77;
        }
        if (selectedVersion == 77) {
            plugin.getConfig().set("integration.towny.enable", false);
            plugin.getConfig().set("integration.towny.create", new String[]{"SHOPTYPE", "MODIFY"});
            plugin.getConfig().set("integration.towny.trade", new String[]{});
            plugin.getConfig().set("integration.worldguard.enable", false);
            plugin.getConfig().set("integration.worldguard.create", new String[]{"FLAG", "CHEST_ACCESS"});
            plugin.getConfig().set("integration.worldguard.trade", new String[]{});
            plugin.getConfig().set("integration.plotsquared.enable", false);
            plugin.getConfig().set("integration.plotsquared.enable", false);
            plugin.getConfig().set("integration.plotsquared.enable", false);
            plugin.getConfig().set("integration.residence.enable", false);
            plugin.getConfig().set("integration.residence.create", new String[]{"FLAG", "interact", "use"});
            plugin.getConfig().set("integration.residence.trade", new String[]{});

            plugin.getConfig().set("integration.factions.enable", false);
            plugin.getConfig().set("integration.factions.create.flag", new String[]{});
            plugin.getConfig().set("integration.factions.trade.flag", new String[]{});
            plugin.getConfig().set("integration.factions.create.require.open", false);
            plugin.getConfig().set("integration.factions.create.require.normal", true);
            plugin.getConfig().set("integration.factions.create.require.wilderness", false);
            plugin.getConfig().set("integration.factions.create.require.peaceful", true);
            plugin.getConfig().set("integration.factions.create.require.permanent", false);
            plugin.getConfig().set("integration.factions.create.require.safezone", false);
            plugin.getConfig().set("integration.factions.create.require.own", false);
            plugin.getConfig().set("integration.factions.create.require.warzone", false);
            plugin.getConfig().set("integration.factions.trade.require.open", false);
            plugin.getConfig().set("integration.factions.trade.require.normal", true);
            plugin.getConfig().set("integration.factions.trade.require.wilderness", false);
            plugin.getConfig().set("integration.factions.trade.require.peaceful", false);
            plugin.getConfig().set("integration.factions.trade.require.permanent", false);
            plugin.getConfig().set("integration.factions.trade.require.safezone", false);
            plugin.getConfig().set("integration.factions.trade.require.own", false);
            plugin.getConfig().set("integration.factions.trade.require.warzone", false);
            plugin.getConfig().set("anonymous-metrics", null);
            plugin.getConfig().set("shop.ongoing-fee.async", true);
            plugin.getConfig().set("config-version", 78);
            selectedVersion = 78;
        }
        if (selectedVersion == 78) {
            plugin.getConfig().set("shop.display-type-specifics", null);
            plugin.getConfig().set("config-version", 79);
            selectedVersion = 79;
        }
        if (selectedVersion == 79) {
            plugin.getConfig().set("matcher.item.books", true);
            plugin.getConfig().set("config-version", 80);
            selectedVersion = 80;
        }
        if (selectedVersion == 80) {
            plugin.getConfig().set("shop.use-fast-shop-search-algorithm", true);
            plugin.getConfig().set("config-version", 81);
            selectedVersion = 81;
        }
        if (selectedVersion == 81) {
            plugin.getConfig().set("config-version", 82);
            selectedVersion = 82;
        }
        if (selectedVersion == 82) {
            plugin.getConfig().set("matcher.item.banner", true);
            plugin.getConfig().set("config-version", 83);
            selectedVersion = 83;
        }
        if (selectedVersion == 83) {
            plugin.getConfig().set("matcher.item.banner", true);
            plugin.getConfig().set("protect.explode", true);
            plugin.getConfig().set("config-version", 84);
            selectedVersion = 84;
        }
        if (selectedVersion == 84) {
            plugin.getConfig().set("disable-debuglogger", null);
            plugin.getConfig().set("config-version", 85);
            selectedVersion = 85;
        }
        if (selectedVersion == 85) {
            plugin.getConfig().set("config-version", 86);
            selectedVersion = 86;
        }
        if (selectedVersion == 86) {
            plugin.getConfig().set("shop.use-fast-shop-search-algorithm", true);
            plugin.getConfig().set("config-version", 87);
            selectedVersion = 87;
        }
        if (selectedVersion == 87) {
            plugin.getConfig().set("plugin.BlockHub.enable", true);
            plugin.getConfig().set("plugin.BlockHub.only", false);
            if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
                plugin.getConfig().set("shop.display-type", 2);
            }
            plugin.getConfig().set("config-version", 88);
            selectedVersion = 88;
        }
        if (selectedVersion == 88) {
            plugin.getConfig().set("respect-item-flag", true);
            plugin.getConfig().set("config-version", 89);
            selectedVersion = 89;
        }
        if (selectedVersion == 89) {
            plugin.getConfig().set("use-caching", true);
            plugin.getConfig().set("config-version", 90);
            selectedVersion = 90;
        }
        if (selectedVersion == 90) {
            plugin.getConfig().set("protect.hopper", true);
            plugin.getConfig().set("config-version", 91);
            selectedVersion = 91;
        }
        if (selectedVersion == 91) {
            plugin.getConfig().set("database.queue-commit-interval", 2);
            plugin.getConfig().set("config-version", 92);
            selectedVersion = 92;
        }
        if (selectedVersion == 92) {
            plugin.getConfig().set("send-display-item-protection-alert", false);
            plugin.getConfig().set("send-shop-protection-alert", false);
            plugin.getConfig().set("disable-creative-mode-trading", false);
            plugin.getConfig().set("disable-super-tool", false);
            plugin.getConfig().set("allow-owner-break-shop-sign", false);
            plugin.getConfig().set("matcher.item.skull", true);
            plugin.getConfig().set("matcher.item.firework", true);
            plugin.getConfig().set("matcher.item.map", true);
            plugin.getConfig().set("matcher.item.leatherArmor", true);
            plugin.getConfig().set("matcher.item.fishBucket", true);
            plugin.getConfig().set("matcher.item.suspiciousStew", true);
            plugin.getConfig().set("matcher.item.shulkerBox", true);
            plugin.getConfig().set("config-version", 93);
            selectedVersion = 93;
        }
        if (selectedVersion == 93) {
            plugin.getConfig().set("disable-creative-mode-trading", null);
            plugin.getConfig().set("disable-super-tool", null);
            plugin.getConfig().set("allow-owner-break-shop-sign", null);
            plugin.getConfig().set("shop.disable-creative-mode-trading", true);
            plugin.getConfig().set("shop.disable-super-tool", true);
            plugin.getConfig().set("shop.allow-owner-break-shop-sign", false);
            plugin.getConfig().set("config-version", 94);
            selectedVersion = 94;
        }
        if (selectedVersion == 94) {
            if (plugin.getConfig().get("price-restriction") != null) {
                plugin.getConfig().set("shop.price-restriction", plugin.getConfig().getStringList("price-restriction"));
                plugin.getConfig().set("price-restriction", null);
            } else {
                plugin.getConfig().set("shop.price-restriction", new ArrayList<>(0));
            }
            plugin.getConfig().set("enable-log4j", null);
            plugin.getConfig().set("config-version", 95);
            selectedVersion = 95;
        }
        if (selectedVersion == 95) {
            plugin.getConfig().set("shop.allow-stacks", false);
            plugin.getConfig().set("shop.display-allow-stacks", false);
            plugin.getConfig().set("custom-item-stacksize", new ArrayList<>(0));
            plugin.getConfig().set("config-version", 96);
            selectedVersion = 96;
        }
        if (selectedVersion == 96) {
            plugin.getConfig().set("shop.deny-non-shop-items-to-shop-container", false);
            plugin.getConfig().set("config-version", 97);
            selectedVersion = 97;
        }
        if (selectedVersion == 97) {
            plugin.getConfig().set("shop.disable-quick-create", false);
            plugin.getConfig().set("config-version", 98);
            selectedVersion = 98;
        }
        if (selectedVersion == 98) {
            plugin.getConfig().set("config-version", 99);
            selectedVersion = 99;
        }
        if (selectedVersion == 99) {
            plugin.getConfig().set("shop.currency-symbol-on-right", false);
            plugin.getConfig().set("config-version", 100);
            selectedVersion = 100;
        }
        if (selectedVersion == 100) {
            plugin.getConfig().set("integration.towny.ignore-disabled-worlds", false);
            plugin.getConfig().set("config-version", 101);
            selectedVersion = 101;
        }
        if (selectedVersion == 101) {
            plugin.getConfig().set("matcher.work-type", 1);
            plugin.getConfig().set("work-type", null);
            plugin.getConfig().set("plugin.LWC", true);
            plugin.getConfig().set("config-version", 102);
            selectedVersion = 102;
        }
        if (selectedVersion == 102) {
            plugin.getConfig().set("protect.entity", true);
            plugin.getConfig().set("config-version", 103);
            selectedVersion = 103;
        }
        if (selectedVersion == 103) {
            plugin.getConfig().set("integration.worldguard.whitelist-mode", false);
            plugin.getConfig().set("integration.factions.whitelist-mode", true);
            plugin.getConfig().set("integration.plotsquared.whitelist-mode", true);
            plugin.getConfig().set("integration.residence.whitelist-mode", true);
            plugin.getConfig().set("config-version", 104);
            selectedVersion = 104;
        }
        if (selectedVersion == 104) {
            plugin.getConfig().set("cachingpool", null);
            plugin.getConfig().set("config-version", 105);
            selectedVersion = 105;
        }
        if (selectedVersion == 105) {
            plugin.getConfig().set("shop.interact.sneak-to-create", plugin.getConfig().getBoolean("shop.sneak-to-create"));
            plugin.getConfig().set("shop.sneak-to-create", null);
            plugin.getConfig().set("shop.interact.sneak-to-trade", plugin.getConfig().getBoolean("shop.sneak-to-trade"));
            plugin.getConfig().set("shop.sneak-to-trade", null);
            plugin.getConfig().set("shop.interact.sneak-to-control", plugin.getConfig().getBoolean("shop.sneak-to-control"));
            plugin.getConfig().set("shop.sneak-to-control", null);
            plugin.getConfig().set("config-version", 106);
            selectedVersion = 106;
        }
        if (selectedVersion == 106) {
            plugin.getConfig().set("shop.use-enchantment-for-enchanted-book", false);
            plugin.getConfig().set("config-version", 107);
            selectedVersion = 107;
        }
        if (selectedVersion == 107) {
            plugin.getConfig().set("integration.lands.enable", false);
            plugin.getConfig().set("integration.lands.whitelist-mode", false);
            plugin.getConfig().set("integration.lands.ignore-disabled-worlds", true);
            plugin.getConfig().set("config-version", 108);
            selectedVersion = 108;
        }
        if (selectedVersion == 108) {
            plugin.getConfig().set("debug.shop-deletion", false);
            plugin.getConfig().set("config-version", 109);
            selectedVersion = 109;
        }
        if (selectedVersion == 109) {
            plugin.getConfig().set("shop.protection-checking-blacklist", Collections.singletonList("disabled_world"));
            plugin.getConfig().set("config-version", 110);
            selectedVersion = 110;
        }
        if (selectedVersion == 110) {
            plugin.getConfig().set("integration.worldguard.any-owner", true);
            plugin.getConfig().set("config-version", 111);
            selectedVersion = 111;
        }
        if (selectedVersion == 111) {
            plugin.getConfig().set("logging.enable", plugin.getConfig().getBoolean("log-actions"));
            plugin.getConfig().set("logging.log-actions", plugin.getConfig().getBoolean("log-actions"));
            plugin.getConfig().set("logging.log-balance", true);
            plugin.getConfig().set("logging.file-size", 10);
            plugin.getConfig().set("debug.disable-debuglogger", false);
            plugin.getConfig().set("trying-fix-banlance-insuffient", false);
            plugin.getConfig().set("log-actions", null);
            plugin.getConfig().set("config-version", 112);
            selectedVersion = 112;
        }
        if (selectedVersion == 112) {
            plugin.getConfig().set("integration.lands.delete-on-lose-permission", false);
            plugin.getConfig().set("config-version", 113);
            selectedVersion = 113;
        }
        if (selectedVersion == 113) {
            plugin.getConfig().set("config-damaged", false);
            plugin.getConfig().set("config-version", 114);
            selectedVersion = 114;
        }
        if (selectedVersion == 114) {
            plugin.getConfig().set("shop.interact.interact-mode", plugin.getConfig().getBoolean("shop.interact.switch-mode") ? 0 : 1);
            plugin.getConfig().set("shop.interact.switch-mode", null);
            plugin.getConfig().set("config-version", 115);
            selectedVersion = 115;
        }
        if (selectedVersion == 115) {
            plugin.getConfig().set("integration.griefprevention.enable", false);
            plugin.getConfig().set("integration.griefprevention.whitelist-mode", false);
            plugin.getConfig().set("integration.griefprevention.create", Collections.emptyList());
            plugin.getConfig().set("integration.griefprevention.trade", Collections.emptyList());
            plugin.getConfig().set("config-version", 116);
            selectedVersion = 116;
        }
        if (selectedVersion == 116) {
            plugin.getConfig().set("shop.sending-stock-message-to-staffs", false);
            plugin.getConfig().set("integration.towny.delete-shop-on-resident-leave", false);
            plugin.getConfig().set("config-version", 117);
            selectedVersion = 117;
        }
        if (selectedVersion == 117) {
            plugin.getConfig().set("shop.finding.distance", plugin.getConfig().getInt("shop.find-distance"));
            plugin.getConfig().set("shop.finding.limit", 10);
            plugin.getConfig().set("shop.find-distance", null);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 118) {
            plugin.getConfig().set("shop.finding.oldLogic", false);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 119) {
            plugin.getConfig().set("debug.adventure", false);
            plugin.getConfig().set("shop.finding.all", false);
            plugin.getConfig().set("chat-type", 0);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 120) {
            plugin.getConfig().set("shop.finding.exclude-out-of-stock", false);
            plugin.getConfig().set("chat-type", 0);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 121) {
            plugin.getConfig().set("shop.protection-checking-handler", 0);
            plugin.getConfig().set("shop.protection-checking-listener-blacklist", Collections.singletonList("ignored_listener"));
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 122) {
            plugin.getConfig().set("currency", "");
            plugin.getConfig().set("shop.alternate-currency-symbol-list", Arrays.asList("CNY;¥", "USD;$"));
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 123) {
            plugin.getConfig().set("integration.fabledskyblock.enable", false);
            plugin.getConfig().set("integration.fabledskyblock.whitelist-mode", false);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 124) {
            plugin.getConfig().set("plugin.BKCommonLib", true);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 125) {
            plugin.getConfig().set("integration.superiorskyblock.enable", false);
            plugin.getConfig().set("integration.superiorskyblock.owner-create-only", false);
            plugin.getConfig().set("integration.superiorskyblock.delete-shop-on-member-leave", true);
            plugin.getConfig().set("shop.interact.swap-click-behavior", false);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 126) {
            plugin.getConfig().set("debug.delete-corrupt-shops", false);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }

        if (selectedVersion == 127) {
            plugin.getConfig().set("integration.plotsquared.delete-when-user-untrusted", true);
            plugin.getConfig().set("integration.towny.delete-shop-on-plot-clear", true);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 128) {
            plugin.getConfig().set("shop.force-use-item-original-name", false);
            plugin.getConfig().set("integration.griefprevention.delete-on-untrusted", false);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }

        if (selectedVersion == 129) {
            plugin.getConfig().set("shop.use-global-virtual-item-queue", false);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }

        if (selectedVersion == 130) {
            plugin.getConfig().set("plugin.WorldEdit", true);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 131) {
            plugin.getConfig().set("custom-commands", ImmutableList.of("shop", "chestshop", "cshop"));
            plugin.getConfig().set("unlimited-shop-owner-change", false);
            plugin.getConfig().set("unlimited-shop-owner-change-account", "quickshop");
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 132) {
            plugin.getConfig().set("shop.sign-glowing", false);
            plugin.getConfig().set("shop.sign-dye-color", "null");
            plugin.getConfig().set("unlimited-shop-owner-change-account", "quickshop");
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 133) {
            plugin.getConfig().set("integration.griefprevention.delete-on-unclaim", false);
            plugin.getConfig().set("integration.griefprevention.delete-on-claim-expired", false);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 134) {
            plugin.getConfig().set("integration.griefprevention.delete-on-claim-resized", false);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 135) {
            plugin.getConfig().set("integration.advancedregionmarket.enable", true);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 136) {
            plugin.getConfig().set("shop.use-global-virtual-item-queue", null);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 137) {
            plugin.getConfig().set("integration.griefprevention.create", null);
            plugin.getConfig().set("integration.griefprevention.create", "INVENTORY");

            plugin.getConfig().set("integration.griefprevention.trade", null);
            plugin.getConfig().set("integration.griefprevention.trade", Collections.emptyList());

            boolean oldValueUntrusted = plugin.getConfig().getBoolean("integration.griefprevention.delete-on-untrusted", false);
            plugin.getConfig().set("integration.griefprevention.delete-on-untrusted", null);
            plugin.getConfig().set("integration.griefprevention.delete-on-claim-trust-changed", oldValueUntrusted);

            boolean oldValueUnclaim = plugin.getConfig().getBoolean("integration.griefprevention.delete-on-unclaim", false);
            plugin.getConfig().set("integration.griefprevention.delete-on-unclaim", null);
            plugin.getConfig().set("integration.griefprevention.delete-on-claim-unclaimed", oldValueUnclaim);

            plugin.getConfig().set("integration.griefprevention.delete-on-subclaim-created", false);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 138) {
            plugin.getConfig().set("integration.towny.whitelist-mode", true);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }

        if (selectedVersion == 139) {
            plugin.getConfig().set("integration.iridiumskyblock.enable", false);
            plugin.getConfig().set("integration.iridiumskyblock.owner-create-only", false);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 140) {
            plugin.getConfig().set("integration.towny.delete-shop-on-plot-destroy", true);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 141) {
            plugin.getConfig().set("disabled-languages", Collections.singletonList("disable_here"));
            plugin.getConfig().set("mojangapi-mirror", 0);
            plugin.getConfig().set("purge.enabled", false);
            plugin.getConfig().set("purge.days", 60);
            plugin.getConfig().set("purge.banned", true);
            plugin.getConfig().set("purge.skip-op", true);
            plugin.getConfig().set("purge.return-create-fee", true);
            plugin.getConfig().set("shop.use-fast-shop-search-algorithm", null);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 142) {
            plugin.getConfig().set("disabled-languages", null);
            plugin.getConfig().set("enabled-languages", Collections.singletonList("*"));
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 143) {
//            if (plugin.getConfig().get("language") == null) {
//                plugin.getConfig().set("language", "en-US");
//            }
            plugin.getConfig().set("config-version", ++selectedVersion);
        }

        if (selectedVersion == 144) {
            // Updater set it to true because plugin upgrading
            // Default configuration disable it cause probably fresh install
            plugin.getConfig().set("legacy-updater.shop-sign", true);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 145) {
            // Updater set it to true because plugin upgrading
            // Default configuration disable it cause probably fresh install
            plugin.getConfig().set("logger.location", 0);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 146) {
            // Updater set it to true because plugin upgrading
            // Default configuration disable it cause probably fresh install
            plugin.getConfig().set("language", null);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 147) {
            // Updater set it to true because plugin upgrading
            // Default configuration disable it cause probably fresh install
            plugin.getConfig().set("plugin.BKCommonLib", null);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 148) {
            plugin.getConfig().set("integration.worldguard.respect-global-region", false);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 149) {
            //Fix sign-dye-color setting is missing
            if (!plugin.getConfig().isSet("shop.sign-dye-color")) {
                plugin.getConfig().set("shop.sign-dye-color", "");
            }
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 150) {
            plugin.getConfig().set("purge.at-server-startup", true);
            plugin.getConfig().set("purge.backup", true);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 151) {
            plugin.getConfig().set("shop.protection-checking-handler", null);
            plugin.getConfig().set("config-version", ++selectedVersion);

        }
        if (selectedVersion == 152) {
            plugin.getConfig().set("shop.shop.use-effect-for-potion-item", false);
            plugin.getConfig().set("config-version", ++selectedVersion);

        }
        if (selectedVersion == 153) {
            plugin.getConfig().set("shop.use-effect-for-potion-item", plugin.getConfig().getBoolean("shop.shop.use-effect-for-potion-item", false));
            plugin.getConfig().set("shop.shop.use-effect-for-potion-item", null);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 154) {
            plugin.getConfig().set("integration.fabledskyblock.create", Arrays.asList("MEMBER", "OWNER", "OPERATOR"));
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        if (selectedVersion == 155) {
            plugin.getConfig().set("shop.cost-goto-tax-account", false);
            plugin.getConfig().set("config-version", ++selectedVersion);
        }
        // shop.interact has been removed in Apollo
        // require migrate code while merge into master.
        if (plugin.getConfig().isSet("shop.shop")) {
            plugin.getConfig().set("shop.shop", null);
        }
        plugin.saveConfiguration();
        plugin.getLogger().info("[ApolloConverter] Legacy upgrade script executed.");
    }

    @Builder
    @AllArgsConstructor
    @Getter
    static class ShopStorageUnit {
        private final String owner;
        private final double price;
        private final String itemConfig;
        private final int x;
        private final int y;
        private final int z;
        private final String world;
        private final int unlimited;
        private final int type;
        private final String extra;
        private final String currency;
        private final int disableDisplay;
        private final String taxAccount;

        @NotNull
        public String getInventoryWrapperName() {
            return QuickShop.getInstance().getDescription().getName();
        }

        @NotNull
        public String getInventorySymbolLink() {
            String holder = JsonUtil.standard().toJson(new BukkitInventoryWrapperManager.BlockHolder(world, x, y, z));
            return JsonUtil.standard().toJson(new BukkitInventoryWrapperManager.CommonHolder(BukkitInventoryWrapperManager.HolderType.BLOCK, holder));
        }
    }
}
