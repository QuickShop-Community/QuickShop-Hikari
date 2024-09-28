package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.common.util.JsonUtil;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class SubCommand_About implements CommandHandler<CommandSender> {

  private final QuickShop plugin;

  public SubCommand_About(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final CommandSender sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    final String forkName = plugin.getFork();
    final String version = plugin.getVersion();
    final Component releaseType;
    if(plugin.getBuildInfo().getGitInfo().getBranch().toUpperCase().contains("ORIGIN/LTS")) {
      releaseType = plugin.text().of(sender, "updatenotify.label.lts").forLocale();
    } else if(plugin.getBuildInfo().getGitInfo().getBranch().toUpperCase().contains("ORIGIN/RELEASE")) {
      releaseType = plugin.text().of(sender, "updatenotify.label.stable").forLocale();
    } else {
      releaseType = plugin.text().of(sender, "updatenotify.label.unstable").forLocale();
    }
    final String developers = CommonUtil.list2String(plugin.getJavaPlugin().getDescription().getAuthors());
    final String languageCode = plugin.text().findRelativeLanguages(sender).getLocale();
    final Component localizedStaffs = plugin.text().of(sender, "translation-author").forLocale();
    final Component donationKey = plugin.text().of(sender, "about.invalid-donation-key").forLocale();
    plugin.text().ofList(sender, "about.text", forkName, version, releaseType, developers, languageCode, localizedStaffs, donationKey).send();

  }
}