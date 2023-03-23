package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.ShopAction;
import com.ghostchu.quickshop.shop.SimpleInfo;
import com.ghostchu.quickshop.util.Util;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class SubCommand_SuperCreate implements CommandHandler<Player> {

    private final QuickShop plugin;

    public SubCommand_SuperCreate(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        ItemStack item = sender.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            plugin.text().of(sender, "no-anythings-in-your-hand").send();
            return;
        }

        final BlockIterator bIt = new BlockIterator(sender, 10);

        while (bIt.hasNext()) {
            final Block b = bIt.next();

            if (!Util.canBeShop(b)) {
                continue;
            }

            // Send creation menu.
            final SimpleInfo info = new SimpleInfo(b.getLocation(), ShopAction.CREATE_SELL, sender.getInventory().getItemInMainHand(), b.getRelative(sender.getFacing().getOppositeFace()), true);

            plugin.getShopManager().getInteractiveManager().put(sender.getUniqueId(), info);
            plugin.text().of(sender, "how-much-to-trade-for",Util.getItemStackName(info.getItem()), plugin.isAllowStack() && plugin.perm().hasPermission(sender, "quickshop.create.stacks") ? item.getAmount() : 1).send();
            return;
        }
        plugin.text().of(sender, "not-looking-at-shop").send();
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
            @NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        return parser.getArgs().size() == 1 ? Collections.singletonList(LegacyComponentSerializer.legacySection().serialize(plugin.text().of(sender, "tabcomplete.amount").forLocale())) : Collections.emptyList();
    }

}
