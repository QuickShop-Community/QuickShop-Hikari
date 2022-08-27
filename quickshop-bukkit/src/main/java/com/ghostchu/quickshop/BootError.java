package com.ghostchu.quickshop;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * BootError class contains print errors on /qs command when plugin failed launched.
 *
 * @author Ghost_chu
 */
@EqualsAndHashCode
@ToString
public class BootError {

    private final String[] errors;

    public BootError(@NotNull Logger logger, @NotNull String... errors) {
        this.errors = errors;
        for (String err : errors) {
            logger.severe(err);
        }
    }

    public String[] getErrors() {
        return Arrays.copyOf(errors, errors.length);
    }

    /**
     * Print the errors. ##################################################### QuickShop is disabled,
     * Please fix errors and restart ..........................
     * #################################################### This one.
     *
     * @param sender The sender you want output the errors.
     */
    public void printErrors(CommandSender sender) { //Do not use any method that not in CraftBukkit
        sender.sendMessage(ChatColor.RED + "#####################################################");
        sender.sendMessage(ChatColor.RED + " QuickShop is disabled, Please fix any errors and restart");
        for (String issue : errors) {
            sender.sendMessage(ChatColor.WHITE + "- " + ChatColor.YELLOW + issue);
        }
        sender.sendMessage(ChatColor.RED + "#####################################################");
    }
}
