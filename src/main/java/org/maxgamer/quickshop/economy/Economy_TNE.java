/*
 * This file is a part of project QuickShop, the name is Economy_TNE.java
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

package org.maxgamer.quickshop.economy;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.tnemc.core.TNE;
import net.tnemc.core.common.api.TNEAPI;
import net.tnemc.core.common.currency.TNECurrency;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.economy.AbstractEconomy;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;

import java.math.BigDecimal;
import java.util.UUID;

@ToString
public class Economy_TNE extends AbstractEconomy {

    private final QuickShop plugin;
    private boolean allowLoan;

    @Getter
    @Setter
    private TNEAPI api;

    public Economy_TNE(@NotNull QuickShop plugin) {
        super();
        this.plugin = plugin;
        plugin.getReloadManager().register(this);
        init();
        setupEconomy();
    }

    private void init() {
        this.allowLoan = plugin.getConfig().getBoolean("shop.allow-economy-loan");
    }

    private void setupEconomy() {
        this.api = TNE.instance().api();
    }

    @Nullable
    private TNECurrency getCurrency(@NotNull World world, @Nullable String currency) {
        if (!isValid()) {
            return null;
        }
        if (currency != null) {
            for (TNECurrency apiCurrency : this.api.getCurrencies(world.getName())) {
                if (apiCurrency.getIdentifier().equals(currency)) {
                    return apiCurrency;
                }
            }
        }
        return this.api.getDefault(world.getName()); // Want to get some default currency available in thi world
    }

    /**
     * Deposits a given amount of money from thin air to the given username.
     *
     * @param name     The exact (case insensitive) username to give money to
     * @param amount   The amount to give them
     * @param currency The currency name
     * @return True if success (Should be almost always)
     */
    @Override
    public boolean deposit(@NotNull UUID name, double amount, @NotNull World world, @Nullable String currency) {
        deposit(Bukkit.getOfflinePlayer(name), amount, world, currency);
        return false;
    }

    /**
     * Deposits a given amount of money from thin air to the given username.
     *
     * @param trader   The player to give money to
     * @param amount   The amount to give them
     * @param currency The currency name
     * @return True if success (Should be almost always)
     */
    @Override
    public boolean deposit(@NotNull OfflinePlayer trader, double amount, @NotNull World world, @Nullable String currency) {
        if (!isValid()) {
            return false;
        }
        BigDecimal decimal = BigDecimal.valueOf(amount);
        if (!this.api.canAddHoldings(trader.getUniqueId().toString(), decimal, getCurrency(world, currency), world.getName())) {
            return false;
        }
        return this.api.addHoldings(trader.getUniqueId().toString(), decimal, getCurrency(world, currency), world.getName());
    }


    /**
     * Formats the given number... E.g. 50.5 becomes $50.5 Dollars, or 50 Dollars 5 Cents
     *
     * @param balance  The given number
     * @param currency The currency name
     * @return The balance in human readable text.
     */
    @Override
    public String format(double balance, @NotNull World world, @Nullable String currency) {
        if (!isValid()) {
            return "Error";
        }
        BigDecimal decimal = BigDecimal.valueOf(balance);
        return this.api.format(decimal, getCurrency(world, currency), world.getName());
    }


    /**
     * Fetches the balance of the given account name
     *
     * @param name     The name of the account
     * @param currency The currency name
     * @return Their current balance.
     */
    @Override
    public double getBalance(@NotNull UUID name, @NotNull World world, @Nullable String currency) {
        return getBalance(Bukkit.getOfflinePlayer(name), world, currency);
    }

    /**
     * Fetches the balance of the given player
     *
     * @param player   The name of the account
     * @param currency The currency name
     * @return Their current balance.
     */
    @Override
    public double getBalance(@NotNull OfflinePlayer player, @NotNull World world, @Nullable String currency) {
        if (!isValid()) {
            return 0.0;
        }
        if (getCurrency(world, currency) != null) {
            return this.api.getHoldings(player.getName(), getCurrency(world, currency)).doubleValue();
        } else {
            return this.api.getHoldings(player.getName(), world.getName()).doubleValue();
        }
    }

    /**
     * Withdraws a given amount of money from the given username and turns it to thin air.
     *
     * @param name     The exact (case insensitive) username to take money from
     * @param amount   The amount to take from them
     * @param currency The currency name
     * @return True if success, false if they didn't have enough cash
     */
    @Override
    public boolean withdraw(@NotNull UUID name, double amount, @NotNull World world, @Nullable String currency) {
        return withdraw(Bukkit.getOfflinePlayer(name), amount, world, currency);
    }

    /**
     * Withdraws a given amount of money from the given username and turns it to thin air.
     *
     * @param trader   The player to take money from
     * @param amount   The amount to take from them
     * @param currency The currency name
     * @return True if success, false if they didn't have enough cash
     */
    @Override
    public boolean withdraw(@NotNull OfflinePlayer trader, double amount, @NotNull World world, @Nullable String currency) {
        if (!isValid()) {
            return false;
        }
        BigDecimal decimal = BigDecimal.valueOf(amount);
        if (!this.api.canRemoveHoldings(trader.getUniqueId().toString(), decimal, getCurrency(world, currency), world.getName())) {
            return false;
        }
        return this.api.removeHoldings(trader.getUniqueId().toString(), decimal, getCurrency(world, currency), world.getName());
    }

    /**
     * Gets the currency does exists
     *
     * @param currency Currency name
     * @return exists
     */
    @Override
    public boolean hasCurrency(@NotNull World world, @NotNull String currency) {
        return getCurrency(world, currency) != null;
    }

    /**
     * Gets currency supports status
     *
     * @return true if supports
     */
    @Override
    public boolean supportCurrency() {
        return true;
    }

    /**
     * Checks that this economy is valid. Returns false if it is not valid.
     *
     * @return True if this economy will work, false if it will not.
     */
    @Override
    public boolean isValid() {
        return this.api != null && TNE.instance() != null;
    }

    @Override
    public @NotNull String getName() {
        return "BuiltIn-TNE";
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return this.plugin;
    }

    /**
     * Callback for reloading
     *
     * @return Reloading success
     */
    @Override
    public ReloadResult reloadModule() {
        init();
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
    }
}
