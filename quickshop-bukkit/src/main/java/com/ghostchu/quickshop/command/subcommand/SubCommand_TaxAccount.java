package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.Util;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.enginehub.squirrelid.Profile;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class SubCommand_TaxAccount implements CommandHandler<Player> {

    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        final Shop shop = getLookingShop(sender);
        if (shop != null) {
            if (cmdArg.length < 1) {
                shop.setTaxAccount(null);
                plugin.text().of(sender, "taxaccount-unset").send();
                return;
            }
            if (Util.isUUID(cmdArg[0])) {
                shop.setTaxAccount(UUID.fromString(cmdArg[0]));
            } else {
                Profile profile = plugin.getPlayerFinder().find(cmdArg[0]);
                if (profile == null) {
                    plugin.text().of(sender, "unknown-player").send();
                    return;
                }
                shop.setTaxAccount(profile.getUniqueId());
            }
            plugin.text().of(sender, "taxaccount-set", cmdArg[0]).send();
        } else {
            plugin.text().of(sender, "not-looking-at-shop").send();
        }
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
            @NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return null;
    }

}
