package com.ghostchu.quickshop.util.economyformatter;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.economy.AbstractEconomy;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import com.ghostchu.simplereloadlib.Reloadable;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class EconomyFormatter implements Reloadable {

  private static final Map<String, String> CURRENCY_SYMBOL_MAPPING = new HashMap<>();
  private final QuickShop plugin;
  private final Supplier<AbstractEconomy> economy;
  private boolean disableVaultFormat;
  private boolean useDecimalFormat;
  private boolean currencySymbolOnRight;

  public EconomyFormatter(final QuickShop plugin, final Supplier<AbstractEconomy> economy) {

    this.plugin = plugin;
    this.economy = economy;
    reloadModule();
    plugin.getReloadManager().register(this);
  }

  @Override
  public ReloadResult reloadModule() {

    CURRENCY_SYMBOL_MAPPING.clear();
    this.disableVaultFormat = plugin.getConfig().getBoolean("shop.disable-vault-format", false);
    this.useDecimalFormat = plugin.getConfig().getBoolean("use-decimal-format", false);
    this.currencySymbolOnRight = plugin.getConfig().getBoolean("shop.currency-symbol-on-right", false);
    final List<String> symbols = plugin.getConfig().getStringList("shop.alternate-currency-symbol-list");
    symbols.forEach(entry->{
      final String[] splits = entry.split(";", 2);
      if(splits.length < 2) {
        plugin.logger().warn("Invalid entry in alternate-currency-symbol-list: {}", entry);
        return;
      }
      CURRENCY_SYMBOL_MAPPING.put(splits[0], splits[1]);
    });
    return new ReloadResult(ReloadStatus.SUCCESS, "Reload successfully.", null);
  }

  /**
   * Formats the given number according to how vault would like it. E.g. $50 or 5 dollars.
   *
   * @param n price
   *
   * @return The formatted string.
   */
  public @NotNull String format(final double n, @NotNull final World world, @Nullable final String currency) {

    return format(n, disableVaultFormat, world, currency);
  }

  @NotNull
  public String format(final double n, final boolean internalFormat, @NotNull final World world, @Nullable final String currency) {

    if(internalFormat) {
      return getInternalFormat(n, currency);
    }
    try {
      final String formatted = economy.get().format(n, world, currency);
      if(StringUtils.isEmpty(formatted)) {
        Log.debug(
                "Use alternate-currency-symbol to formatting, Cause economy plugin returned null");
        return getInternalFormat(n, currency);
      } else {
        return formatted;
      }
    } catch(NumberFormatException e) {
      Log.debug(e.getMessage());
      Log.debug("Use alternate-currency-symbol to formatting, Cause NumberFormatException");
      return getInternalFormat(n, currency);
    }
  }

  private String getInternalFormat(final double amount, @Nullable final String currency) {

    if(StringUtils.isEmpty(currency)) {
      Log.debug("Format: Currency is null");
      final String formatted = useDecimalFormat? MsgUtil.decimalFormat(amount) : Double.toString(amount);
      return currencySymbolOnRight? formatted + plugin.getConfig().getString("shop.alternate-currency-symbol", "$") : plugin.getConfig().getString("shop.alternate-currency-symbol", "$") + formatted;
    } else {
      Log.debug("Format: Currency is: [" + currency + "]");
      final String formatted = useDecimalFormat? MsgUtil.decimalFormat(amount) : Double.toString(amount);
      final String symbol = CURRENCY_SYMBOL_MAPPING.getOrDefault(currency, currency);
      return currencySymbolOnRight? formatted + symbol : symbol + formatted;
    }
  }

  /**
   * Formats the given number according to how vault would like it. E.g. $50 or 5 dollars.
   *
   * @param n    price
   * @param shop shop
   *
   * @return The formatted string.
   */
  @NotNull
  public String format(final double n, @NotNull final Shop shop) {

    return format(n, disableVaultFormat, shop.getLocation().getWorld(), shop);
  }

  @NotNull
  public String format(final double n, final boolean internalFormat, @NotNull final World world, @Nullable final Shop shop) {

    if(shop != null) {
      return format(n, internalFormat, world, shop.getCurrency());
    } else {
      return format(n, internalFormat, world, (Shop)null);
    }
  }
}
