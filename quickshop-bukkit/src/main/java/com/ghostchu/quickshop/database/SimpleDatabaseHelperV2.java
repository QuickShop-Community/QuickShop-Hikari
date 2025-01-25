package com.ghostchu.quickshop.database;

import cc.carm.lib.easysql.action.PreparedSQLBatchUpdateActionImpl;
import cc.carm.lib.easysql.api.SQLManager;
import cc.carm.lib.easysql.api.SQLQuery;
import cc.carm.lib.easysql.api.builder.TableQueryBuilder;
import cc.carm.lib.easysql.api.enums.IndexType;
import cc.carm.lib.easysql.manager.SQLManagerImpl;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.database.DatabaseHelper;
import com.ghostchu.quickshop.api.database.ShopMetricRecord;
import com.ghostchu.quickshop.api.database.bean.DataRecord;
import com.ghostchu.quickshop.api.database.bean.InfoRecord;
import com.ghostchu.quickshop.api.database.bean.ShopRecord;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.cache.ShopInventoryCountCache;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.common.util.JsonUtil;
import com.ghostchu.quickshop.common.util.QuickExecutor;
import com.ghostchu.quickshop.database.bean.SimpleDataRecord;
import com.ghostchu.quickshop.shop.ContainerShop;
import com.ghostchu.quickshop.shop.cache.SimpleShopInventoryCountCache;
import com.ghostchu.quickshop.util.PackageUtil;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.performance.PerfMonitor;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A Util to execute all SQLs.
 */
public class SimpleDatabaseHelperV2 implements DatabaseHelper {

  @NotNull
  private final SQLManager manager;

  @NotNull
  private final QuickShop plugin;

  @NotNull
  private final String prefix;

  private final int LATEST_DATABASE_VERSION = 17;

  public SimpleDatabaseHelperV2(@NotNull final QuickShop plugin, @NotNull final SQLManager manager, @NotNull final String prefix) throws Exception {

    this.plugin = plugin;
    this.manager = manager;
    this.prefix = prefix;
    //manager.setDebugMode(Util.isDevMode());
    checkTables();
    checkColumns();
    checkDatabaseVersion();
  }

  private void checkDatabaseVersion() {

    if(PackageUtil.parsePackageProperly("skipDatabaseVersionCheck").asBoolean(false)) {
      return;
    }
    final int databaseVersion = getDatabaseVersion();
    if(databaseVersion > LATEST_DATABASE_VERSION) {
      throw new IllegalStateException("Database schema version " + databaseVersion + " is newer than this support max supported schema version " + LATEST_DATABASE_VERSION + ", downgrading the QuickShop-Hikari without restore the database from backup is disallowed cause it will break the data.");
    }
  }

  public void checkTables() throws SQLException {

    final boolean metadataExists = DataTables.METADATA.isExists(manager, prefix);
    DataTables.initializeTables(manager, prefix);
    if(!metadataExists) {
      setDatabaseVersion(LATEST_DATABASE_VERSION);
    }
  }


  /**
   * Verifies that all required columns exist.
   */
  public void checkColumns() {

    plugin.logger().info("Checking and updating database columns, it may take a while...");
    try(final PerfMonitor ignored = new PerfMonitor("Perform database schema upgrade")) {
      new DatabaseUpgrade(this).upgrade();
      if(getDatabaseVersion() != LATEST_DATABASE_VERSION) {
        plugin.logger().warn("Database not upgrade to latest schema, or the developer forget update the version number, please report this to developer.");
      }
    }
    plugin.logger().info("Finished!");
  }

  public int getDatabaseVersion() {

    try(final SQLQuery query = DataTables.METADATA
            .createQuery()
            .addCondition("key", "database_version")
            .selectColumns("value")
            .setLimit(1)
            .build().execute()) {
      final ResultSet result = query.getResultSet();
      if(!result.next()) {
        return -1; // Default latest version
      }
      return Integer.parseInt(result.getString("value"));
    } catch(final SQLException e) {
      Log.debug("Failed to getting database version! Err: " + e.getMessage());
      return -1;
    }
  }

  public @NotNull CompletableFuture<@NotNull Integer> setDatabaseVersion(final int version) {

    return DataTables.METADATA
            .createReplace()
            .setColumnNames("key", "value")
            .setParams("database_version", version)
            .executeFuture(lines->lines);
  }


