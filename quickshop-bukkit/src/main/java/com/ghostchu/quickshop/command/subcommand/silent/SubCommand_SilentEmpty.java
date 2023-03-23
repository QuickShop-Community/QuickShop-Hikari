package com.ghostchu.quickshop.command.subcommand.silent;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.shop.ContainerShop;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public class SubCommand_SilentEmpty extends SubCommand_SilentBase {

    public SubCommand_SilentEmpty(QuickShop plugin) {
        super(plugin);
    }

    @Override
    protected void doSilentCommand(Player sender, @NotNull Shop shop, @NotNull CommandParser parser) {
        if (!(shop instanceof final ContainerShop cs)) {
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
        }

        final InventoryWrapper inventory = cs.getInventory();

        if (inventory == null) {
            Log.debug("Inventory is empty! " + cs);
            return;
        }

        inventory.clear();
        shop.setSignText(plugin.text().findRelativeLanguages(sender));
        MsgUtil.sendControlPanelInfo(sender, shop);
        plugin.text().of(sender, "empty-success").send();
    }
}
