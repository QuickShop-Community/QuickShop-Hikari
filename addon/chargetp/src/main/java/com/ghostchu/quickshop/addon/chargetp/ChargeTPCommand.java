package com.ghostchu.quickshop.addon.chargetp;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ChargeTPCommand implements CommandHandler<CommandSender> {

  private final Main main;
  private final QuickShop quickshop;

  public ChargeTPCommand(final Main main, final QuickShop quickshop) {

    this.main = main;
    this.quickshop = quickshop;
  }

  @Override
  public void onCommand(final CommandSender sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if(!quickshop.perm().hasPermission(sender, "quickshopaddon.chargetp.reload")) {
      quickshop.text().of(sender, "no-permission").send();
      return;
    }
    if(parser.getArgs().size() != 1) {
      quickshop.text().of(sender, "command-incorrect", "/quickshop chargetp reload").send();
      return;
    }
    switch(parser.getArgs().getFirst().toLowerCase(Locale.ROOT)) {
      case "reload" -> reload(sender);
      default -> quickshop.text().of(sender, "command-incorrect", "/quickshop chargetp reload").send();
    }
  }

  private void reload(final CommandSender sender) {

    if(main.reloadSettings()) {
      sender.sendMessage("ChargeTP configuration reloaded.");
    } else {
      sender.sendMessage("Invalid configuration; previous settings retained, if available.");
    }
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final String commandLabel, @NotNull final String[] cmdArg) {

    if(cmdArg.length == 1 && quickshop.perm().hasPermission(sender, "quickshopaddon.chargetp.reload") && "reload".startsWith(cmdArg[0].toLowerCase(Locale.ROOT))) {
      return List.of("reload");
    }
    return Collections.emptyList();
  }
}
