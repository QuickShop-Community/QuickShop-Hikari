/*
 * This file is a part of project QuickShop, the name is EconomyFormatter.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.util.economyformatter;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.economy.AbstractEconomy;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;
import org.maxgamer.quickshop.util.reload.Reloadable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EconomyFormatter implements Reloadable {
    private static final Map<String, String> CURRENCY_SYMBOL_MAPPING = new HashMap<>();
    private final QuickShop plugin;
    private final AbstractEconomy economy;
    private boolean disableVaultFormat;
    private boolean useDecimalFormat;
    private boolean currencySymbolOnRight;

    public EconomyFormatter(QuickShop plugin, AbstractEconomy economy) {
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
        List<String> symbols = plugin.getConfig().getStringList("shop.alternate-currency-symbol-list");
        symbols.forEach(entry -> {
            String[] splits = entry.split(";", 2);
            if (splits.length < 2) {
                plugin.getLogger().warning("Invalid entry in alternate-currency-symbol-list: " + entry);
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
     * @return The formatted string.
     */
    public @Nullable String format(double n, @NotNull World world, @Nullable String currency) {
        return format(n, disableVaultFormat, world, currency);
    }

    /**
     * Formats the given number according to how vault would like it. E.g. $50 or 5 dollars.
     *
     * @param n    price
     * @param shop shop
     * @return The formatted string.
     */
    @NotNull
    public String format(double n, @NotNull Shop shop) {
        return format(n, disableVaultFormat, shop.getLocation().getWorld(), shop);
    }

    @NotNull
    public String format(double n, boolean internalFormat, @NotNull World world, @Nullable Shop shop) {
        if (shop != null) {
            return format(n, internalFormat, world, shop.getCurrency());
        } else {
            return format(n, internalFormat, world, (Shop) null);
        }
    }

    @NotNull
    public String format(double n, boolean internalFormat, @NotNull World world, @Nullable String currency) {
        if (internalFormat) {
            return getInternalFormat(n, currency);
        }
        try {
            String formatted = QuickShop.getInstance().getEconomy().format(n, world, currency);
            if (StringUtils.isEmpty(formatted)) {
                Util.debugLog(
                        "Use alternate-currency-symbol to formatting, Cause economy plugin returned null");
                return getInternalFormat(n, currency);
            } else {
                return formatted;
            }
        } catch (NumberFormatException e) {
            Util.debugLog("format", e.getMessage());
            Util.debugLog(
                    "format", "Use alternate-currency-symbol to formatting, Cause NumberFormatException");
            return getInternalFormat(n, currency);
        }
    }

    private String getInternalFormat(double amount, @Nullable String currency) {
        if (StringUtils.isEmpty(currency)) {
            Util.debugLog("Format: Currency is null");
            String formatted = useDecimalFormat ? MsgUtil.decimalFormat(amount) : Double.toString(amount);
            return currencySymbolOnRight ? formatted + plugin.getConfig().getString("shop.alternate-currency-symbol", "$") : plugin.getConfig().getString("shop.alternate-currency-symbol", "$") + formatted;
        } else {
            Util.debugLog("Format: Currency is: [" + currency + "]");
            String formatted = useDecimalFormat ? MsgUtil.decimalFormat(amount) : Double.toString(amount);
            String symbol = CURRENCY_SYMBOL_MAPPING.getOrDefault(currency, currency);
            return currencySymbolOnRight ? formatted + symbol : symbol + formatted;
        }
    }
}
