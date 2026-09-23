package com.ghostchu.quickshop.economy;
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

import com.ghostchu.quickshop.BuiltInSolution;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.economy.EconomyProvider;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.economy.provider.VaultProvider;
import com.ghostchu.quickshop.economy.provider.VaultUnlockedProvider;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.performance.PerfMonitor;
import net.milkbowl.vault2.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * EconomyLoader
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class EconomyLoader {

  private final QuickShop plugin;

  public EconomyLoader(final QuickShop plugin) {

    this.plugin = plugin;
  }

  public boolean load() {

    try(final PerfMonitor ignored = new PerfMonitor("Loading Economy Bridge")) {
      return setup();
    } catch(final Exception e) {

      if(plugin.getSentryErrorReporter() != null) {
        plugin.getSentryErrorReporter().ignoreThrow();
      }

      plugin.logger().error("Something went wrong while trying to load the economy system!");
      plugin.logger().error("QuickShop was unable to hook into an economy system (Couldn't find Vault or VaultUnlocked)!");
      plugin.logger().error("QuickShop can NOT enable properly!");
      plugin.setupBootError(BuiltInSolution.econError(), false);
      plugin.logger().error("Plugin Listeners have been disabled. Please fix this economy issue.", e);
      return false;
    }
  }

  public boolean setup() throws Exception {

    final String provider = switch(plugin.getConfig().getInt("economy-type")) {
      case 0 -> "Vault";
      default -> null;
    };

    if(provider == null) {
      Log.debug("No economy bridge found.");
      return false;
    }

    final EconomyProvider providerInstance = provider();
    if(providerInstance == null || !providerInstance.valid()) {
      plugin.setupBootError(BuiltInSolution.econError(), false);
      return false;
    }

    plugin.getEconomyManager().provider(providerInstance);
    plugin.getEconomyManager().useProvider(providerInstance.name());
    plugin.logger().info("Selected economy bridge: {}", providerInstance.name());
    return true;
  }

  public EconomyProvider provider() {

    if(vaultUnlockedPresent()) {

      final RegisteredServiceProvider<Economy> economyProvider;
      try {

        economyProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault2.economy.Economy.class);
        if(economyProvider == null) {

          return loadVault();
        }

      } catch(final Exception ignore) {

        return loadVault();
      }

      return loadVaultUnlocked();
    }

    return loadVault();
  }

  private EconomyProvider loadVaultUnlocked() {

    final VaultUnlockedProvider vault = new VaultUnlockedProvider(plugin);
    if(!vault.valid()) {
      return null;
    }

    final QUser taxAccount = resolveTaxAccount();
    if(taxAccount == null) {
      return vault;
    }

    final UUID taxID = taxAccount.getUniqueId();
    if(taxID == null) {
      return vault;
    }

    final String taxAccountName = (taxAccount.getUsername() == null)? taxID.toString() : taxAccount.getUsername();

    if(!Objects.requireNonNull(vault.economy()).hasAccount(taxID)) {

      Log.debug("Tax account doesn't exists: " + taxAccountName);

      plugin.logger().warn("QuickShop detected that no tax account exists and will try to create one. If you see any errors, please change the shop-tax.account value in the config.yml to that of the Server owner.");

      if(vault.economy().createAccount(taxID, taxAccountName, false)) {

        plugin.logger().info("Tax account created.");
      } else {

        plugin.logger().warn("Cannot create tax-account, please change the shop-tax.account value in the config.yml to that of the server owner");
      }

      if(!vault.economy().hasAccount(taxID)) {

        plugin.logger().warn("Player for the Tax-account has never played on this server before and we couldn't create an account. This may cause server lag or economy errors, therefore changing the name is recommended. You may ignore this warning if it doesn't cause any issues.");
      }
    }
    return vault;
  }

  private EconomyProvider loadVault() {

    final VaultProvider vault = new VaultProvider(plugin);
    if(!vault.valid()) {
      return null;
    }

    final QUser taxAccount = resolveTaxAccount();
    if(taxAccount == null) {
      return vault;
    }

    final UUID taxID = taxAccount.getUniqueId();
    if(taxID == null) {
      return vault;
    }

    final String taxAccountName = (taxAccount.getUsername() == null)? taxID.toString() : taxAccount.getUsername();
    final OfflinePlayer tax = Bukkit.getOfflinePlayer(taxID);

    if(!Objects.requireNonNull(vault.economy()).hasAccount(tax)) {

      Log.debug("Tax account doesn't exists: " + taxAccountName);

      plugin.logger().warn("QuickShop detected that no tax account exists and will try to create one. If you see any errors, please change the shop-tax.account value in the config.yml to that of the Server owner.");

      if(vault.economy().createPlayerAccount(tax)) {

        plugin.logger().info("Tax account created.");
      } else {

        plugin.logger().warn("Cannot create tax-account, please change the shop-tax.account value in the config.yml to that of the server owner");
      }

      if(!vault.economy().hasAccount(tax)) {

        plugin.logger().warn("Player for the Tax-account has never played on this server before and we couldn't create an account. This may cause server lag or economy errors, therefore changing the name is recommended. You may ignore this warning if it doesn't cause any issues.");
      }
    }
    return vault;
  }

  /**
   * Resolves the tax account configured in the {@code shop-tax.account} option into a {@link QUser}.
   * <p>
   * The account is resolved with the very same logic the shop manager uses while depositing the tax
   * money, so an account created by this loader is always the account that will receive the tax
   * money later on.
   *
   * @return the resolved tax account, or null if no tax account is configured (which means the tax
   * money will be deducted but never deposited)
   */
  private @Nullable QUser resolveTaxAccount() {

    final String taxAccount = plugin.getConfig().getString("shop-tax.account", "tax");
    if(CommonUtil.isEmptyString(taxAccount)) {

      //An empty tax account name means "disable depositing", see the shop-tax section in config.yml
      plugin.logger().warn("Tax account is empty, the tax money will be deducted but not deposited.");
      return null;
    }

    return QUserImpl.createSync(plugin.getPlayerFinder(), taxAccount);
  }

  private boolean vaultUnlockedPresent() {


    try {
      Class.forName("com.example.vault.VaultPlugin");
      return false;
    } catch(final ClassNotFoundException ignored) {
    }

    final Plugin vault = plugin.getJavaPlugin().getServer().getPluginManager().getPlugin("Vault");
    return vault != null && vault.getDescription().getVersion().startsWith("2");
  }

  private boolean vaultPresent() {

    final Plugin vault = plugin.getJavaPlugin().getServer().getPluginManager().getPlugin("Vault");
    return vault != null && vault.getDescription().getVersion().startsWith("1");
  }
}