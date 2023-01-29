package com.ghostchu.quickshop.addon.discordsrv.command;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.discordsrv.Main;
import com.ghostchu.quickshop.addon.discordsrv.bean.NotifactionFeature;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
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

    public SubCommand_Discord(QuickShop qs, Main plugin) {
        this.qs = qs;
        this.plugin = plugin;
    }

    @Override
    public void onCommand(Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 2) {
            qs.text().of(sender, "command-incorrect", "/qs discord <features> <enable/disable>").send();
            return;
        }
        NotifactionFeature feature = getFeatureByName(cmdArg[0]);
        if (feature == null) {
            qs.text().of(sender, "command-incorrect", "/qs discord <features> <enable/disable>").send();
            return;
        }
        boolean ops = cmdArg[1].equalsIgnoreCase("enable");
        Util.asyncThreadRun(() -> {
            try {
                Integer i = plugin.getDatabaseHelper().setNotifactionFeatureEnabled(sender.getUniqueId(), feature, ops);
                Log.debug("Execute Discord Notifaction with id " + i + " affected");
                qs.text().of(sender, "addon.discord.feature-status-changed", feature.getConfigNode(), ops).send();
            } catch (SQLException e) {
                qs.text().of(sender, "addon.discord.save-notifaction-exception").send();
                plugin.getLogger().log(Level.WARNING, "Cannot save the player Discord notification feature status", e);
            }
        });
    }

    @Nullable
    private NotifactionFeature getFeatureByName(String name) {
        for (NotifactionFeature value : NotifactionFeature.values()) {
            if (value.name().equalsIgnoreCase(name))
                return value;
            if (value.getConfigNode().equalsIgnoreCase(name))
                return value;
            if (value.getDatabaseNode().equalsIgnoreCase(name))
                return value;
        }
        return null;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length == 1) {
            return Arrays.stream(NotifactionFeature.values()).map(NotifactionFeature::getConfigNode).toList();
        }
        if (cmdArg.length == 2) {
            return List.of("enable", "disable");
        }
        return Collections.emptyList();
    }
}
