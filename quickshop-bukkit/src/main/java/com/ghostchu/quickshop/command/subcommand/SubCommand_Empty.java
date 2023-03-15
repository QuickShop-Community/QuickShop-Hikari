package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.shop.ContainerShop;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SubCommand_Empty implements CommandHandler<Player> {

    private final QuickShop plugin;

    public SubCommand_Empty(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        final Shop shop = getLookingShop(sender);
        if (shop instanceof final ContainerShop cs) {
            final InventoryWrapper inventory = cs.getInventory();
            if (inventory == null) {
                return;
            }
            cs.getInventory().clear();
            plugin.text().of(sender, "empty-success").send();
        } else {
            plugin.text().of(sender, "not-looking-at-shop").send();
        }
    }


}
