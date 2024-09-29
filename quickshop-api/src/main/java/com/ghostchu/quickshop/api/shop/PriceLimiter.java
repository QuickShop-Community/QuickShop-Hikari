package com.ghostchu.quickshop.api.shop;

import com.ghostchu.quickshop.api.obj.QUser;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility used for shop price validating
 */
public interface PriceLimiter {

  /**
   * Check the price restriction rules
   *
   * @param sender   the sender
   * @param stack    the item to check
   * @param currency the currency
   * @param price    the price
   *
   * @return the result
   */
  @NotNull
  PriceLimiterCheckResult check(@NotNull CommandSender sender, @NotNull ItemStack stack, @Nullable String currency, double price);

  /**
   * Check the price restriction rules
   *
   * @param user     the user
   * @param stack    the item to check
   * @param currency the currency
   * @param price    the price
   *
   * @return the result
   */
  @NotNull
  PriceLimiterCheckResult check(@NotNull QUser user, @NotNull ItemStack stack, @Nullable String currency, double price);
}
