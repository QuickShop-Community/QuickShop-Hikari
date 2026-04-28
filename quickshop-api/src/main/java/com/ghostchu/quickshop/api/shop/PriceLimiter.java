package com.ghostchu.quickshop.api.shop;

import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.limit.RuleSet;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Utility used for shop price validating
 */
public interface PriceLimiter<P> {

  /**
   * Finds the set of rule sets that are applicable to the provided ItemStack.
   *
   * @param stack the ItemStack to evaluate; must not be null
   * @return a set of RuleSet objects applicable to the provided ItemStack
   */
  Set<RuleSet<P>> findApplicableRules(@NotNull ItemStack stack);

  /**
   * Determines the set of rule sets applicable to the specified command sender
   * and item stack. The method evaluates the provided CommandSender and ItemStack
   * against the conditions defined within the rule sets and only includes those
   * rule sets that are relevant.
   *
   * @param sender the command sender to be evaluated; must not be null
   * @param stack  the item stack to be evaluated; must not be null
   * @return a set of RuleSet objects that are applicable based on the provided
   *         CommandSender and ItemStack
   */
  Set<RuleSet<P>> findApplicableRules(@NotNull CommandSender sender, @NotNull ItemStack stack);

  /**
   * Determines the set of rule sets that are applicable to the provided `ItemStack` for a given `QUser`.
   *
   * @param user  the QUser to evaluate; must not be null
   * @param stack the ItemStack to evaluate; must not be null
   * @return a set of RuleSet objects that are applicable to the given QUser and ItemStack
   */
  Set<RuleSet<P>> findApplicableRules(@NotNull QUser user, @NotNull ItemStack stack);

  /**
   * Determines the set of rule sets that are applicable to the provided ItemStack and currency.
   *
   * @param stack the ItemStack to evaluate; must not be null
   * @param currency the currency to evaluate; must not be null
   * @return a set of RuleSet objects that are applicable to the given ItemStack and currency
   */
  Set<RuleSet<P>> findApplicableRules(@NotNull ItemStack stack, @NotNull String currency);

  /**
   * Determines the set of rule sets that are applicable based on the provided
   * command sender, item stack, and currency. This method evaluates the given
   * parameters against the conditions defined in the rule sets and returns the
   * relevant ones.
   *
   * @param sender   the command sender to evaluate; must not be null
   * @param stack    the item stack to evaluate; must not be null
   * @param currency the currency to evaluate; must not be null
   * @return a set of RuleSet objects applicable to the given CommandSender, ItemStack, and currency
   */
  Set<RuleSet<P>> findApplicableRules(@NotNull CommandSender sender, @NotNull ItemStack stack, @NotNull String currency);

  /**
   * Determines the set of rule sets that are applicable to the provided user, item stack, and currency.
   * The method evaluates the given QUser, ItemStack, and currency string against the conditions defined
   * in the rule sets, returning only those rule sets that are relevant.
   *
   * @param user     the QUser to evaluate; must not be null
   * @param stack    the ItemStack to evaluate; must not be null
   * @param currency the currency to evaluate; must not be null
   * @return a set of RuleSet objects that are applicable to the given QUser, ItemStack, and currency
   */
  Set<RuleSet<P>> findApplicableRules(@NotNull QUser user, @NotNull ItemStack stack, @NotNull String currency);

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
