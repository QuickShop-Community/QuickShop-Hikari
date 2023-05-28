package com.ghostchu.quickshop.addon.displaycontrol.database;

import cc.carm.lib.easysql.api.SQLManager;
import cc.carm.lib.easysql.api.SQLQuery;
import com.ghostchu.quickshop.addon.displaycontrol.Main;
import com.ghostchu.quickshop.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class DisplayControlDatabaseHelper {
    private final Main plugin;

    public DisplayControlDatabaseHelper(@NotNull Main plugin, @NotNull SQLManager sqlManager, @NotNull String dbPrefix) throws SQLException {
        this.plugin = plugin;
        try {
            DisplayControlTables.initializeTables(sqlManager, dbPrefix);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Cannot initialize tables", e);
            throw e;
        }

    }

    public @NotNull Integer setDisplayStatusForPlayer(@NotNull UUID uuid, @Nullable Boolean status) throws SQLException {
        Util.ensureThread(true);
        try (ResultSet set = DisplayControlTables.DISPLAY_CONTROL_PLAYERS.createQuery()
                .setLimit(1)
                .addCondition("player", uuid.toString())
                .build().execute().getResultSet()) {
            if (set.next()) {
                return DisplayControlTables.DISPLAY_CONTROL_PLAYERS.createUpdate()
                        .setLimit(1)
                        .addCondition("player", uuid.toString())
                        .setColumnValues("disableDisplay", status)
                        .build().execute();
            } else {
                return DisplayControlTables.DISPLAY_CONTROL_PLAYERS.createInsert()
                        .setColumnNames("player", "disableDisplay")
                        .setParams(uuid.toString(), status)
                        .returnGeneratedKey()
                        .execute();
            }
        }

    }

    @Nullable
    public Boolean getDisplayStatusForPlayer(@NotNull UUID player) throws SQLException {
        Util.ensureThread(true);
        try (SQLQuery query = DisplayControlTables.DISPLAY_CONTROL_PLAYERS
                .createQuery()
                .selectColumns("disableDisplay")
                .addCondition("player", player.toString()).setLimit(1).build().execute();
             ResultSet set = query.getResultSet()) {
            if (set.next()) {
                return set.getBoolean("disableDisplay");
            }
            return null;
        }
    }
}
