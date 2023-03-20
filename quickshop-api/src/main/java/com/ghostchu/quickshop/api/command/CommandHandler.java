package com.ghostchu.quickshop.api.command;

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.shop.Shop;
import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
                if (shop == null) {
                    continue;
                }
                return shop;
            }
            return null;
        }
        throw new IllegalStateException("Sender is not player");
    }

    /**
     * Getting the shops by ids
     * @param ids The shop ids
     * @return The shops
     */
    @Nullable
    default Map<Long, Shop> getShopsByIds(List<Long> ids){
        Map<Long, Shop> shops = new HashMap<>();
        for (Long id : ids) {
           shops.put(id, QuickShopAPI.getInstance().getShopManager().getShop(id));
        }
        return shops;
    }

    default void onCommand_Internal(T sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        StringJoiner joiner = new StringJoiner(" ");
        for (String s : cmdArg) {
            joiner.add(s);
        }
        CommandParser parser = new CommandParser(joiner.toString());
        try{
            onCommand(sender, commandLabel, parser);
        }catch (NotImplementedException e){
            onCommand(sender, commandLabel, parser.getArgs().toArray(new String[0]));
        }
    }

    /**
     * Calling while command executed by specified sender
     *
     * @param sender       The command sender but will automatically convert to specified instance
     * @param commandLabel The command prefix (/qs = qs, /shop = shop)
     * @param parser       The command parser which include arguments and colon arguments
     */
    default void onCommand(T sender, @NotNull String commandLabel, @NotNull CommandParser parser){
    }
    /**
     * Calling while command executed by specified sender
     *
     * @param sender       The command sender but will automatically convert to specified instance
     * @param commandLabel The command prefix (/qs = qs, /shop = shop)
     * @param cmdArgs      The command arguments
     * @deprecated This method is deprecated, please use {@link #onCommand(T, String, CommandParser)} instead.
     */
    @Deprecated(since = "4.2.0.0")
    default void onCommand(T sender, @NotNull String commandLabel, @NotNull String[] cmdArgs){
        throw new NotImplementedException("This method is deprecated, please use onCommand(T sender, @NotNull String commandLabel, @NotNull CommandParser parser) instead.");
    }

    @Nullable
    default List<String> onTabComplete_Internal(@NotNull T sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        StringJoiner joiner = new StringJoiner(" ");
        for (String s : cmdArg) {
            joiner.add(s);
        }
        CommandParser parser = new CommandParser(joiner.toString());
        return onTabComplete(sender, commandLabel, parser);
    }

    /**
     * Calling while sender trying to tab-complete
     *
     * @param sender       The command sender but will automatically convert to specified instance
     * @param commandLabel The command prefix (/qs = qs, /shop = shop)
     * @param parser       The command parser which include arguments and colon arguments
     * @return Candidate list
     */
    @Nullable
    default List<String> onTabComplete(@NotNull T sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        return Collections.emptyList();
    }
    /**
     * Calling while sender trying to tab-complete
     *
     * @param sender       The command sender but will automatically convert to specified instance
     * @param commandLabel The command prefix (/qs = qs, /shop = shop)
     * @param cmdArgs      The command arguments
     * @return Candidate list
     * @deprecated This method is deprecated, please use {@link #onTabComplete(T, String, CommandParser)} instead.
     */
    @Deprecated(since = "4.2.0.0")
    default List<String> onTabComplete(@NotNull T sender, @NotNull String commandLabel, @NotNull String[] cmdArgs) {
        throw new NotImplementedException("This method is deprecated, please use onTabComplete(T sender, @NotNull String commandLabel, @NotNull CommandParser parser) instead.");
    }
}
