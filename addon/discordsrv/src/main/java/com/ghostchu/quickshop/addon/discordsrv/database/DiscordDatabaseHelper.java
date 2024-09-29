package com.ghostchu.quickshop.addon.discordsrv.database;

import cc.carm.lib.easysql.api.SQLManager;
import cc.carm.lib.easysql.api.SQLQuery;
import com.ghostchu.quickshop.addon.discordsrv.Main;
import com.ghostchu.quickshop.addon.discordsrv.bean.NotificationFeature;
import com.ghostchu.quickshop.addon.discordsrv.bean.NotificationSettings;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.common.util.JsonUtil;
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

  public DiscordDatabaseHelper(@NotNull final Main plugin, @NotNull final SQLManager sqlManager, @NotNull final String dbPrefix) throws SQLException {

    this.plugin = plugin;
    try {
      DiscordTables.initializeTables(sqlManager, dbPrefix);
    } catch(SQLException e) {
      plugin.getLogger().log(Level.WARNING, "Cannot initialize tables", e);
      throw e;
    }

  }

  public @NotNull Integer setNotifactionFeatureEnabled(@NotNull final QUser qUser, @NotNull final NotificationFeature feature, @Nullable final Boolean status) throws SQLException {

    Util.ensureThread(true);
    final UUID playerUuid = qUser.getUniqueIdIfRealPlayer().orElse(null);
    if(playerUuid == null) {
      return 0;
    }
    final NotificationSettings settings = getPlayerNotifactionSetting(playerUuid);
    if(status == null) {
      settings.getSettings().remove(feature);
    } else {
      settings.getSettings().put(feature, status);
    }
    try(SQLQuery query = DiscordTables.DISCORD_PLAYERS.createQuery()
            .setLimit(1)
            .addCondition("player", playerUuid)
            .build().execute();
        ResultSet set = query.getResultSet()) {
      final Integer integer;
      if(set.next()) {
        integer = DiscordTables.DISCORD_PLAYERS.createUpdate()
                .setLimit(1)
                .addCondition("player", playerUuid)
                .setColumnValues("notifaction", JsonUtil.getGson().toJson(settings))
                .build().execute();
      } else {
        integer = DiscordTables.DISCORD_PLAYERS.createInsert()
                .setColumnNames("player", "notifaction")
                .setParams(playerUuid, JsonUtil.getGson().toJson(settings))
                .returnGeneratedKey()
                .execute();
      }
      return integer;
    }
  }

  @NotNull
  public NotificationSettings getPlayerNotifactionSetting(@NotNull final UUID uuid) throws SQLException {

    Util.ensureThread(true);
    try(SQLQuery query = DiscordTables.DISCORD_PLAYERS.createQuery().selectColumns("notifaction").addCondition("player",
                                                                                                               uuid).setLimit(1).build().execute(); ResultSet set = query.getResultSet()) {
      if(set.next()) {
        final String json = set.getString("notifaction");
        Log.debug("Json data: " + json);
        if(StringUtils.isNotEmpty(json)) {
          if(CommonUtil.isJson(json)) {
            return JsonUtil.getGson().fromJson(json, NotificationSettings.class);
          }
        }
      }
      Log.debug("Generating default value...");
      final Map<NotificationFeature, Boolean> booleanMap = new HashMap<>();
      for(final NotificationFeature feature : NotificationFeature.values()) {
        booleanMap.put(feature, plugin.isServerNotificationFeatureEnabled(feature));
      }
      return new NotificationSettings(booleanMap);
    }
  }

  @SuppressWarnings("ConstantValue")
  public boolean isNotifactionFeatureEnabled(@NotNull final UUID uuid, @NotNull final NotificationFeature feature) {

    Util.ensureThread(true);
    final boolean defValue = plugin.isServerNotificationFeatureEnabled(feature);
    if(!defValue) {
      return false; // If server disabled it, do not send it
    }
    try {
      final NotificationSettings settings = getPlayerNotifactionSetting(uuid);
      Log.debug("Notifaction Settings: " + settings);
      return settings.getSettings().getOrDefault(feature, defValue);
    } catch(SQLException e) {
      return defValue;
    }
  }

}
