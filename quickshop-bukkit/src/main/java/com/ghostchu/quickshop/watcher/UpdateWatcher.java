package com.ghostchu.quickshop.watcher;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.Random;

public class UpdateWatcher implements Listener {

  private final Random random = new Random();
  private final QuickShop plugin = QuickShop.getInstance();
  private WrappedTask cronTask = null;

  public void init() {

    cronTask = QuickShop.folia().getScheduler().runTimerAsync(()->{
      if(!plugin.updateManager().isLatest()) {
        plugin.logger().info("A new version of QuickShop has been released! [{}]", plugin.updateManager().getLatestVersion());
        plugin.logger().info("Update here: https://modrinth.com/plugin/quickshop-hikari");
        for(final Player player : Bukkit.getOnlinePlayers()) {
          if(plugin.perm().hasPermission(player, "quickshop.alerts")) {
            MsgUtil.sendDirectMessage(player, ChatColor.GREEN + "---------------------------------------------------");
            MsgUtil.sendDirectMessage(player, ChatColor.GREEN + LegacyComponentSerializer.legacySection().serialize(pickRandomMessage(player)));
            MsgUtil.sendDirectMessage(player, Component.text("https://modrinth.com/plugin/quickshop-hikari").color(NamedTextColor.AQUA).clickEvent(ClickEvent.openUrl("https://modrinth.com/plugin/quickshop-hikari")));
            MsgUtil.sendDirectMessage(player, ChatColor.GREEN + "---------------------------------------------------");
          }
        }
      }
    }, 1, 20L * 60L * 60L);
  }

  private Component pickRandomMessage(final CommandSender sender) {

    final List<Component> messages = plugin.text().ofList(sender, "updatenotify.list").forLocale();
    int notifyNum = -1;
    if(messages.size() > 1) {
      notifyNum = random.nextInt(messages.size());
    }
    final Component notify;
    if(notifyNum > 0) { // Translate bug.
      notify = messages.get(notifyNum);
    } else {
      notify = Component.text("New update {0} now available! Please update!");
    }
    return MsgUtil.fillArgs(notify, Component.text(plugin.updateManager().getLatestVersion()), Component.text(plugin.getVersion()));
  }

  @EventHandler
  public void playerJoin(final PlayerJoinEvent e) {

    Util.asyncThreadRun(()->{
      if(!plugin.perm().hasPermission(e.getPlayer(), "quickshop.alerts") || plugin.updateManager().isLatest()) {
        return;
      }
      MsgUtil.sendDirectMessage(e.getPlayer(), ChatColor.GREEN + "---------------------------------------------------");
      MsgUtil.sendDirectMessage(e.getPlayer(), ChatColor.GREEN + LegacyComponentSerializer.legacySection().serialize(pickRandomMessage(e.getPlayer())));
      MsgUtil.sendDirectMessage(e.getPlayer(), ChatColor.AQUA + " https://modrinth.com/plugin/quickshop-hikari");
      MsgUtil.sendDirectMessage(e.getPlayer(), ChatColor.GREEN + "---------------------------------------------------");
    });
  }

  public void uninit() {

    if(cronTask == null) {
      return;
    }
    cronTask.cancel();
  }
}
