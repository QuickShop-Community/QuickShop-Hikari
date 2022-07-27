package com.ghostchu.quickshop.command.subcommand.silent;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.logger.Log;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public abstract class SubCommand_SilentBase implements CommandHandler<Player> {
    protected final QuickShop plugin;

    @Override
    public void onCommand(Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length != 1) {
            Log.debug("Exception on command! Canceling!");
            return;
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(cmdArg[0]);
        } catch (IllegalArgumentException e) {
            //Not valid, return for doing nothing
            return;
        }

        Shop shop = plugin.getShopManager().getShopFromRuntimeRandomUniqueId(uuid);
        if (shop != null) {
            doSilentCommand(sender, shop, cmdArg);
        } else {
            plugin.text().of(sender, "not-looking-at-shop").send();
        }
    }

    protected abstract void doSilentCommand(Player sender, @NotNull Shop shop, @NotNull String[] cmdArg);

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return Collections.emptyList();
    }
}
