package com.ghostchu.quickshop.economy.provider;
/*
 * QuickShop-Hikari
 * Copyright (C) 2025 Daniel "creatorfromhell" Vidmar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.economy.EconomyProvider;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.economyformatter.BuiltInEconomyFormatter;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class CoinsEngineProvider implements EconomyProvider {

  private static final String API_CLASS = "su.nightexpress.coinsengine.api.CoinsEngineAPI";
  private static final String CURRENCY_CLASS = "su.nightexpress.coinsengine.api.currency.Currency";
  private final BuiltInEconomyFormatter formatter;
  private String lastError = "No transaction error logged";

  public CoinsEngineProvider(@NotNull final QuickShop plugin) {

    this.formatter = new BuiltInEconomyFormatter(plugin);
  }

  @Override
  public @NotNull String name() {

    return "BuiltIn-CoinsEngine";
  }

  @Override
  public String providerName() {

    final Plugin plugin = Bukkit.getPluginManager().getPlugin("CoinsEngine");
    if(plugin == null) {
      return "CoinsEngine";
    }
    return plugin.getDescription().getName();
  }

  @Override
  public @NotNull String lastError() {

    return lastError;
  }

  @Override
  public boolean valid() {

    if(!CommonUtil.isClassAvailable(API_CLASS)) {
      return false;
    }
    try {
      final Class<?> apiClass = Class.forName(API_CLASS);
      final Method isLoaded = apiClass.getMethod("isLoaded");
      return Boolean.TRUE.equals(isLoaded.invoke(null));
    } catch(final Exception e) {
      return false;
    }
  }

  @Override
  public boolean multiCurrency() {

    if(!valid()) {
      return false;
    }
    return getCurrencyCount() > 1;
  }

  @Override
  public boolean supportsCurrency(final @NotNull String world, final @Nullable String currency) {

    if(!valid()) {
      return false;
    }
    if(currency == null) {
      return true;
    }
    return hasCurrency(currency);
  }

  @Override
  public @NotNull String format(final @NotNull BigDecimal amount, final @NotNull String world, final @Nullable String currency) {

    if(!valid()) {
      return "No Economy Provider";
    }

    final Object currencyInstance = resolveCurrency(currency);
    if(currencyInstance == null) {
      return formatter.getInternalFormat(amount.doubleValue(), null);
    }

    try {
      final Method format = currencyInstance.getClass().getMethod("format", double.class);
      final Object result = format.invoke(currencyInstance, amount.doubleValue());
      if(result instanceof String formatted) {
        return formatted;
      }
    } catch(final Exception e) {
      Log.transaction(Level.WARNING, "CoinsEngine format failed: " + e.getMessage());
    }

    return formatter.getInternalFormat(amount.doubleValue(), null);
  }

  @Override
  public @NotNull BigDecimal balance(final @NotNull QUser user, final @NotNull String world, final @Nullable String currency) {

    if(!valid()) {
      return BigDecimal.ZERO;
    }

    try {
      final Class<?> apiClass = Class.forName(API_CLASS);
      final Class<?> currencyClass = Class.forName(CURRENCY_CLASS);
      final UUID uuid = Objects.requireNonNull(user.getUniqueId());

      if(currency != null) {
        final Method getBalance = apiClass.getMethod("getBalance", UUID.class, String.class);
        final Object result = getBalance.invoke(null, uuid, currency);
        return BigDecimal.valueOf(((Number) result).doubleValue());
      }

      final Object currencyInstance = resolveCurrency(null);
      if(currencyInstance == null) {
        return BigDecimal.ZERO;
      }

      final Method getBalance = apiClass.getMethod("getBalance", UUID.class, currencyClass);
      final Object result = getBalance.invoke(null, uuid, currencyInstance);
      return BigDecimal.valueOf(((Number) result).doubleValue());
    } catch(final Exception e) {
      QuickShop.getInstance().logger().warn("Failure - getBalance - " + user + " - " + world + " - " + currency);
      QuickShop.getInstance().logger().warn(String.format(ERROR_MESSAGE, providerName()), e);
    }
    return BigDecimal.ZERO;
  }

  @Override
  public boolean deposit(final @NotNull QUser user, final @NotNull String world, final @Nullable String currency, final @NotNull BigDecimal amount) {

    if(!valid()) {
      return false;
    }
    try {
      final Class<?> apiClass = Class.forName(API_CLASS);
      final Class<?> currencyClass = Class.forName(CURRENCY_CLASS);
      final UUID uuid = Objects.requireNonNull(user.getUniqueId());

      final boolean success;
      if(currency != null) {
        if(!hasCurrency(currency)) {
          lastError = providerName() + ": currency not found: " + currency;
          return false;
        }
        final Method addBalance = apiClass.getMethod("addBalance", UUID.class, String.class, double.class);
        success = Boolean.TRUE.equals(addBalance.invoke(null, uuid, currency, amount.doubleValue()));
      } else {
        final Object currencyInstance = resolveCurrency(null);
        if(currencyInstance == null) {
          lastError = providerName() + ": currency not found";
          return false;
        }
        final Method addBalance = apiClass.getMethod("addBalance", UUID.class, currencyClass, double.class);
        success = Boolean.TRUE.equals(addBalance.invoke(null, uuid, currencyInstance, amount.doubleValue()));
      }

      if(!success) {
        lastError = providerName() + ": deposit failed for " + currency;
        Log.transaction(Level.WARNING, "CoinsEngine deposit failed for " + user.getUniqueId());
      }

      return success;
    } catch(final Exception e) {
      QuickShop.getInstance().logger().warn("Failure - deposit - " + user + " - " + amount + " - " + world + " - " + currency);
      QuickShop.getInstance().logger().warn(String.format(ERROR_MESSAGE, providerName()), e);
    }
    return false;
  }

  @Override
  public boolean withdraw(final @NotNull QUser user, final @NotNull String world, final @Nullable String currency, final @NotNull BigDecimal amount) {

    if(!valid()) {
      return false;
    }

    if(balance(user, world, currency).compareTo(amount) < 0) {
      lastError = providerName() + ": insufficient funds";
      return false;
    }

    try {
      final Class<?> apiClass = Class.forName(API_CLASS);
      final Class<?> currencyClass = Class.forName(CURRENCY_CLASS);
      final UUID uuid = Objects.requireNonNull(user.getUniqueId());

      final boolean success;
      if(currency != null) {
        if(!hasCurrency(currency)) {
          lastError = providerName() + ": currency not found: " + currency;
          return false;
        }
        final Method removeBalance = apiClass.getMethod("removeBalance", UUID.class, String.class, double.class);
        success = Boolean.TRUE.equals(removeBalance.invoke(null, uuid, currency, amount.doubleValue()));
      } else {
        final Object currencyInstance = resolveCurrency(null);
        if(currencyInstance == null) {
          lastError = providerName() + ": currency not found";
          return false;
        }
        final Method removeBalance = apiClass.getMethod("removeBalance", UUID.class, currencyClass, double.class);
        success = Boolean.TRUE.equals(removeBalance.invoke(null, uuid, currencyInstance, amount.doubleValue()));
      }

      if(!success) {
        lastError = providerName() + ": withdraw failed for " + currency;
        Log.transaction(Level.WARNING, "CoinsEngine withdraw failed for " + user.getUniqueId());
      }
      return success;
    } catch(final Exception e) {
      QuickShop.getInstance().logger().warn("Failure - withdraw - " + user + " - " + amount + " - " + world + " - " + currency);
      QuickShop.getInstance().logger().warn(String.format(ERROR_MESSAGE, providerName()), e);
    }
    return false;
  }

  @Nullable
  private Object resolveCurrency(@Nullable final String currency) {

    try {
      final Class<?> apiClass = Class.forName(API_CLASS);

      if(currency != null) {
        final Method getCurrency = apiClass.getMethod("getCurrency", String.class);
        return getCurrency.invoke(null, currency);
      }

      final Method getCurrencies = apiClass.getMethod("getCurrencies");
      final Object result = getCurrencies.invoke(null);
      if(!(result instanceof Collection<?> currencies)) {
        return null;
      }

      Object fallback = null;
      for(final Object entry : currencies) {
        if(entry == null) {
          continue;
        }
        if(fallback == null) {
          fallback = entry;
        }
        final Method isPrimary = entry.getClass().getMethod("isPrimary");
        if(Boolean.TRUE.equals(isPrimary.invoke(entry))) {
          return entry;
        }
      }
      return fallback;
    } catch(final Exception e) {
      return null;
    }
  }

  private boolean hasCurrency(@NotNull final String currency) {

    try {
      final Class<?> apiClass = Class.forName(API_CLASS);
      final Method hasCurrency = apiClass.getMethod("hasCurrency", String.class);
      return Boolean.TRUE.equals(hasCurrency.invoke(null, currency));
    } catch(final Exception e) {
      return false;
    }
  }

  private int getCurrencyCount() {

    try {
      final Class<?> apiClass = Class.forName(API_CLASS);
      final Method getCurrencies = apiClass.getMethod("getCurrencies");
      final Object result = getCurrencies.invoke(null);
      if(result instanceof Collection<?> currencies) {
        return currencies.size();
      }
    } catch(final Exception e) {
      return 0;
    }
    return 0;
  }
}
