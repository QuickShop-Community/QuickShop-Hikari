package com.ghostchu.quickshop.command.subcommand.silent;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public abstract class SubCommand_SilentBase implements CommandHandler<Player> {
    protected final QuickShop plugin;

    protected SubCommand_SilentBase(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        if (parser.getArgs().size() != 1) {
            Log.debug("Exception on command! Canceling!");
            return;
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(parser.getArgs().get(0));
        } catch (IllegalArgumentException e) {
            //Not valid, return for doing nothing
            return;
        }

        Shop shop = plugin.getShopManager().getShopFromRuntimeRandomUniqueId(uuid);
        if (shop != null) {
            doSilentCommand(sender, shop, parser);
        } else {
            plugin.text().of(sender, "not-looking-at-shop").send();
        }
    }

    protected abstract void doSilentCommand(Player sender, @NotNull Shop shop, @NotNull CommandParser parser);

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        return Collections.emptyList();
    }
}
