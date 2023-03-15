package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class SubCommand_Amount implements CommandHandler<Player> {

    private final QuickShop plugin;

    public SubCommand_Amount(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        if (parser.getArgs().size() < 1) {
            plugin.text().of(sender, "command.wrong-args").send();
            return;
        }

        if (!plugin.getShopManager().getInteractiveManager().containsKey(sender.getUniqueId())) {
            plugin.text().of(sender, "no-pending-action").send();
            return;
        }

        plugin.getShopManager().handleChat(sender, parser.getArgs().get(0));
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
            @NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        return parser.getArgs().size() == 1 ? Collections.singletonList(LegacyComponentSerializer.legacySection().serialize(plugin.text().of(sender, "tabcomplete.amount").forLocale())) : Collections.emptyList();
    }

}
