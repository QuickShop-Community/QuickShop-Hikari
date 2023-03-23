package com.ghostchu.quickshop.converter;

import cc.carm.lib.easysql.EasySQL;
import cc.carm.lib.easysql.api.SQLManager;
import cc.carm.lib.easysql.hikari.HikariConfig;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.common.util.JsonUtil;
import com.ghostchu.quickshop.database.HikariUtil;
import com.ghostchu.quickshop.database.SimpleDatabaseHelperV1;
import com.ghostchu.quickshop.database.SimpleDatabaseHelperV2;
import com.ghostchu.quickshop.shop.inventory.BukkitInventoryWrapperManager;
import com.ghostchu.quickshop.util.logger.Log;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.h2.Driver;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.ConnectException;
import java.nio.file.Files;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HikariDatabaseConverter implements HikariConverterInterface {

    private final HikariConverter instance;
    private final QuickShop plugin;
    private SQLManager liveDatabase = null;

    public HikariDatabaseConverter(@NotNull HikariConverter instance) {
        this.instance = instance;
        this.plugin = instance.getPlugin();
        /* Engage HikariCP logging */
        Logger.getLogger("cc.carm.lib.easysql.hikari.pool.PoolBase").setLevel(Level.OFF);
        Logger.getLogger("cc.carm.lib.easysql.hikari.pool.HikariPool").setLevel(Level.OFF);
        Logger.getLogger("cc.carm.lib.easysql.hikari.HikariDataSource").setLevel(Level.OFF);
        Logger.getLogger("cc.carm.lib.easysql.hikari.HikariConfig").setLevel(Level.OFF);
        Logger.getLogger("cc.carm.lib.easysql.hikari.util.DriverDataSource").setLevel(Level.OFF);
    }

    /**
     * Start for backing up
     *
     * @param actionId Action Identifier for this upgrade operation.
     * @param folder   The target folder for backup.
     * @throws Exception Backup fails.
     */
    @Override
    public void backup(@NotNull UUID actionId, @NotNull File folder) throws Exception {
        DatabaseConfig config = getDatabaseConfig();
        if (config.isMysql()) {
            instance.getLogger().warning("ApolloConverter doesn't support for MySQL backup, do it by your self with `mysqldump`!");
        } else {
            Files.copy(new File(plugin.getDataFolder(), "shops.db").toPath(), new File(folder, "shops.db").toPath());
        }
    }

    /**
     * Returns empty for ready, any elements inside will mark as not ready and will be post to users.
     *
     * @return The element about not ready.
     * @throws Exception Any exception throws will mark as unready and will show to users.
     */
    @Override
    public @NotNull List<Component> checkReady() throws Exception {
        DatabaseConfig config = getDatabaseConfig();
        List<Component> entries = new ArrayList<>();
        // Initialze drivers
        Driver.load();
        Class.forName("org.sqlite.JDBC");
        try (Connection liveDatabaseConnection = getLiveDatabase().getConnection()) {
            if (!config.isMysql()) {
                try (Connection sqliteDatabase = getSQLiteDatabase()) {
                    if (hasTable(config.getPrefix() + "shops", liveDatabaseConnection)) {
                        throw new IllegalStateException("The target database has exists shops data!");
                    }
                    if (hasTable(config.getPrefix() + "messages", liveDatabaseConnection)) {
                        throw new IllegalStateException("The target database has exists messages data!");
                    }
                    if (hasTable(config.getPrefix() + "external_cache", liveDatabaseConnection)) {
                        throw new IllegalStateException("The target database has external_data data!");
                    }
                    if (!hasTable(config.getPrefix() + "shops", sqliteDatabase)) {
                        throw new IllegalStateException("The sources database had no exists shops data! shops.db file data missing!");
                    }
                    if (!hasTable(config.getPrefix() + "messages", sqliteDatabase)) {
                        throw new IllegalStateException("The sources database had no exists messages data! shops.db file data missing!");
                    }
                    if (!hasTable(config.getPrefix() + "external_cache", sqliteDatabase)) {
                        throw new IllegalStateException("The sources database had no external_data data! shops.db file data missing!");
                    }
                    if (!hasColumn(config.getPrefix() + "shops", "owner", sqliteDatabase)) {
                        entries.add(Component.text("The sources database has not exists owner column!"));
                    }
                    if (!hasColumn(config.getPrefix() + "shops", "price", sqliteDatabase)) {
                        throw new IllegalStateException("The sources database has not exists price column!");
                    }
                    if (!hasColumn(config.getPrefix() + "shops", "itemConfig", sqliteDatabase)) {
                        entries.add(Component.text("The sources database has not exists itemConfig column!"));
                    }
                    if (!hasColumn(config.getPrefix() + "shops", "x", sqliteDatabase)) {
                        entries.add(Component.text("The sources database has not exists x column!"));
                    }
                    if (!hasColumn(config.getPrefix() + "shops", "y", sqliteDatabase)) {
                        entries.add(Component.text("The sources database has not exists y column!"));
                    }
                    if (!hasColumn(config.getPrefix() + "shops", "z", sqliteDatabase)) {
                        entries.add(Component.text("The sources database has not exists z column!"));
                    }
                    if (!hasColumn(config.getPrefix() + "shops", "world", sqliteDatabase)) {
                        entries.add(Component.text("The sources database has not exists world column!"));
                    }
                    if (!hasColumn(config.getPrefix() + "shops", "unlimited", sqliteDatabase)) {
                        entries.add(Component.text("The sources database has not exists unlimited column!"));
                    }
                    if (!hasColumn(config.getPrefix() + "shops", "type", sqliteDatabase)) {
                        entries.add(Component.text("The sources database has not exists type column!"));
                    }
                    if (!hasColumn(config.getPrefix() + "shops", "extra", sqliteDatabase)) {
                        entries.add(Component.text("The sources database has not exists extra column!"));
                    }
                    if (!hasColumn(config.getPrefix() + "shops", "disableDisplay", sqliteDatabase)) {
                        entries.add(Component.text("The sources database has not exists disableDisplay column!"));
                    }
                    if (!hasColumn(config.getPrefix() + "shops", "taxAccount", sqliteDatabase)) {
                        entries.add(Component.text("The sources database has not exists taxAccount column!"));
                    }
                }
            } else {
                // mysql
                if (!hasColumn(config.getPrefix() + "shops", "owner", liveDatabaseConnection)) {
                    entries.add(Component.text("The sources database has not exists owner column!"));
                }
                if (!hasColumn(config.getPrefix() + "shops", "price", liveDatabaseConnection)) {
                    throw new IllegalStateException("The sources database has not exists price column!");
                }
                if (!hasColumn(config.getPrefix() + "shops", "itemConfig", liveDatabaseConnection)) {
                    entries.add(Component.text("The sources database has not exists itemConfig column!"));
                }
                if (!hasColumn(config.getPrefix() + "shops", "x", liveDatabaseConnection)) {
                    entries.add(Component.text("The sources database has not exists x column!"));
                }
                if (!hasColumn(config.getPrefix() + "shops", "y", liveDatabaseConnection)) {
                    entries.add(Component.text("The sources database has not exists y column!"));
                }
                if (!hasColumn(config.getPrefix() + "shops", "z", liveDatabaseConnection)) {
                    entries.add(Component.text("The sources database has not exists z column!"));
                }
                if (!hasColumn(config.getPrefix() + "shops", "world", liveDatabaseConnection)) {
                    entries.add(Component.text("The sources database has not exists world column!"));
                }
                if (!hasColumn(config.getPrefix() + "shops", "unlimited", liveDatabaseConnection)) {
                    entries.add(Component.text("The sources database has not exists unlimited column!"));
                }
                if (!hasColumn(config.getPrefix() + "shops", "type", liveDatabaseConnection)) {
                    entries.add(Component.text("The sources database has not exists type column!"));
                }
                if (!hasColumn(config.getPrefix() + "shops", "extra", liveDatabaseConnection)) {
                    entries.add(Component.text("The sources database has not exists extra column!"));
                }
                if (!hasColumn(config.getPrefix() + "shops", "disableDisplay", liveDatabaseConnection)) {
                    entries.add(Component.text("The sources database has not exists disableDisplay column!"));
                }
                if (!hasColumn(config.getPrefix() + "shops", "taxAccount", liveDatabaseConnection)) {
                    entries.add(Component.text("The sources database has not exists taxAccount column!"));
                }
            }
        }

        return entries;
    }

    /**
     * Start the migrating
     *
     * @param actionId Action Identifier for this upgrade operation.
     * @throws IllegalStateException Not ready.
     * @throws Exception             Migrate operation fails.
     */
    @Override
    public void migrate(@NotNull UUID actionId) throws Exception {
        if (!checkReady().isEmpty()) {
            throw new IllegalStateException("Not ready!");
        }
        DatabaseConfig config = getDatabaseConfig();
        instance.getLogger().info("Renaming tables...");
        String shopsTmpTable = renameTables(actionId, config);
        instance.getLogger().info("Offline Messages and External Caches won't be migrated because they are have totally different syntax and cache need regenerate after migrated.");
        instance.getLogger().info("Downloading old data from database connection...");
        List<ShopStorageUnit> units;
        SQLManager liveDatabaseManager = getLiveDatabase();
        try (Connection liveDatabaseConnection = liveDatabaseManager.getConnection()) {
            if (config.isMysql()) {
                units = pullShops(shopsTmpTable, liveDatabaseConnection);
            } else {
                try (Connection sqliteDatabase = getSQLiteDatabase()) {
                    units = pullShops(shopsTmpTable, sqliteDatabase);
                }
            }
            instance.getLogger().info("Checking and creating for database tables... ");
            // Database Helper will resolve all we need while starting up.
            //noinspection deprecation
            new SimpleDatabaseHelperV1(plugin, liveDatabaseManager, config.getPrefix());
            instance.getLogger().info("Migrating old data to new database...");
            pushShops(units, config.getPrefix(), liveDatabaseManager);
            new SimpleDatabaseHelperV2(plugin, liveDatabaseManager, config.getPrefix());
        }
        instance.getLogger().info("Database migration completed!");
    }

    /**
     * Getting the live database configuration.
     *
     * @return The live database configuration.
     * @throws IllegalStateException If any configuration key not set correct.
     */
    @NotNull
    private DatabaseConfig getDatabaseConfig() throws IllegalStateException {
        ConfigurationSection dbCfg = plugin.getConfig().getConfigurationSection("database");
        if (dbCfg == null) {
            throw new IllegalStateException("Database configuration section not found!");
        }
        if (!dbCfg.isSet("mysql")) {
            throw new IllegalStateException("Database configuration section -> type not set!");
        }
        if (!dbCfg.isSet("prefix")) {
            throw new IllegalStateException("Database configuration section -> prefix not set!");
        }
        boolean mysql = dbCfg.getBoolean("mysql");
        if (mysql) {
            if (!dbCfg.isSet("host")) {
                throw new IllegalStateException("Database configuration section -> host not set!");
            }
            if (!dbCfg.isSet("port")) {
                throw new IllegalStateException("Database configuration section -> port not set!");
            }
            if (!dbCfg.isSet("user")) {
                throw new IllegalStateException("Database configuration section -> user not set!");
            }
            if (!dbCfg.isSet("password")) {
                throw new IllegalStateException("Database configuration section -> password not set!");
            }
            if (!dbCfg.isSet("database")) {
                throw new IllegalStateException("Database configuration section -> name not set!");
            }
            if (!dbCfg.isSet("usessl")) {
                throw new IllegalStateException("Database configuration section -> SSL not set!");
            }
        }

        String user = dbCfg.getString("user", "mc");
        String pass = dbCfg.getString("password", "minecraft");
        String host = dbCfg.getString("host", "localhost");
        int port = dbCfg.getInt("port", 3306);
        String database = dbCfg.getString("database", "mc");
        boolean useSSL = dbCfg.getBoolean("usessl", false);
        String dbPrefix = "";
        if (mysql) {
            dbPrefix = dbCfg.getString("prefix", "");
            if ("none".equals(dbPrefix)) {
                dbPrefix = "";
            }
        }
        return new DatabaseConfig(mysql, host, user, pass, port, database, useSSL, dbPrefix);
    }

    void close() {
        EasySQL.shutdownManager(this.liveDatabase);
    }

    /**
     * Returns a valid SQLManager for the live database.
     *
     * @return A valid SQLManager for the live database.
     * @throws IllegalStateException Something not ready.
     * @throws ConnectException      Failed to connect to the database.
     */
    @NotNull
    private SQLManager getLiveDatabase() throws IllegalStateException, ConnectException {
        if (liveDatabase != null) {
            return liveDatabase;
        }
        HikariConfig config = HikariUtil.createHikariConfig();
        SQLManager manager;
        try {
            DatabaseConfig databaseConfig = getDatabaseConfig();
            if (databaseConfig.isMysql()) {
                config.setJdbcUrl("jdbc:mysql://" + databaseConfig.getHost() + ":" + databaseConfig.getPort()
                        + "/" + databaseConfig.getDatabase() + "?useSSL=" + databaseConfig.isUseSSL());
                config.setUsername(databaseConfig.getUser());
                config.setPassword(databaseConfig.getPass());
                manager = EasySQL.createManager(config);
                liveDatabase = manager;
            } else {
                // SQLite database - Doing this handles file creation
                Driver.load();
                config.setJdbcUrl("jdbc:h2:" + new File(plugin.getDataFolder(), "shops").getCanonicalFile().getAbsolutePath() + ";DB_CLOSE_DELAY=-1;MODE=MYSQL");
                manager = EasySQL.createManager(config);
                liveDatabase = manager;
                manager.executeSQL("SET MODE=MYSQL"); // Switch to MySQL mode
            }
            if (!manager.getConnection().isValid(10)) {
                throw new ConnectException("Live database is not valid!");
            }
            return manager;
        } catch (Exception e) {
            throw new ConnectException("Couldn't connect to live database! " + e.getMessage());
        }
    }

    /**
     * Returns a valid connection for SQLite database.
     *
     * @return A valid connection for SQLite database.
     * @throws IllegalStateException Something not ready.
     * @throws ConnectException      Connection to SQLite failed.
     */
    @NotNull
    private Connection getSQLiteDatabase() throws IllegalStateException, ConnectException {
        File sqliteFile = new File(plugin.getDataFolder(), "shops.db");
        if (!sqliteFile.exists()) {
            throw new IllegalStateException("SQLite database not found!");
        }
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteFile);
            if (!connection.isValid(10)) {
                throw new ConnectException("SQLite database is not valid!");
            }
            return connection;
        } catch (SQLException exception) {
            throw new ConnectException("Failed to connect to SQLite database!" + exception.getMessage());
        }
    }

    /**
     * Returns true if the given table has the given column
     *
     * @param table  The table
     * @param column The column
     * @return True if the given table has the given column
     */
    public boolean hasColumn(@NotNull String table, @NotNull String column, @NotNull Connection connection) throws SQLException {
        String query = "SELECT * FROM " + table + " LIMIT 1";
        boolean match = false;
        try (PreparedStatement ps = connection.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData metaData = rs.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                if (metaData.getColumnLabel(i).equals(column)) {
                    match = true;
                    break;
                }
            }
        }
        return match; // Uh, wtf.
    }

    /**
     * Returns true if the table exists
     *
     * @param table The table to check for
     * @return True if the table is found
     * @throws SQLException Throw exception when failed execute somethins on SQL
     */
    public boolean hasTable(@NotNull String table, @NotNull Connection connection) throws SQLException {
        boolean match = false;
        try (ResultSet rs = connection.getMetaData().getTables(null, null, "%", null)) {
            while (rs.next()) {
                if (table.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
                    match = true;
                    break;
                }
            }
        }
        return match;
    }

    @NotNull
    private List<ShopStorageUnit> pullShops(@NotNull String shopsTable, @NotNull Connection connection) throws SQLException {
        instance.getLogger().info("Preparing for pulling shops from database...");
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM " + shopsTable);
        List<ShopStorageUnit> units = new ArrayList<>();
        int count = 0;
        int fails = 0;
        while (resultSet.next()) {
            ++count;
            instance.getLogger().info("Pulling and converting shops from database, downloading: #" + count + "...");
            ShopStorageUnit.ShopStorageUnitBuilder builder = ShopStorageUnit.builder();
            try {
                builder.owner(resultSet.getString("owner"));
                builder.price(resultSet.getDouble("price"));
                builder.itemConfig(resultSet.getString("itemConfig"));
                builder.x(resultSet.getInt("x"));
                builder.y(resultSet.getInt("y"));
                builder.z(resultSet.getInt("z"));
                builder.world(resultSet.getString("world"));
                builder.unlimited(resultSet.getInt("unlimited"));
                builder.type(resultSet.getInt("type"));
                builder.currency(resultSet.getString("currency"));
                builder.extra(resultSet.getString("extra"));
                builder.disableDisplay(resultSet.getInt("disableDisplay"));
                builder.taxAccount(resultSet.getString("taxAccount"));
                units.add(builder.build());
            } catch (SQLException exception) {
                instance.getLogger().log(Level.WARNING, "Error while pulling shop from database: " + exception.getMessage() + ", skipping...", exception);
                ++fails;
            }
        }
        instance.getLogger().info("Completed! Pulled " + (count - fails) + " shops from database! Total " + fails + " fails.");
        return units;

    }

    private void pushShops(@NotNull List<ShopStorageUnit> units, @NotNull String prefix, @NotNull SQLManager manager) {
        instance.getLogger().info("Preparing to pushing shops into database...");
        instance.getLogger().info("Statistics: Total " + units.size() + " shops waiting for pushing.");
        instance.getLogger().info("Initializing target database...");
        int count = 0;
        int fails = 0;
        for (ShopStorageUnit unit : units) {
            ++count;
            instance.getLogger().info("Pushing shop " + count + " of " + units.size() + " to target database...");
            try {
                manager.createInsert(prefix + "shops")
                        .setColumnNames("owner", "price", "itemConfig", "x", "y", "z", "world", "unlimited", "type", "extra",
                                "currency", "disableDisplay", "taxAccount", "inventorySymbolLink", "inventoryWrapperName")
                        .setParams(unit.getOwner(), unit.getPrice(), unit.getItemConfig(), unit.getX(), unit.getY(), unit.getZ(), unit.getWorld(), unit.getUnlimited(), unit.getType(), unit.getExtra(),
                                unit.getCurrency(), unit.getDisableDisplay(), unit.getTaxAccount(), unit.getInventorySymbolLink(), unit.getInventoryWrapperName())
                        .execute();
            } catch (Exception e) {
                ++fails;
                instance.getLogger().log(Level.WARNING, "Failed to push shop " + unit + " into database! " + e.getMessage() + ", skipping...", e);
            }
        }
        instance.getLogger().info("Pushed " + count + " shops into database. " + fails + " shops failed to push.");
    }

    /**
     * Rename tables
     *
     * @param actionId ActionID
     * @param config   DatabaseConfig
     * @return The shops table name
     * @throws Exception Any error happens
     */
    @NotNull
    private String renameTables(@NotNull UUID actionId, @NotNull DatabaseConfig config) throws Exception {
        if (config.isMysql()) {
            SQLManager manager = getLiveDatabase();
            silentTableCopy(manager, config.getPrefix() + "shops", config.getPrefix() + "shops_" + actionId.toString().replace("-", ""));
            silentTableCopy(manager, config.getPrefix() + "messages", config.getPrefix() + "messages_" + actionId.toString().replace("-", ""));
            silentTableCopy(manager, config.getPrefix() + "logs", config.getPrefix() + "logs_" + actionId.toString().replace("-", ""));
            silentTableCopy(manager, config.getPrefix() + "external_cache", config.getPrefix() + "external_cache_" + actionId.toString().replace("-", ""));
            try (Connection connection = manager.getConnection()) {
                if (!hasTable(config.getPrefix() + "shops_" + actionId.toString().replace("-", ""), connection)) {
                    throw new IllegalStateException("Failed to rename tables!");
                }
            }
            return config.getPrefix() + "shops_" + actionId.toString().replace("-", "");
        } else {
            return config.getPrefix() + "shops";
        }
    }

    private boolean silentTableCopy(@NotNull SQLManager manager, @NotNull String originTableName, @NotNull String newTableName) {
        try (Connection conn = manager.getConnection()) {
            if (hasTable(originTableName, conn)) {
                if (getDatabaseConfig().isMysql()) {
                    manager.executeSQL("CREATE TABLE " + newTableName + " SELECT * FROM " + originTableName);
                } else {
                    manager.executeSQL("CREATE TABLE " + newTableName + " AS SELECT * FROM " + originTableName);
                }
            } else {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    @Data
    static class DatabaseConfig {
        private final boolean mysql;
        private final String host;
        private final String user;
        private final String pass;
        private final int port;
        private final String database;
        private final boolean useSSL;
        private final String prefix;

        public DatabaseConfig(boolean mysql, String host, String user, String pass, int port, String database, boolean useSSL, String prefix) {
            this.mysql = mysql;
            this.host = host;
            this.user = user;
            this.pass = pass;
            this.port = port;
            this.database = database;
            this.useSSL = useSSL;
            this.prefix = prefix;
        }
    }

    @Builder
    @Getter
    @ToString
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

        public ShopStorageUnit(String owner, double price, String itemConfig, int x, int y, int z, String world, int unlimited, int type, String extra, String currency, int disableDisplay, String taxAccount) {
            this.owner = owner;
            this.price = price;
            this.itemConfig = itemConfig;
            this.x = x;
            this.y = y;
            this.z = z;
            this.world = world;
            this.unlimited = unlimited;
            this.type = type;
            this.extra = extra;
            this.currency = currency;
            this.disableDisplay = disableDisplay;
            this.taxAccount = taxAccount;
        }

        @NotNull
        public String getInventorySymbolLink() {
            String holder = JsonUtil.standard().toJson(new BukkitInventoryWrapperManager.BlockHolder(world, x, y, z));
            String link = JsonUtil.standard().toJson(new BukkitInventoryWrapperManager.CommonHolder(BukkitInventoryWrapperManager.HolderType.BLOCK, holder));
            Log.debug("Generating SymbolLink: " + link + ", InventoryHolder: BukkitInventoryWrapper, Holder:" + holder);
            return link;
        }

        @NotNull
        public String getInventoryWrapperName() {
            return QuickShop.getInstance().getJavaPlugin().getDescription().getName();
        }
    }
}