  public CompletableFuture<Integer> purgeIsolated() {

    return CompletableFuture.supplyAsync(()->{
      final List<Long> shop2ShopMapIds = listAllANotExistsInB(DataTables.SHOPS, "id", DataTables.SHOP_MAP, "shop");
      final List<Long> shop2LogPurchaseIds = listAllANotExistsInB(DataTables.SHOPS, "id", DataTables.LOG_PURCHASE, "shop");
      final List<Long> shop2LogChangesIds = listAllANotExistsInB(DataTables.SHOPS, "id", DataTables.LOG_CHANGES, "shop");
      final List<Long> shop2Tags = listAllANotExistsInB(DataTables.SHOPS, "id", DataTables.TAGS, "shop");
      final List<Long> shopAllIds = CommonUtil.linkLists(shop2LogChangesIds, shop2LogPurchaseIds, shop2Tags);
      final List<Long> shopIsolatedFinal = new ArrayList<>(shop2ShopMapIds);
      shopIsolatedFinal.retainAll(shopAllIds);
      shopIsolatedFinal.forEach(isolatedShopId->{
        try {
          DataTables.SHOPS.createDelete().addCondition("id", isolatedShopId).build().execute();
        } catch(final SQLException e) {
          Log.debug("Failed to delete: " + e.getMessage());
        }
      });
      final List<Long> data2ShopIds = listAllANotExistsInB(DataTables.DATA, "id", DataTables.SHOPS, "data");
      final List<Long> data2LogPurchaseIds = listAllANotExistsInB(DataTables.DATA, "id", DataTables.LOG_PURCHASE, "data");
      final List<Long> dataIsolatedFinal = new ArrayList<>(data2ShopIds);
      dataIsolatedFinal.retainAll(data2LogPurchaseIds);
      dataIsolatedFinal.forEach(isolatedDataId->{
        try {
          DataTables.DATA.createDelete().addCondition("id", isolatedDataId).build().execute();
        } catch(final SQLException e) {
          Log.debug("Failed to delete: " + e.getMessage());
        }
      });
      return shopIsolatedFinal.size() + dataIsolatedFinal.size();
    }, QuickExecutor.getCommonExecutor());
  }

  @NotNull
  public List<Long> listAllANotExistsInB(final DataTables aTable, final String aId, final DataTables bTable, final String bId) {

    final List<Long> isolatedIds = new ArrayList<>();
    final String SQL = "SELECT " + aId + " FROM " + aTable.getName() + " WHERE " + aId + " NOT IN (SELECT " + bId + " FROM " + bTable.getName() + ")";
    try(final SQLQuery query = manager.createQuery().withPreparedSQL(SQL).execute()) {
      final ResultSet rs = query.getResultSet();
      while(rs.next()) {
        final long id = rs.getLong(aId);
        isolatedIds.add(id);
      }
    } catch(final SQLException e) {
      plugin.logger().warn("Failed to list all " + aTable.getName() + " not exists in " + bTable.getName() + "!", e);
    }
    return isolatedIds;
  }

  private void fastBackup() {

    try {
      final DatabaseIOUtil databaseIOUtil = new DatabaseIOUtil(this);
      databaseIOUtil.performBackup("database-upgrade");
    } catch(final Throwable throwable) {
      plugin.logger().warn("Failed to backup the database.", throwable);
    }
  }

  private void upgradeBenefit() {

    fastBackup();
    try {
      getManager().alterTable(DataTables.DATA.getName())
              .addColumn("benefit", "MEDIUMTEXT")
              .execute();
    } catch(final SQLException e) {
      Log.debug("Failed to add benefit column in " + DataTables.DATA.getName() + "! Err:" + e.getMessage());
    }
  }

  public @NotNull SQLManager getManager() {

    return manager;
  }

  private void addEncodedColumn() {
    fastBackup();
    try {
      getManager().alterTable(DataTables.DATA.getName())
              .addColumn("encoded", "TEXT NOT NULL")
              .execute();
    } catch(final SQLException e) {
      Log.debug("Failed to add encoded " + DataTables.DATA.getName() + "! Err:" + e.getMessage());
    }
  }

  private void upgradePlayers() {

    fastBackup();
    try {
      getManager().alterTable(DataTables.PLAYERS.getName())
              .modifyColumn("locale", "VARCHAR(255)")
              .execute();
      getManager().alterTable(DataTables.PLAYERS.getName())
              .addColumn("cachedName", "VARCHAR(255)")
              .execute();
    } catch(final SQLException e) {
      Log.debug("Failed to add cachedName or modify locale column in " + DataTables.DATA.getName() + "! Err:" + e.getMessage());
    }
  }

