package com.ghostchu.quickshop.addon.discordsrv.database;

import cc.carm.lib.easysql.api.SQLManager;
import cc.carm.lib.easysql.api.action.PreparedSQLUpdateAction;
import cc.carm.lib.easysql.api.action.PreparedSQLUpdateBatchAction;
import cc.carm.lib.easysql.api.builder.DeleteBuilder;
import cc.carm.lib.easysql.api.builder.InsertBuilder;
import cc.carm.lib.easysql.api.builder.ReplaceBuilder;
import cc.carm.lib.easysql.api.builder.TableCreateBuilder;
import cc.carm.lib.easysql.api.builder.TableQueryBuilder;
import cc.carm.lib.easysql.api.builder.UpdateBuilder;
import cc.carm.lib.easysql.api.enums.IndexType;
import cc.carm.lib.easysql.api.function.SQLHandler;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public enum DiscordTables {

    DISCORD_PLAYERS("discord_players", (table) -> {
        table.addAutoIncrementColumn("id", true); // SHOP DATA ID
        table.addColumn("player", "VARCHAR(36) NOT NULL"); // SHOP DATA OWNER (ALL-ZERO if this is server)
        table.addColumn("notifaction", "TEXT NOT NULL"); // NOTIFACTION SETTINGS, JSON ENCODE
        table.setIndex(IndexType.INDEX, "idx_qs_addon_discord_players", "player");
    });
    private final @NotNull String name;
    private final @NotNull SQLHandler<TableCreateBuilder> tableHandler;

    private String prefix;
    private SQLManager manager;

    DiscordTables(@NotNull String name,
                  @NotNull SQLHandler<TableCreateBuilder> tableHandler) {
        this.name = name;
        this.tableHandler = tableHandler;
    }

    public static void initializeTables(@NotNull SQLManager sqlManager,
                                        @NotNull String tablePrefix) throws SQLException {
        for (DiscordTables value : values()) {
            value.create(sqlManager, tablePrefix);
        }
    }

    private void create(@NotNull SQLManager sqlManager, @NotNull String tablePrefix) throws SQLException {
        if (this.manager == null) {
            this.manager = sqlManager;
        }
        this.prefix = tablePrefix;

        TableCreateBuilder tableBuilder = sqlManager.createTable(this.getName());
        tableHandler.accept(tableBuilder);
        tableBuilder.build().execute();
        Log.debug("Table creating:" + this.getName());
    }

    public @NotNull String getName() {
        return this.prefix + this.name;
    }

    public @NotNull DeleteBuilder createDelete() {
        return this.createDelete(this.manager);
    }

    public @NotNull DeleteBuilder createDelete(@NotNull SQLManager sqlManager) {
        return sqlManager.createDelete(this.getName());
    }

    public @NotNull InsertBuilder<PreparedSQLUpdateAction<Integer>> createInsert() {
        return this.createInsert(this.manager);
    }

    public @NotNull InsertBuilder<PreparedSQLUpdateAction<Integer>> createInsert(@NotNull SQLManager sqlManager) {
        return sqlManager.createInsert(this.getName());
    }

    public @NotNull InsertBuilder<PreparedSQLUpdateBatchAction<Integer>> createInsertBatch() {
        return this.createInsertBatch(this.manager);
    }

    public @NotNull InsertBuilder<PreparedSQLUpdateBatchAction<Integer>> createInsertBatch(@NotNull SQLManager sqlManager) {
        return sqlManager.createInsertBatch(this.getName());
    }

    public @NotNull TableQueryBuilder createQuery() {
        return this.createQuery(this.manager);
    }

    public @NotNull TableQueryBuilder createQuery(@NotNull SQLManager sqlManager) {
        return sqlManager.createQuery().inTable(this.getName());
    }

    public @NotNull ReplaceBuilder<PreparedSQLUpdateAction<Integer>> createReplace() {
        return this.createReplace(this.manager);
    }

    public @NotNull ReplaceBuilder<PreparedSQLUpdateAction<Integer>> createReplace(@NotNull SQLManager sqlManager) {
        return sqlManager.createReplace(this.getName());
    }

    public @NotNull ReplaceBuilder<PreparedSQLUpdateBatchAction<Integer>> createReplaceBatch() {
        return this.createReplaceBatch(this.manager);
    }

    public @NotNull ReplaceBuilder<PreparedSQLUpdateBatchAction<Integer>> createReplaceBatch(@NotNull SQLManager sqlManager) {
        return sqlManager.createReplaceBatch(this.getName());
    }

    public @NotNull UpdateBuilder createUpdate() {
        return this.createUpdate(this.manager);
    }

    public @NotNull UpdateBuilder createUpdate(@NotNull SQLManager sqlManager) {
        return sqlManager.createUpdate(this.getName());
    }

    public boolean isExists() {
        return isExists(this.manager);
    }

    public boolean isExists(@NotNull SQLManager manager) {
        boolean match = false;
        try {
            try (Connection connection = manager.getConnection(); ResultSet rs = connection.getMetaData().getTables(null, null, "%", null)) {
                while (rs.next()) {
                    if (getName().equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
                        match = true;
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            if (Util.isDevMode()) {
                e.printStackTrace();
            }
            Log.debug("Error while checking table existence: " + getName());
        }
        return match;
    }

    public boolean purgeTable() {
        return purgeTable(this.manager);
    }

    public boolean purgeTable(@NotNull SQLManager sqlManager) {
        try {
            sqlManager.createDelete(this.getName())
                    .addCondition("1=1")
                    .build().execute();
            return true;
        } catch (SQLException e) {
            Log.debug("Failed to purge table " + this.getName() + e);
            return false;
        }
    }
}
