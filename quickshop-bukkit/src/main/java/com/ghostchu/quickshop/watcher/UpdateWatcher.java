package com.ghostchu.quickshop.watcher;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Random;

public class UpdateWatcher implements Listener {
    private final Random random = new Random();
    private final QuickShop plugin = QuickShop.getInstance();
    private BukkitTask cronTask = null;

    public void init() {
        cronTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (!plugin.getNexusManager().isLatest()) {
                plugin.getLogger().info("A new version of QuickShop has been released! [" + plugin.getNexusManager().getLatestVersion() + "]");
                plugin.getLogger().info("Update here: https://www.spigotmc.org/resources/100125/");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (plugin.perm().hasPermission(player, "quickshop.alerts")) {
                        MsgUtil.sendDirectMessage(player, ChatColor.GREEN + "---------------------------------------------------");
                        MsgUtil.sendDirectMessage(player, ChatColor.GREEN + LegacyComponentSerializer.legacySection().serialize(pickRandomMessage(player)));
                        MsgUtil.sendDirectMessage(player, ChatColor.GREEN + "Type command " + ChatColor.YELLOW + "/qs update" + ChatColor.GREEN + " or click the link below to update QuickShop :)");
                        MsgUtil.sendDirectMessage(player, ChatColor.AQUA + " https://www.spigotmc.org/resources/100125/");
                        MsgUtil.sendDirectMessage(player, ChatColor.GREEN + "---------------------------------------------------");
                    }
                }
            }
        }, 1, 20 * 60 * 60);
    }

    private Component pickRandomMessage(CommandSender sender) {
        List<Component> messages = plugin.text().ofList(sender, "updatenotify.list").forLocale();
        int notifyNum = -1;
        if (messages.size() > 1) {
            notifyNum = random.nextInt(messages.size());
        }
        Component notify;
        if (notifyNum > 0) { // Translate bug.
            notify = messages.get(notifyNum);
        } else {
            notify = Component.text("New update {0} now available! Please update!");
        }
        return MsgUtil.fillArgs(notify, Component.text(plugin.getNexusManager().getLatestVersion()), Component.text(plugin.getDescription().getVersion()));
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {
        Util.asyncThreadRun(() -> {
            if (!plugin.perm().hasPermission(e.getPlayer(), "quickshop.alerts") || plugin.getNexusManager().isLatest()) {
                return;
            }
            MsgUtil.sendDirectMessage(e.getPlayer(), ChatColor.GREEN + "---------------------------------------------------");
            MsgUtil.sendDirectMessage(e.getPlayer(), ChatColor.GREEN + LegacyComponentSerializer.legacySection().serialize(pickRandomMessage(e.getPlayer())));
            MsgUtil.sendDirectMessage(e.getPlayer(), ChatColor.AQUA + " https://www.spigotmc.org/resources/100125/");
            MsgUtil.sendDirectMessage(e.getPlayer(), ChatColor.GREEN + "---------------------------------------------------");
        });
    }

    public void uninit() {
        if (cronTask == null) {
            return;
        }
        cronTask.cancel();
    }
}