  private void upgradeUniqueIdsField() {

    fastBackup();
    CompletableFuture.allOf(
            manager.alterTable(DataTables.DATA.getName())
                    .modifyColumn("owner", "VARCHAR(128) NOT NULL")
                    .executeFuture(),
            manager.alterTable(DataTables.DATA.getName())
                    .modifyColumn("tax_account", "VARCHAR(64)")
                    .executeFuture(),
            manager.alterTable(DataTables.LOG_PURCHASE.getName())
                    .modifyColumn("buyer", "VARCHAR(128) NOT NULL")
                    .executeFuture(),
            manager.alterTable(DataTables.LOG_TRANSACTION.getName())
                    .modifyColumn("from", "VARCHAR(128) NOT NULL")
                    .executeFuture(),
            manager.alterTable(DataTables.LOG_TRANSACTION.getName())
                    .modifyColumn("to", "VARCHAR(128) NOT NULL")
                    .executeFuture(),
            manager.alterTable(DataTables.LOG_TRANSACTION.getName())
                    .modifyColumn("tax_account", "VARCHAR(64)")
                    .executeFuture()).join();
  }

  private void upgradeWorldNameLength() {

    fastBackup();
    manager.alterTable(DataTables.SHOP_MAP.getName())
            .modifyColumn("world", "VARCHAR(255) NOT NULL")
            .executeFuture().join();
  }

  private void upgradeTablesEncoding() {

    fastBackup();
    for(final DataTables value : DataTables.values()) {
      if(value.isExists(manager, prefix)) {
        final Integer integer = manager.executeSQL("ALTER TABLE `" + value.getName() + "` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");
        Log.debug("Changing the table " + value.getName() + " charset to utf8mb4, returns " + integer + " lines changed.");
      } else {
        Log.debug("Table " + value.getName() + " not exists, skipping.");
      }
    }
  }

  public @NotNull String getPrefix() {

    return prefix;
  }

  @Override
  @NotNull
  public CompletableFuture<@NotNull Integer> cleanMessage(final long weekAgo) {

    return DataTables.MESSAGES.createDelete()
            .addTimeCondition("time", -1L, weekAgo)
            .build()
            .executeFuture(lines->lines);
  }

  @Override
  public @NotNull CompletableFuture<@NotNull Integer> cleanMessageForPlayer(@NotNull final UUID player) {

    return DataTables.MESSAGES.createDelete()
            .addCondition("receiver", player.toString())
            .build().executeFuture(lines->lines);
  }

  @Override
  public @NotNull CompletableFuture<@NotNull Long> createData(@NotNull final Shop shop) {

    final SimpleDataRecord simpleDataRecord = ((ContainerShop)shop).createDataRecord();
    return queryDataId(simpleDataRecord).thenCompose(id->{
      if(id == null) {
        final Map<String, Object> map = simpleDataRecord.generateParams();
        return DataTables.DATA.createInsert()
                .setColumnNames(new ArrayList<>(map.keySet()))
                .setParams(map.values())
                .returnGeneratedKey(Long.class).executeFuture(i->i);
      } else {
        return CompletableFuture.completedFuture(id);
      }
    });
  }

  @Override
  @NotNull
  public CompletableFuture<@NotNull Long> createShop(final long dataId) {

    Validate.isTrue(dataId > 0, "Data ID must be greater than 0!");
    return DataTables.SHOPS.createInsert()
            .setColumnNames("data")
            .setParams(dataId)
            .returnGeneratedKey(Long.class)
            .executeFuture(dat->dat);
  }

  @Override
  public CompletableFuture<@NotNull Void> createShopMap(final long shopId, @NotNull final Location location) {

    Validate.isTrue(shopId > 0, "Shop ID must be greater than 0!");
    return DataTables.SHOP_MAP.createReplace()
            .setColumnNames("world", "x", "y", "z", "shop")
            .setParams(location.getWorld().getName(),
                       location.getBlockX(),
                       location.getBlockY(),
                       location.getBlockZ(),
                       shopId)
            .executeFuture();
  }

  @Override
  public @NotNull CompletableFuture<@Nullable DataRecord> getDataRecord(final long dataId) {

    return DataTables.DATA.createQuery()
            .addCondition("id", dataId)
            .setLimit(1)
            .build()
            .executeFuture(query->{
              final ResultSet result = query.getResultSet();
              if(result.next()) {
                return new SimpleDataRecord(plugin.getPlayerFinder(), result);
              }
              return null;
            });
  }

  @Override
  @NotNull
  public CompletableFuture<@Nullable String> getPlayerLocale(@NotNull final UUID uuid) {

    return DataTables.PLAYERS.createQuery()
            .addCondition("uuid", uuid.toString())
            .selectColumns("locale")
            .setLimit(1)
            .build()
            .executeFuture(sqlQuery->{
                             final ResultSet set = sqlQuery.getResultSet();
                             if(set.next()) {
                               return set.getString("locale");
                             }
                             return null;
                           }
                          );
  }

  @Override
  public CompletableFuture<@Nullable String> getPlayerLocale(@NotNull final QUser qUser) {

    final UUID uuid = qUser.getUniqueIdIfRealPlayer().orElse(null);
    if(uuid == null) {
      return null;
    }
    return getPlayerLocale(uuid);
  }

