package com.ghostchu.quickshop.compatibility.openinv;

import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.shop.inventory.BukkitInventoryWrapper;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OpenInvCommand implements CommandHandler<Player> {
    private final Openinv plugin;

    public OpenInvCommand(Openinv openinv) {
        this.plugin = openinv;
    }

    /**
     * Calling while command executed by specified sender
     *
     * @param sender       The command sender but will automatically convert to specified instance
     * @param commandLabel The command prefix (/qs = qs, /shop = shop)
     * @param cmdArg       The arguments (/qs create stone will receive stone)
     */
    @Override
    public void onCommand(Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        BlockIterator bIt = new BlockIterator(sender, 10);
        while (bIt.hasNext()) {
            final Block b = bIt.next();
            final Shop shop = plugin.getApi().getShopManager().getShop(b.getLocation());

            if (shop == null) {
                continue;
            }
            if (!shop.getOwner().equals(sender.getUniqueId()) && !sender.hasPermission("quickshop.admin")) {
                plugin.getApi().getTextManager().of(sender, "no-permission").send();
                return;
            }
            if (shop.getInventory() instanceof EnderChestWrapper) {
                shop.setInventory(new BukkitInventoryWrapper((((InventoryHolder) shop.getLocation().getBlock().getState()).getInventory())), plugin.getApi().getInventoryWrapperRegistry().get("QuickShop-Hikari"));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.to-chest")));
            } else {
                shop.setInventory(new EnderChestWrapper(shop.getOwner(), plugin.getOpenInv(), plugin), plugin.getManager());
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.to-echest")));
            }
            return;
        }
        plugin.getApi().getTextManager().of(sender, "not-looking-at-shop").send();
    }

    /**
     * Calling while sender trying to tab-complete
     *
     * @param sender       The command sender but will automatically convert to specified instance
     * @param commandLabel The command prefix (/qs = qs, /shop = shop)
     * @param cmdArg       The arguments (/qs create stone [TAB] will receive stone)
     * @return Candidate list
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return CommandHandler.super.onTabComplete(sender, commandLabel, cmdArg);
    }
}
