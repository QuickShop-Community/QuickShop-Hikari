package com.ghostchu.quickshop.api.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

//Preserved for backward compatibility
@Deprecated
public interface CommandProcesser extends CommandHandler<CommandSender> {

  @Override
  void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg);

  @Override
  @Nullable
  default List<String> onTabComplete(
          @NotNull final CommandSender sender, @NotNull final String commandLabel, @NotNull final String[] cmdArg) {

    return Collections.emptyList();
  }

}