  @Override
  public CompletableFuture<@Nullable String> getPlayerName(@NotNull final UUID uuid) {

    return DataTables.PLAYERS.createQuery()
            .addCondition("uuid", uuid.toString())
            .selectColumns("cachedName")
            .setLimit(1)
            .build()
            .executeFuture(sqlQuery->{
                             final ResultSet set = sqlQuery.getResultSet();
                             if(set.next()) {
                               return set.getString("cachedName");
                             }
                             return null;
                           }
                          );
  }

  @Override
  public CompletableFuture<@Nullable UUID> getPlayerUUID(@NotNull final String name) {

    return DataTables.PLAYERS.createQuery()
            .addCondition("cachedName", name)
            .selectColumns("uuid")
            .setLimit(1)
            .build()
            .executeFuture(sqlQuery->{
                             final ResultSet set = sqlQuery.getResultSet();
                             if(set.next()) {
                               return UUID.fromString(set.getString("uuid"));
                             }
                             return null;
                           }
                          );
  }

  @Override
  public @NotNull CompletableFuture<@NotNull Integer> insertHistoryRecord(@NotNull final Object rec) {

    return DataTables.LOG_OTHERS.createInsert()
            .setColumnNames("type", "data")
            .setParams(rec.getClass().getName(), JsonUtil.getGson().toJson(rec))
            .executeFuture(lines->lines);
  }

  @Override
  public @NotNull CompletableFuture<@NotNull Integer> insertMetricRecord(@NotNull final ShopMetricRecord metricRecord) {

    final CompletableFuture<Integer> future = new CompletableFuture<>();
    plugin.getDatabaseHelper().locateShopDataId(metricRecord.getShopId()).whenCompleteAsync((dataId, err)->{
      if(err != null) {
        future.completeExceptionally(err);
      }
      DataTables.LOG_PURCHASE
              .createInsert()
              .setColumnNames("time", "shop", "data", "buyer", "type", "amount", "money", "tax")
              .setParams(new Date(metricRecord.getTime()), metricRecord.getShopId()
                      , dataId, metricRecord.getPlayer(), metricRecord.getType().name(),
                         metricRecord.getAmount(), metricRecord.getTotal(), metricRecord.getTax())
              .executeFuture(lines->lines).whenComplete((line, err2)->{
                if(err2 != null) {
                  future.completeExceptionally(err2);
                }
                future.complete(line);
              });
    });
    return future;

  }

  @Override
  public void insertTransactionRecord(@Nullable UUID from, @Nullable UUID to, final double amount, @Nullable final String currency, final double taxAmount, @Nullable final UUID taxAccount, @Nullable final String error) {

    if(from == null) {
      from = CommonUtil.getNilUniqueId();
    }
    if(to == null) {
      to = CommonUtil.getNilUniqueId();
    }
    DataTables.LOG_TRANSACTION.createInsert()
            .setColumnNames("from", "to", "currency", "amount", "tax_amount", "tax_account", "error")
            .setParams(from.toString(), to.toString(), currency, amount, taxAmount, taxAccount == null? null : taxAccount.toString(), error)
            .executeAsync(handler->Log.debug("Operation completed, insertTransactionRecord, " + handler + " lines affected"));
  }

  @Override
  public @NotNull List<ShopRecord> listShops(final boolean deleteIfCorrupt) {

    return listShops(null, deleteIfCorrupt);
  }

  @Override
  public @NotNull List<ShopRecord> listShops(@Nullable final String worldFilter, final boolean deleteIfCorrupt) {

    final List<ShopRecord> shopRecords = new ArrayList<>();
    final String SQL = "SELECT * FROM " + DataTables.DATA.getName()
                       + " INNER JOIN " + DataTables.SHOPS.getName()
                       + " ON " + DataTables.DATA.getName() + ".id = " + DataTables.SHOPS.getName() + ".data"
                       + " INNER JOIN " + DataTables.SHOP_MAP.getName()
                       + " ON " + DataTables.SHOP_MAP.getName() + ".shop = " + DataTables.SHOPS.getName() + ".id";
    try(final SQLQuery query = manager.createQuery().withPreparedSQL(SQL).execute()) {
      final ResultSet rs = query.getResultSet();
      while(rs.next()) {
        final String world = rs.getString("world");
        if(worldFilter != null && !worldFilter.equals(world)) {
          continue;
        }
        final long shopId = rs.getLong("shop");
        final int x = rs.getInt("x");
        final int y = rs.getInt("y");
        final int z = rs.getInt("z");
        final DataRecord dataRecord = new SimpleDataRecord(plugin.getPlayerFinder(), rs);
        final InfoRecord infoRecord = new ShopInfo(shopId, world, x, y, z);
        shopRecords.add(new ShopRecord(dataRecord, infoRecord));
      }
    } catch(final SQLException e) {
      plugin.logger().error("Failed to list shops", e);
    }
    return shopRecords;
  }

