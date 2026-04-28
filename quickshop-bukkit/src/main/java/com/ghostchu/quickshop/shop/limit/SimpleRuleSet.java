package com.ghostchu.quickshop.shop.limit;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.limit.RuleSet;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

public class SimpleRuleSet implements RuleSet<Double> {

  private final List<Function<ItemStack, Boolean>> items;
  private final String bypassPermission;
  private final List<Pattern> currencies;
  private final double minPrice;
  private final double maxPrice;


  public SimpleRuleSet(final List<Function<ItemStack, Boolean>> items, final String bypassPermission,
                       final List<Pattern> currencies, final double minPrice, final double maxPrice) {

    this.items = items;
    this.bypassPermission = bypassPermission;
    this.currencies = currencies;
    this.minPrice = minPrice;
    this.maxPrice = maxPrice;
  }

  @Override
  public List<Function<ItemStack, Boolean>> items() {

    return items;
  }

  @Override
  public List<Pattern> currencies() {

    return currencies;
  }

  @Override
  public String bypassPermission() {

    return bypassPermission;
  }

  @Override
  public Double minPrice() {

    return minPrice;
  }

  @Override
  public Double maxPrice() {

    return maxPrice;
  }

  @Override
  public boolean hasMinPrice() {

    return minPrice > 0;
  }

  @Override
  public boolean hasMaxPrice() {

    return maxPrice >= 0;
  }

  @Override
  public boolean isAllowed(final Double price) {

    if(hasMaxPrice() && price > maxPrice) {
      return false;
    }
    if(hasMinPrice()) {
      return price >= minPrice;
    }
    return true;
  }

  @Override
  public boolean canBypass(final @NotNull CommandSender sender) {

    return QuickShop.getPermissionManager().hasPermission(sender, this.bypassPermission);
  }

  @Override
  public boolean canBypass(final @NotNull QUser user) {

    return QuickShop.getPermissionManager().hasPermission(user, this.bypassPermission);
  }
}