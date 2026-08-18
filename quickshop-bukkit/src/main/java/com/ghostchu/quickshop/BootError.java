package com.ghostchu.quickshop;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Arrays;

/**
 * BootError class contains print errors on /quickshop command when plugin failed launched.
 *
 * @author Ghost_chu
 */
public class BootError {

  private final String[] errors;

  public BootError(@NotNull final Logger logger, @NotNull final String... errors) {

    this.errors = errors;
    for(final String err : errors) {
      logger.error(err);
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
  public void printErrors(final CommandSender sender) { //Do not use any method that not in CraftBukkit
    sender.sendMessage(ChatColor.RED + "#####################################################");
    sender.sendMessage(ChatColor.RED + " QuickShop is disabled, Please fix any errors and restart");
    for(final String issue : errors) {
      sender.sendMessage(ChatColor.WHITE + "- " + ChatColor.YELLOW + issue);
    }
    sender.sendMessage(ChatColor.RED + "#####################################################");
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof BootError)) return false;
    final BootError other = (BootError)o;
    if(!other.canEqual((Object)this)) return false;
    if(!java.util.Arrays.deepEquals(this.getErrors(), other.getErrors())) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {

    return other instanceof BootError;
  }

  @Override
  public int hashCode() {

    final int PRIME = 59;
    int result = 1;
    result = result * PRIME + java.util.Arrays.deepHashCode(this.getErrors());
    return result;
  }

  @Override
  public String toString() {

    return "BootError(errors=" + java.util.Arrays.deepToString(this.getErrors()) + ")";
  }
}