  @Override
  public @NotNull List<Long> listShopsTaggedBy(@NotNull final UUID tagger, @NotNull final String tag) {

    final List<Long> shopIds = new ArrayList<>();
    try(final SQLQuery query = DataTables.TAGS.createQuery()
            .addCondition("tagger", tagger.toString())
            .addCondition("tag", tag)
            .build().execute()) {
      final ResultSet set = query.getResultSet();
      shopIds.add(set.getLong("shop"));
    } catch(final SQLException e) {
      plugin.logger().error("Failed to list shops tagged by " + tagger + " with tag " + tag, e);
    }
    return shopIds;
  }

  @Override
  public @NotNull List<String> listTags(@NotNull final UUID tagger) {

    final List<String> tags = new ArrayList<>();
    try(final SQLQuery query = DataTables.TAGS.createQuery()
            .addCondition("tagger", tagger.toString())
            .build().execute()) {
      final ResultSet set = query.getResultSet();
      tags.add(set.getString("tag"));
    } catch(final SQLException e) {
      plugin.logger().error("Failed to list tags by " + tagger, e);
    }
    return tags;
  }

  @Override
  public CompletableFuture<@Nullable Integer> removeShopTag(@NotNull final UUID tagger, @NotNull final Long shopId, @NotNull final String tag) {

    return DataTables.TAGS.createDelete()
            .addCondition("tagger", tagger.toString())
            .addCondition("shop", shopId)
            .addCondition("tag", tag).build().executeFuture(i->i);
  }

  @Override
  public CompletableFuture<@Nullable Integer> removeShopAllTag(@NotNull final UUID tagger, @NotNull final Long shopId) {

    return DataTables.TAGS.createDelete()
            .addCondition("tagger", tagger.toString())
            .addCondition("shop", shopId)
            .build().executeFuture(i->i);
  }

  @Override
  public CompletableFuture<@Nullable Integer> removeTagFromShops(@NotNull final UUID tagger, @NotNull final String tag) {

    return DataTables.TAGS.createDelete()
            .addCondition("tagger", tagger.toString())
            .addCondition("tag", tag)
            .build().executeFuture(i->i);
  }

  @Override
  public @NotNull CompletableFuture<@Nullable Integer> tagShop(@NotNull final UUID tagger, @NotNull final Long shopId, @NotNull final String tag) {

    return DataTables.TAGS.createInsert()
            .setColumnNames("tagger", "shop", "tag")
            .setParams(tagger.toString(), shopId, tag)
            .executeFuture(i->i);
  }

  @Override
  public @NotNull CompletableFuture<@Nullable Long> locateShopDataId(final long shopId) {

    return DataTables.SHOPS.createQuery()
            .addCondition("id", shopId)
            .setLimit(1)
            .build()
            .executeFuture(query->{
              final ResultSet result = query.getResultSet();
              if(result.next()) {
                return result.getLong("data");
              }
              return null;
            });
  }

  @Override
  @NotNull
  public CompletableFuture<@Nullable Long> locateShopId(@NotNull final String world, final int x, final int y, final int z) {

    return DataTables.SHOP_MAP.createQuery()
            .addCondition("world", world)
            .addCondition("x", x)
            .addCondition("y", y)
            .addCondition("z", z)
            .setLimit(1)
            .build().executeFuture(query->{
              final ResultSet result = query.getResultSet();
              if(result.next()) {
                return result.getLong("shop");
              } else {
                return null;
              }
            });
  }

  @Override
  public @NotNull CompletableFuture<@NotNull Integer> removeData(final long dataId) {

    Validate.isTrue(dataId > 0, "Data ID must be greater than 0!");
    return DataTables.DATA.createDelete()
            .addCondition("id", dataId)
            .build().executeFuture(lines->lines);
  }

  @Override
  public @NotNull CompletableFuture<@NotNull Integer> removeShop(final long shopId) {

    Validate.isTrue(shopId > 0, "Shop ID must be greater than 0!");
    return DataTables.SHOPS.createDelete()
            .addCondition("id", shopId)
            .build().executeFuture(lines->lines);
  }

  @Override
  public @NotNull CompletableFuture<@NotNull Integer> removeShopMap(@NotNull final String world, final int x, final int y, final int z) {
    // TODO: Execute isolated data check in async thread
    return DataTables.SHOP_MAP.createDelete()
            .addCondition("world", world)
            .addCondition("x", x)
            .addCondition("y", y)
            .addCondition("z", z)
            .build()
            .executeFuture(lines->lines);
  }

