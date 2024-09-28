package com.ghostchu.quickshop.addon.discordsrv.command;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.discordsrv.Main;
import com.ghostchu.quickshop.addon.discordsrv.bean.NotificationFeature;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class SubCommand_Discord implements CommandHandler<Player> {

  private final QuickShop qs;
  private final Main plugin;

  public SubCommand_Discord(final QuickShop qs, final Main plugin) {

    this.qs = qs;
    this.plugin = plugin;
  }

  @Override
  public void onCommand(final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if(parser.getArgs().size() < 2) {
      qs.text().of(sender, "command-incorrect", "/quickshop discord <features> <enable/disable>").send();
      return;
    }
    final NotificationFeature feature = getFeatureByName(parser.getArgs().get(0));
    if(feature == null) {
      qs.text().of(sender, "command-incorrect", "/quickshop discord <features> <enable/disable>").send();
      return;
    }
    final boolean ops = "enable".equalsIgnoreCase(parser.getArgs().get(1));
    Util.asyncThreadRun(()->{
      try {
        final Integer i = plugin.getDatabaseHelper().setNotifactionFeatureEnabled(QUserImpl.createFullFilled(sender), feature, ops);
        Log.debug("Execute Discord Notifaction with id " + i + " affected");
        final Component text = qs.text().of(sender, "addon.discord.message-type-mapping." + feature.getConfigNode(), feature.getConfigNode(), ops).forLocale();
        qs.text().of(sender, "addon.discord.feature-status-changed", text, ops).send();
      } catch(SQLException e) {
        qs.text().of(sender, "addon.discord.save-notifaction-exception").send();
        plugin.getLogger().log(Level.WARNING, "Cannot save the player Discord notification feature status", e);
      }
    });
  }

  @Nullable
  private NotificationFeature getFeatureByName(final String name) {

    for(final NotificationFeature value : NotificationFeature.values()) {
      NotificationFeature selected = null;
      if(value.name().equalsIgnoreCase(name)) {
        selected = value;
      } else if(value.getConfigNode().equalsIgnoreCase(name)) {
        selected = value;
      } else if(value.getDatabaseNode().equalsIgnoreCase(name)) {
        selected = value;
      }
      if(selected != null) {
        if(selected.isPlayerToggleable()) {
          return selected;
        }
      }
    }
    return null;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if(parser.getArgs().size() == 1) {
      return Arrays.stream(NotificationFeature.values())
              .filter(NotificationFeature::isPlayerToggleable)
              .map(NotificationFeature::getConfigNode)
              .toList();
    }
    if(parser.getArgs().size() == 2) {
      return List.of("enable", "disable");
    }
    return Collections.emptyList();
  }
}
