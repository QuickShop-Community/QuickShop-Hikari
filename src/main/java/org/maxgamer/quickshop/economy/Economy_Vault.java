/*
 * This file is a part of project QuickShop, the name is Economy_Vault.java
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
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.economy.AbstractEconomy;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.economyformatter.BuiltInEconomyFormatter;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;

import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

@ToString
public class Economy_Vault extends AbstractEconomy implements Listener {

    private static final String ERROR_MESSAGE =
            "QuickShop received an error when processing Economy response, THIS NOT A QUICKSHOP FAULT, you might need ask help with your Economy Provider plugin (%s) author.";
    private final QuickShop plugin;
    private final BuiltInEconomyFormatter formatter;
    private boolean allowLoan;
    @Getter
    @Setter
    @Nullable
    private net.milkbowl.vault.economy.Economy vault;
    @Nullable
    private String lastError = null;


    public Economy_Vault(@NotNull QuickShop plugin) {
        super();
        this.plugin = plugin;
        this.formatter = new BuiltInEconomyFormatter(plugin);
        plugin.getReloadManager().register(this);
        init();
        setupEconomy();
    }

    private void init() {
        this.allowLoan = plugin.getConfig().getBoolean("shop.allow-economy-loan");
    }

    private boolean setupEconomy() {
        if (!Util.isClassAvailable("net.milkbowl.vault.economy.Economy")) {
            return false; // QUICKSHOP-YS I can't believe it broken almost a year and nobody found it, my sentry exploded.
        }
        RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> economyProvider;
        try {
            economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        } catch (Exception e) {
            return false;
        }

        if (economyProvider != null) {
            this.vault = economyProvider.getProvider();
        }

        if (this.vault == null) {
            return false;
        }

        if (this.vault.getName() == null || this.vault.getName().isEmpty()) {
            plugin
                    .getLogger()
                    .warning(
                            "Current economy plugin not correct process all request, this usually cause by irregular code, you should report this issue to your economy plugin author or use other economy plugin.");
            plugin
                    .getLogger()
                    .warning(
                            "This is technical information, please send this to economy plugin author: "
                                    + "VaultEconomyProvider.getName() return a null or empty.");
        } else {
            plugin.getLogger().info("Using economy system: " + this.vault.getName());
        }
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        Util.debugLog("Economy service listener was registered.");
        return true;
    }

    @EventHandler
    public void onServiceRegister(ServiceRegisterEvent event) {
        if (!(event.getProvider() instanceof net.milkbowl.vault.economy.Economy)) {
            return;
        }
        setupEconomy();
    }

    @EventHandler
    public void onServiceUnregister(ServiceUnregisterEvent event) {
        if (!(event.getProvider() instanceof net.milkbowl.vault.economy.Economy)) {
            return;
        }
        setupEconomy();
    }

    @Override
    public boolean deposit(@NotNull UUID name, double amount, @NotNull World world, @Nullable String currency) {
        if (!isValid()) {
            return false;
        }
        return deposit(plugin.getServer().getOfflinePlayer(name), amount, world, currency);

    }

    @Override
    public boolean deposit(@NotNull OfflinePlayer trader, double amount, @NotNull World world, @Nullable String currency) {
        if (!isValid()) {
            return false;
        }
        try {
            EconomyResponse response = Objects.requireNonNull(this.vault).depositPlayer(trader, amount);
            if(response.transactionSuccess())
                return true;
            this.lastError = getProviderName()+ ": "+response.type.name()+" - " + response.errorMessage;
            Util.debugLog("Deposit player "+trader.getUniqueId()+" failed, Vault response: "+response.errorMessage);
            return false;
        } catch (Exception t) {
            plugin.getSentryErrorReporter().ignoreThrow();
            if (trader.getName() == null) {
                plugin.getLogger().warning("Deposit failed and player name is NULL, Player uuid: " + trader.getUniqueId() + ". Provider (" + getProviderName() + ")");
                return false;
            }
            plugin.getLogger().log(Level.WARNING, String.format(ERROR_MESSAGE, getProviderName()), t);
            return false;
        }
    }

    @Override
    public String format(double balance, @NotNull World world, @Nullable String currency) {
        if (!isValid()) {
            return "Error";
        }
        try {
            String formatedBalance = Objects.requireNonNull(this.vault).format(balance);
            if (formatedBalance == null) // Stupid Ecosystem
            {
                return formatInternal(balance);
            }
            return formatedBalance;
        } catch (Exception e) {
            return formatInternal(balance);
        }
    }

    private String formatInternal(double balance) {
        if (!isValid()) {
            return "Error";
        }

        return this.formatter.getInternalFormat(balance, null);
    }

    @Override
    public double getBalance(@NotNull UUID name, @NotNull World world, @Nullable String currency) {
        if (!isValid()) {
            return 0.0;
        }

        return getBalance(plugin.getServer().getOfflinePlayer(name), world, currency);

    }

    @Override
    public double getBalance(@NotNull OfflinePlayer player, @NotNull World world, @Nullable String currency) {
        if (!isValid()) {
            return 0.0;
        }
        if (player.getName() == null) {
            return 0.0;
        }
        try {
            return Objects.requireNonNull(this.vault).getBalance(player);
        } catch (Exception t) {
            plugin.getSentryErrorReporter().ignoreThrow();
            plugin.getLogger().log(Level.WARNING, String.format(ERROR_MESSAGE, getProviderName()), t);
            return 0.0;
        }
    }

    @Override
    public boolean withdraw(@NotNull UUID name, double amount, @NotNull World world, @Nullable String currency) {
        if (!isValid()) {
            return false;
        }
        return withdraw(plugin.getServer().getOfflinePlayer(name), amount, world, currency);
    }

    @Override
    public boolean withdraw(@NotNull OfflinePlayer trader, double amount, @NotNull World world, @Nullable String currency) {
        if (!isValid()) {
            return false;
        }
        try {
            if ((!allowLoan) && (getBalance(trader, world, currency) < amount)) {
                return false;
            }
            EconomyResponse response = Objects.requireNonNull(this.vault).withdrawPlayer(trader, amount);
            if(response.transactionSuccess())
                return true;
            this.lastError = getProviderName()+ ": "+response.type.name()+" - " + response.errorMessage;
            Util.debugLog("Withdraw player "+trader.getUniqueId()+" failed, Vault response: "+response.errorMessage);
            return false;
        } catch (Exception t) {
            plugin.getSentryErrorReporter().ignoreThrow();
            if (trader.getName() == null) {
                plugin.getLogger().warning("Withdraw failed and player name is NULL, Player uuid: " + trader.getUniqueId() + ", Provider: " + getProviderName());
                return false;
            }
            plugin.getLogger().log(Level.WARNING, String.format(ERROR_MESSAGE, getProviderName()), t);
            return false;
        }
    }

    /**
     * Gets the currency does exists
     *
     * @param currency Currency name
     * @return exists
     */
    @Override
    public boolean hasCurrency(@NotNull World world, @NotNull String currency) {
        return false;
    }

    /**
     * Gets currency supports status
     *
     * @return true if supports
     */
    @Override
    public boolean supportCurrency() {
        return false;
    }

    @Override
    public @Nullable String getLastError() {
        return this.lastError;
    }

    @Override
    public boolean isValid() {
        return this.vault != null;
    }

    @Override
    public @NotNull String getName() {
        return "BuiltIn-Vault";
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return plugin;
    }

    public String getProviderName() {
        if (this.vault == null) {
            return "Provider not found.";
        }
        return String.valueOf(this.vault.getName());
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