  @Override
  public @NotNull CompletableFuture<@NotNull Integer> saveOfflineTransactionMessage(@NotNull final UUID player, @NotNull final String message, final long time) {

    return DataTables.MESSAGES.createInsert()
            .setColumnNames("receiver", "time", "content")
            .setParams(player.toString(), new Date(time), message)
            .executeFuture(lines->lines);
  }

  @Override
  public @NotNull SQLQuery selectAllMessages() throws SQLException {

    return DataTables.MESSAGES.createQuery().build().execute();
  }

  @Override
  public @NotNull CompletableFuture<List<String>> selectPlayerMessages(final UUID player) {

    return DataTables.MESSAGES.createQuery()
            .addCondition("receiver", player.toString())
            .selectColumns()
            .build()
            .executeFuture(dat->{
              final List<String> msgs = new ArrayList<>();
              try(final ResultSet set = dat.getResultSet()) {
                while(set.next()) {
                  msgs.add(set.getString("content"));
                }
              }
              return msgs;
            });
  }

  @Override
  public @NotNull SQLQuery selectTable(@NotNull final String table) throws SQLException {

    return manager.createQuery()
            .inTable(prefix + table)
            .build()
            .execute();
  }

  @Override
  @NotNull
  public CompletableFuture<@NotNull Integer> updatePlayerProfile(@NotNull final UUID uuid, @Nullable final String locale, @NotNull final String username) {

    if(locale != null) {
      return DataTables.PLAYERS.createReplace()
              .setColumnNames("uuid", "locale", "cachedName")
              .setParams(uuid.toString(), locale, username)
              .executeFuture(lines->lines);
    } else {
      return CompletableFuture.supplyAsync(()->{
        String cachedLocale = getPlayerLocale(uuid).join();
        if(cachedLocale == null) {
          cachedLocale = "en_us";
        }
        return DataTables.PLAYERS.createReplace()
                .setColumnNames("uuid", "locale", "cachedName")
                .setParams(uuid.toString(), cachedLocale, username)
                .executeFuture(lines->lines).join();
      });
    }
  }

  @Override
  public CompletableFuture<Integer> updatePlayerProfileInBatch(final List<Triple<UUID, String, String>> uuidLocaleUsername) {

    final List<Object[]> specificLocale = new ArrayList<>();
    final List<Triple<UUID, String, String>> unspecificLocale = new ArrayList<>();

    for(final Triple<UUID, String, String> user : uuidLocaleUsername) {
      if(user.getMiddle() == null) {
        unspecificLocale.add(user);
      } else {
        specificLocale.add(new Object[]{ user.getLeft(), user.getMiddle(), user.getRight() });
      }
    }

    final var action = new PreparedSQLBatchUpdateActionImpl<>((SQLManagerImpl)getManager(), Integer.class,
                                                        "INSERT INTO " + DataTables.PLAYERS.getName() + "(uuid, locale, cachedName) VALUES (?, ?, ?) " +
                                                        "ON DUPLICATE KEY UPDATE cachedName = ?"
    );
    for(final Triple<UUID, String, String> data : unspecificLocale) {
      action.addParamsBatch(data.getLeft().toString(), "en_us", data.getRight(), data.getRight());
    }

    return DataTables.PLAYERS.createReplaceBatch().setColumnNames("uuid", "locale", "cachedName")
            .setAllParams(specificLocale)
            .executeFuture(lines->lines.stream().mapToInt(Integer::intValue).sum())
            .thenCombine(action.executeFuture(lines->lines.stream().mapToInt(Integer::intValue).sum()), Integer::sum);
  }

  @Override
  public @NotNull CompletableFuture<@NotNull Integer> updateExternalInventoryProfileCache(final long shopId, final int space, final int stock) {

    Validate.isTrue(shopId > 0, "Shop ID must be greater than 0!");
    return DataTables.EXTERNAL_CACHE.createReplace()
            .setColumnNames("shop", "space", "stock")
            .setParams(shopId, space, stock)
            .executeFuture(lines->lines);
  }

  @Override
  public CompletableFuture<Void> updateShop(@NotNull final Shop shop) {

    final SimpleDataRecord simpleDataRecord = ((ContainerShop)shop).createDataRecord();
    final Location loc = shop.getLocation();
    // check if datarecord exists
    final long shopId = shop.getShopId();
    if(shopId < 1) {
      Log.debug("Warning: Failed to update shop because the shop id locate result for " + loc + ", because the query shopId is " + shopId);
      return null;
    }
    return queryDataId(simpleDataRecord).thenCompose(dataId->{
      if(dataId != null) {
        return DataTables.SHOPS.createUpdate()
                .addCondition("id", shopId)
                .setColumnValues("data", dataId)
                .build()
                .executeFuture();
      } else {
        return createData(shop).thenCompose(createdDataId->DataTables.SHOPS.createUpdate()
                .addCondition("id", shopId)
                .setColumnValues("data", createdDataId)
                .build()
                .executeFuture());
      }
    });
  }

