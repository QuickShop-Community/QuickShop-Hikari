package com.ghostchu.quickshop.api.command;

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.shop.Shop;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * The command handler that processing sub commands under QS main command
 *
 * @param <T> The required sender class you want, must is the sub type of {@link CommandSender}
 */
public interface CommandHandler<T extends CommandSender> {

    /**
     * Getting the player now looking shop
     *
     * @return The shop that player looking or null if not found
     * @throws IllegalStateException if sender is not player
     */
    @Nullable
    default Shop getLookingShop(T sender) throws IllegalStateException {
        if (sender instanceof Player player) {
            BlockIterator bIt = new BlockIterator(player, 10);
            while (bIt.hasNext()) {
                final Block b = bIt.next();
                final Shop shop = QuickShopAPI.getInstance().getShopManager().getShop(b.getLocation());
                if (shop == null)
                    continue;
                return shop;
            }
            return null;
        }
        throw new IllegalStateException("Sender is not player");
    }

    /**
     * Calling while command executed by specified sender
     *
     * @param sender       The command sender but will automatically convert to specified instance
     * @param commandLabel The command prefix (/qs = qs, /shop = shop)
     * @param cmdArg       The arguments (/qs create stone will receive stone)
     */
    void onCommand(T sender, @NotNull String commandLabel, @NotNull String[] cmdArg);

    /**
     * Calling while sender trying to tab-complete
     *
     * @param sender       The command sender but will automatically convert to specified instance
     * @param commandLabel The command prefix (/qs = qs, /shop = shop)
     * @param cmdArg       The arguments (/qs create stone [TAB] will receive stone)
     * @return Candidate list
     */
    @Nullable
    default List<String> onTabComplete(@NotNull T sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return Collections.emptyList();
    }
}
