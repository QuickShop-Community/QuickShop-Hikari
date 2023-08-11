package com.ghostchu.quickshop.addon.displaycontrol.database;

import cc.carm.lib.easysql.api.SQLManager;
import cc.carm.lib.easysql.api.SQLQuery;
import com.ghostchu.quickshop.addon.displaycontrol.Main;
import com.ghostchu.quickshop.addon.displaycontrol.bean.DisplayOption;
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

    public @NotNull Integer setDisplayDisableForPlayer(@NotNull UUID uuid, DisplayOption status) throws SQLException {
        Util.ensureThread(true);
        try (SQLQuery query = DisplayControlTables.DISPLAY_CONTROL_PLAYERS.createQuery()
                .setLimit(1)
                .addCondition("player", uuid.toString())
                .build().execute();
             ResultSet set = query.getResultSet()) {
            if (set.next()) {
                return DisplayControlTables.DISPLAY_CONTROL_PLAYERS.createUpdate()
                        .setLimit(1)
                        .addCondition("player", uuid.toString())
                        .setColumnValues("displayOption", status.getId())
                        .build().execute();
            } else {
                return DisplayControlTables.DISPLAY_CONTROL_PLAYERS.createInsert()
                        .setColumnNames("player", "displayOption")
                        .setParams(uuid.toString(), status.getId())
                        .returnGeneratedKey()
                        .execute();
            }
        }

    }

    @Nullable
    public DisplayOption getDisplayOption(@NotNull UUID player) throws SQLException {
        Util.ensureThread(true);
        try (SQLQuery query = DisplayControlTables.DISPLAY_CONTROL_PLAYERS
                .createQuery()
                .selectColumns("displayOption")
                .addCondition("player", player.toString()).setLimit(1).build().execute();
             ResultSet set = query.getResultSet()) {
            if (set.next()) {
                int optionId = set.getInt("displayOption");
                return DisplayOption.fromId(optionId);
            }
            return DisplayOption.AUTO;
        }
    }
}
