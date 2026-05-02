package com.ghostchu.quickshop.api.shop.limit;

import com.ghostchu.quickshop.api.obj.QUser;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;


public interface RuleSet<P> {

  /**
   * Retrieves a list of functions that evaluate a given ItemStack against a boolean condition.
   *
   * @return a list of functions where each function accepts an ItemStack and returns a boolean.
   */
  List<Function<ItemStack, Boolean>> items();

  /**
   * Retrieves a list of patterns representing currencies.
   *
   * @return a list of Pattern objects defining currency-related parameters.
   */
  List<Pattern> currencies();

  /**
   * Retrieves the permission string required to bypass the associated rule set.
   *
   * @return a string representing the permission required to bypass the rule set.
   */
  String bypassPermission();

  /**
   * Retrieves the minimum price defined in the rule set.
   *
   * @return the minimum price as an object of type P.
   */
  P minPrice();

  /**
   * Retrieves the maximum price defined in the rule set.
   *
   * @return the maximum price as an object of type P.
   */
  P maxPrice();

  /**
   * Determines whether a minimum price is set for this rule.
   *
   * @return true if a minimum price is defined and is greater than 0, false otherwise.
   */
  boolean hasMinPrice();

  /**
   * Determines whether a maximum price is set for this rule.
   *
   * @return true if a maximum price is defined and is non-negative, false otherwise.
   */
  boolean hasMaxPrice();

  /**
   * Determines whether the specified price is allowed according to the rule set.
   *
   * @param price the price to evaluate against the rule set
   * @return true if the price satisfies the conditions of the rule set, false otherwise
   */
  boolean isAllowed(P price);

  /**
   * Checks if the provided CommandSender can bypass restrictions.
   *
   * @param sender the CommandSender to check
   * @return true if the sender has the required bypass permission, otherwise false.
   */
  boolean canBypass(@NotNull final CommandSender sender);

  /**
   * Checks if the provided QUser can bypass restrictions.
   *
   * @param user the QUser to check
   * @return true if the user can bypass the restrictions, false otherwise.
   */
  boolean canBypass(@NotNull final QUser user);

  /**
   * Checks if the provided currency matches any of the allowed currency patterns
   * defined in this rule. If the currency is null, it is considered applicable.
   *
   * @param currency the currency to check. Can be null, indicating no specific currency to validate.
   *
   * @return true if the currency matches any defined pattern or is null; false otherwise
   */
  default boolean isApplicableCurrency(@Nullable final String currency) {

    if(currency != null) {
      return this.currencies().stream().anyMatch(pattern->pattern.matcher(currency).matches());
    }
    return true;
  }

  /**
   * Tallies the total amount of `ItemStack` objects in the provided iterable
   * that meet the conditions specified by the `isApplicable` method.
   *
   * @param stacks an iterable collection of `ItemStack` objects to evaluate; must not be null
   * @return the cumulative total of amounts for all applicable `ItemStack` objects
   */
  default int tallyApplicableItems(@NotNull final Iterable<ItemStack> stacks) {

    int tally = 0;
    for(final ItemStack is : stacks) {

      if(isApplicable(is)) {

        tally += is.getAmount();
      }
    }
    return tally;
  }

  /**
   * Determines if the provided CommandSender, ItemStack, and optional currency combination
   * satisfies the conditions of the rule set.
   * If the sender can bypass restrictions or the provided currency does not match the
   * applicable patterns, the method returns false. Otherwise, it evaluates the item
   * against rule-specific conditions.
   *
   * @param sender the CommandSender to evaluate; must not be null
   * @param item the ItemStack to evaluate; must not be null
   * @param currency the currency to evaluate; can be null, indicating no specific currency to validate
   * @return true if the sender, item, and currency combination satisfies the rule set conditions; false otherwise
   */
  default boolean isApplicable(@NotNull final CommandSender sender, @NotNull final ItemStack item, @Nullable final String currency) {

    if(canBypass(sender) || !isApplicableCurrency(currency)) {
      return false;
    }
    return isApplicable(item);
  }

  /**
   * Determines if the given user, item, and currency combination is applicable according to the rule set.
   * The method first checks if the user can bypass restrictions or if the provided currency is not applicable.
   * If either condition is true, the method returns false. Otherwise, it evaluates the item against the rule set.
   *
   * @param user the QUser to evaluate; must not be null
   * @param item the ItemStack to evaluate; must not be null
   * @param currency the currency to evaluate; can be null, indicating no specific currency to validate
   * @return true if the user, item, and currency combination satisfies the rule set conditions; false otherwise
   */
  default boolean isApplicable(@NotNull final QUser user, @NotNull final ItemStack item, @Nullable final String currency) {

    if(canBypass(user) || !isApplicableCurrency(currency)) {
      return false;
    }
    return isApplicable(item);
  }

  /**
   * Determines whether the provided {@link CommandSender} and {@link ItemStack}
   * satisfy the conditions of the rule set. If the sender can bypass restrictions,
   * the method immediately returns false. Otherwise, it evaluates the {@link ItemStack}
   * against a list of predefined rule-specific conditions.
   *
   * @param sender the {@link CommandSender} to evaluate; must not be null
   * @param stack  the {@link ItemStack} to evaluate; must not be null
   * @return true if the {@link ItemStack} satisfies the rule set conditions and
   *         the {@link CommandSender} cannot bypass restrictions; false otherwise
   */
  default boolean isApplicable(@NotNull final CommandSender sender, @NotNull final ItemStack stack) {

    if(canBypass(sender)) {
      return false;
    }

    return items().stream().anyMatch(fun->fun.apply(stack));
  }

  /**
   * Determines if the specified QUser and ItemStack combination satisfies the conditions of the rule set.
   * If the user can bypass the restrictions, the method immediately returns false. Otherwise, it evaluates
   * the ItemStack against a list of predefined rule-specific conditions.
   *
   * @param user the QUser to evaluate; must not be null
   * @param stack the ItemStack to evaluate; must not be null
   * @return true if the ItemStack satisfies the conditions of the rule set and the user cannot bypass restrictions,
   *         false otherwise
   */
  default boolean isApplicable(@NotNull final QUser user, @NotNull final ItemStack stack) {

    if(canBypass(user)) {
      return false;
    }

    return items().stream().anyMatch(fun->fun.apply(stack));
  }

  /**
   * Determines if the given ItemStack meets the conditions of the rule set.
   *
   * @param stack the ItemStack to evaluate; must not be null
   * @return true if the ItemStack is applicable according to the rule set, false otherwise
   */
  default boolean isApplicable(@NotNull final ItemStack stack) {

    return items().stream().anyMatch(fun->fun.apply(stack));
  }

  /**
   * Determines whether the given ItemStack and currency meet the conditions of the rule set.
   * First, it verifies if the provided currency is applicable. If not, the method returns false.
   * Then, it evaluates the ItemStack against predefined rule-specific conditions.
   *
   * @param stack the ItemStack to evaluate; must not be null
   * @param currency the currency to evaluate; must not be null
   * @return true if the ItemStack and currency satisfy the conditions of the rule set, false otherwise
   */
  default boolean isApplicable(@NotNull final ItemStack stack, @NotNull final String currency) {

    if(!isApplicableCurrency(currency)) {
      return false;
    }
    return items().stream().anyMatch(fun->fun.apply(stack));
  }
}