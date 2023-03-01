package com.ghostchu.quickshop.addon.discordsrv.database;

import cc.carm.lib.easysql.api.SQLManager;
import cc.carm.lib.easysql.api.SQLQuery;
import com.ghostchu.quickshop.addon.discordsrv.Main;
import com.ghostchu.quickshop.addon.discordsrv.bean.NotificationFeature;
import com.ghostchu.quickshop.addon.discordsrv.bean.NotificationSettings;
import com.ghostchu.quickshop.common.util.JsonUtil;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class DiscordDatabaseHelper {
    private final Main plugin;

    public DiscordDatabaseHelper(@NotNull Main plugin, @NotNull SQLManager sqlManager, @NotNull String dbPrefix) throws SQLException {
        this.plugin = plugin;
        try {
            DiscordTables.initializeTables(sqlManager, dbPrefix);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Cannot initialize tables", e);
            throw e;
        }

    }

    public @NotNull Integer setNotifactionFeatureEnabled(@NotNull UUID uuid, @NotNull NotificationFeature feature, @Nullable Boolean status) throws SQLException {
        Util.ensureThread(true);
        NotificationSettings settings = getPlayerNotifactionSetting(uuid);
        if (status == null) {
            settings.getSettings().remove(feature);
        } else {
            settings.getSettings().put(feature, status);
        }
        try (ResultSet set = DiscordTables.DISCORD_PLAYERS.createQuery()
                .setLimit(1)
                .addCondition("player", uuid.toString())
                .build().execute().getResultSet()) {
            if (set.next()) {
                return DiscordTables.DISCORD_PLAYERS.createUpdate()
                        .setLimit(1)
                        .addCondition("player", uuid.toString())
                        .setColumnValues("notifaction", JsonUtil.getGson().toJson(settings))
                        .build().execute();
            } else {
                return DiscordTables.DISCORD_PLAYERS.createInsert()
                        .setColumnNames("player", "notifaction")
                        .setParams(uuid.toString(), JsonUtil.getGson().toJson(settings))
                        .returnGeneratedKey()
                        .execute();
            }
        }

    }

    @NotNull
    public NotificationSettings getPlayerNotifactionSetting(@NotNull UUID player) throws SQLException {
        Util.ensureThread(true);
        try (SQLQuery query = DiscordTables.DISCORD_PLAYERS.createQuery().selectColumns("notifaction").addCondition("player", player.toString()).setLimit(1).build().execute(); ResultSet set = query.getResultSet()) {
            if (set.next()) {
                String json = set.getString("notifaction");
                Log.debug("Json data: " + json);
                if (StringUtils.isNotEmpty(json)) {
                    if (MsgUtil.isJson(json)) {
                        return JsonUtil.getGson().fromJson(json, NotificationSettings.class);
                    }
                }
            }
            Log.debug("Generating default value...");
            Map<NotificationFeature, Boolean> booleanMap = new HashMap<>();
            for (NotificationFeature feature : NotificationFeature.values()) {
                booleanMap.put(feature, plugin.isServerNotificationFeatureEnabled(feature));
            }
            return new NotificationSettings(booleanMap);
        }
    }

    @SuppressWarnings("ConstantValue")
    public boolean isNotifactionFeatureEnabled(@NotNull UUID uuid, @NotNull NotificationFeature feature) {
        Util.ensureThread(true);
        boolean defValue = plugin.isServerNotificationFeatureEnabled(feature);
        if (!defValue) return false; // If server disabled it, do not send it
        try {
            NotificationSettings settings = getPlayerNotifactionSetting(uuid);
            Log.debug("Notifaction Settings: " + settings);
            return settings.getSettings().getOrDefault(feature, defValue);
        } catch (SQLException e) {
            return defValue;
        }
    }


}
