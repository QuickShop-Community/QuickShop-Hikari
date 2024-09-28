package com.ghostchu.quickshop.addon.displaycontrol.command;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.displaycontrol.Main;
import com.ghostchu.quickshop.addon.displaycontrol.bean.DisplayOption;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class SubCommand_DisplayControl implements CommandHandler<Player> {

  private final QuickShop qs;
  private final Main plugin;

  public SubCommand_DisplayControl(final QuickShop qs, final Main plugin) {

    this.qs = qs;
    this.plugin = plugin;
  }

  @Override
  public void onCommand(final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if(parser.getArgs().isEmpty()) {
      qs.text().of(sender, "command-incorrect", "/quickshop displaycontrol <auto/enable/disable>").send();
      return;
    }
    DisplayOption option = DisplayOption.AUTO;
    final String userInput = parser.getArgs().get(0).trim();
    if("enable".equalsIgnoreCase(userInput)) {
      option = DisplayOption.ENABLED;
    }
    if("disable".equalsIgnoreCase(userInput)) {
      option = DisplayOption.DISABLED;
    }
    final DisplayOption optionFinCopy = option;
    Util.asyncThreadRun(()->{
      try {
        final Integer i = plugin.getDatabaseHelper().setDisplayDisableForPlayer(sender.getUniqueId(), optionFinCopy);
        Log.debug("Execute DisplayToggle with id " + i + " affected");
        qs.text().of(sender, "addon.displaycontrol.toggle", optionFinCopy.name()).send();
      } catch(SQLException e) {
        qs.text().of(sender, "addon.displaycontrol.toggle-exception").send();
        plugin.getLogger().log(Level.WARNING, "Cannot save the player display status", e);
      }
    });
  }


  @Override
  public @Nullable List<String> onTabComplete(@NotNull final Player sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if(parser.getArgs().size() == 1) {
      return List.of("enable", "disable");
    }
    return Collections.emptyList();
  }
}