  @Override
  public CompletableFuture<@NotNull ShopInventoryCountCache> queryInventoryCache(final long shopId) {

    return CompletableFuture.supplyAsync(()->{
      ShopInventoryCountCache cache = new SimpleShopInventoryCountCache(-2, -2, false);
      try(final SQLQuery query = DataTables.EXTERNAL_CACHE.createQuery()
              .selectColumns("stock", "space")
              .addCondition("shop", shopId)
              .setLimit(1)
              .build().execute()) {
        final ResultSet set = query.getResultSet();
        if(set.next()) {
          cache = new SimpleShopInventoryCountCache(set.getInt("stock"), set.getInt("space"), true);
        }
      } catch(final SQLException exception) {
        plugin.logger().warn("Cannot handle the inventory cache lookup for shop {}", shopId, exception);
      }
      return cache;
    });
  }

  @NotNull
  public CompletableFuture<@Nullable Long> queryDataId(@NotNull final SimpleDataRecord simpleDataRecord) {
    // Check if dataRecord exists in database with same values
    final Map<String, Object> lookupParams = simpleDataRecord.generateLookupParams();
    final TableQueryBuilder builder = DataTables.DATA.createQuery();
    builder.setLimit(1);
    for(final Map.Entry<String, Object> entry : lookupParams.entrySet()) {
      builder.addCondition(entry.getKey(), entry.getValue());
    }
    return builder.build()
            .executeFuture(query->{
              final ResultSet set = query.getResultSet();
              if(set.next()) {
                final long id = set.getLong("id");
                Log.debug("Found data record with id " + id + " for record " + simpleDataRecord);
                return id;
              }
              Log.debug("No data record found for record basic data: " + simpleDataRecord);
              return null;
            });

  }

  public CompletableFuture<Integer> purgeLogsRecords(@Nullable final Date endDate) {

    return CompletableFuture.supplyAsync(()->{
      int linesAffected = 0;
      try {
        linesAffected += DataTables.LOG_TRANSACTION.createDelete()
                .addTimeCondition("time", null, endDate)
                .build().execute();
        linesAffected += DataTables.LOG_CHANGES.createDelete()
                .addTimeCondition("time", null, endDate)
                .build().execute();
        linesAffected += DataTables.LOG_PURCHASE.createDelete()
                .addTimeCondition("time", null, endDate)
                .build().execute();
        linesAffected += DataTables.LOG_OTHERS.createDelete()
                .addTimeCondition("time", null, endDate)
                .build().execute();
        return linesAffected;
      } catch(final SQLException e) {
        plugin.logger().warn("Failed to purge logs records", e);
        return -1;
      }
    });
  }

  /**
   * Returns true if the given table has the given column
   *
   * @param table  The table
   * @param column The column
   *
   * @return True if the given table has the given column
   *
   * @throws SQLException If the database isn't connected
   */
  public boolean hasColumn(@NotNull final String table, @NotNull final String column) throws SQLException {

    if(!hasTable(table)) {
      return false;
    }
    final String query = "SELECT * FROM " + table + " LIMIT 1";
    boolean match = false;
    try(final Connection connection = manager.getConnection(); final PreparedStatement ps = connection.prepareStatement(query); final ResultSet rs = ps.executeQuery()) {
      final ResultSetMetaData metaData = rs.getMetaData();
      for(int i = 1; i <= metaData.getColumnCount(); i++) {
        if(metaData.getColumnLabel(i).equals(column)) {
          match = true;
          break;
        }
      }
    } catch(final SQLException e) {
      return match;
    }
    return match; // Uh, wtf.
  }


  /**
   * Returns true if the table exists
   *
   * @param table The table to check for
   *
   * @return True if the table is found
   *
   * @throws SQLException Throw exception when failed execute somethins on SQL
   */
  public boolean hasTable(@NotNull final String table) throws SQLException {

    final Connection connection = manager.getConnection();
    boolean match = false;
    try(final ResultSet rs = connection.getMetaData().getTables(null, null, "%", null)) {
      while(rs.next()) {
        if(table.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
          match = true;
          break;
        }
      }
    } finally {
      connection.close();
    }
    return match;
  }

  private void makeBackup() {

    final File backupFile = new File(new File(plugin.getDataFolder(), "backup"), System.currentTimeMillis() + "-I-told-you-backup-database-before-1.20.5-upgrade.zip");
    try {
      new DatabaseIOUtil(this).exportTables(backupFile);
    } catch(final SQLException | IOException e) {
      plugin.logger().warn("Failed to backup database", e);
    }
  }

  static class DatabaseUpgrade {

    private final SimpleDatabaseHelperV2 parent;
    private final String prefix;
    private final Logger logger;
    private final SQLManager manager;

