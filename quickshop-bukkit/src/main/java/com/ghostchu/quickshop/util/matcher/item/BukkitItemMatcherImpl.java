package com.ghostchu.quickshop.util.matcher.item;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.ItemMatcher;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A simple impl for ItemMatcher
 *
 * @author Ghost_chu
 */
public class BukkitItemMatcherImpl implements ItemMatcher {

  private final QuickShop plugin;

  public BukkitItemMatcherImpl(final QuickShop plugin) {

    this.plugin = plugin;
  }

  /**
   * Gets the ItemMatcher provider name
   *
   * @return Provider name
   */
  @Override
  public @NotNull String getName() {

    return Bukkit.getServer().getName();
  }

  /**
   * Gets the ItemMatcher provider plugin instance
   *
   * @return Provider Plugin instance
   */
  @Override
  public @NotNull Plugin getPlugin() {

    return plugin.getJavaPlugin();
  }

  /**
   * Tests ItemStacks is matches BEWARE: Different order of itemstacks you might will got different
   * results
   *
   * @param original The original ItemStack
   * @param tester   The ItemStack will test matches with original itemstack.
   *
   * @return The result of tests
   */
  @Override
  public boolean matches(@Nullable ItemStack original, @Nullable ItemStack tester) {

    if(original == null && tester == null) {
      return true;
    }
    final boolean originalNull = original == null;
    final boolean testerNull = tester == null;
    if(originalNull || testerNull) {
      return false;
    }

    original = original.clone();
    original.setAmount(1);
    tester = tester.clone();
    tester.setAmount(1);

    final String shopIdOrigin = plugin.getPlatform().getItemShopId(original);
    if(shopIdOrigin != null) {
      final String shopIdTester = plugin.getPlatform().getItemShopId(tester);
      if(shopIdOrigin.equals(shopIdTester)) {
        return true;
      }
    }
    return tester.isSimilar(original);
  }
}