    DatabaseUpgrade(final SimpleDatabaseHelperV2 parent) {

      this.parent = parent;
      this.manager = parent.manager;
      this.logger = parent.plugin.logger();
      this.prefix = parent.getPrefix();
    }

    public void upgrade() {

      int currentDatabaseVersion = parent.getDatabaseVersion();
      if(currentDatabaseVersion == -1) {
        currentDatabaseVersion = 17;
      }
      if(currentDatabaseVersion > parent.LATEST_DATABASE_VERSION) {
        throw new IllegalStateException("The database version is newer than this build supported.");
      }
      if(currentDatabaseVersion == parent.LATEST_DATABASE_VERSION) {
        return;
      }
      if(currentDatabaseVersion <= 3) {
        throw new IllegalStateException("Database Upgrade for <= Hikari 3.0.0.0 is no-longer supported");
      }
      if(currentDatabaseVersion < 9) {
        logger.info("Data upgrading: Performing purge isolated data...");
        parent.purgeIsolated();
        logger.info("Data upgrading: All completed!");
        currentDatabaseVersion = 9;
      }
      if(currentDatabaseVersion == 9) {
        logger.info("Data upgrading: Performing database structure upgrade (benefit)...");
        parent.upgradeBenefit();
        logger.info("Data upgrading: All completed!");
        currentDatabaseVersion = 10;
      }
      if(currentDatabaseVersion == 10) {
        logger.info("Data upgrading: Performing database structure upgrade (players)...");
        parent.upgradePlayers();
        logger.info("Data upgrading: All completed!");
        currentDatabaseVersion = 11;
      }
      if(currentDatabaseVersion == 11) {
        logger.info("Data upgrading: Performing database structure upgrade (uuid field length)...");
        parent.upgradeUniqueIdsField();
        currentDatabaseVersion = 12;
      }
      if(currentDatabaseVersion == 12) {
        logger.info("Data upgrading: Converting data tables to utf8mb4...");
        parent.upgradeTablesEncoding();
        currentDatabaseVersion = 13;
      }
      if(currentDatabaseVersion == 13) {
        logger.info("Data upgrading: Converting shop_map world length to 255...");
        parent.upgradeWorldNameLength();
        currentDatabaseVersion = 14;
      }
      if(currentDatabaseVersion == 14) {
        logger.info("Data upgrading: Creating an Index for the log_purchase table to improve performance...");
        parent.performLogPurchasesIndex();
        currentDatabaseVersion = 15;
      }
      if(currentDatabaseVersion == 15) {
        logger.info("Data upgrading: Just create a backup to avoid of somebody forget to backup database while upgrading to 1.20.5...");
        parent.makeBackup();
        currentDatabaseVersion = 16;
      }

      if(currentDatabaseVersion == 16) {
        logger.info("Data upgrading: Creating a new column... new_item for enhanced item storage.");
        parent.addEncodedColumn();
        currentDatabaseVersion = 17;
      }
      parent.setDatabaseVersion(currentDatabaseVersion).join();
    }

    private boolean silentTableMoving(@NotNull final String originTableName, @NotNull final String newTableName) {

      try {
        if(parent.hasTable(originTableName)) {
          if(parent.plugin.getDatabaseDriverType() == QuickShop.DatabaseDriverType.MYSQL) {
            manager.executeSQL("CREATE TABLE " + newTableName + " SELECT * FROM " + originTableName);
          } else {
            manager.executeSQL("CREATE TABLE " + newTableName + " AS SELECT * FROM " + originTableName);
          }
          manager.executeSQL("DROP TABLE " + originTableName);
        }
      } catch(final SQLException e) {
        return false;
      }
      return true;
    }
  }

  private void performLogPurchasesIndex() {

    try {
      getManager().alterTable(DataTables.LOG_PURCHASE.getName())
              .addIndex(IndexType.INDEX, "idx_log_purchase_shop", "shop")
              .execute();
      getManager().alterTable(DataTables.LOG_PURCHASE.getName())
              .addIndex(IndexType.INDEX, "idx_log_purchase_time", "time")
              .execute();
      getManager().alterTable(DataTables.LOG_PURCHASE.getName())
              .addIndex(IndexType.INDEX, "idx_log_purchase_buyer", "buyer")
              .execute();
    } catch(final SQLException e) {
      plugin.logger().warn("Cannot setup the table index", e);
    }
  }


  private record ShopInfo(long shopID, String world, int x, int y, int z) implements InfoRecord {

    @Override
    public long getShopId() {

      return shopID;
    }

    @Override
    public String getWorld() {

      return world;
    }

    @Override
    public int getX() {

      return x;
    }

    @Override
    public int getY() {

      return y;
    }

    @Override
    public int getZ() {

      return z;
    }
  }
}
