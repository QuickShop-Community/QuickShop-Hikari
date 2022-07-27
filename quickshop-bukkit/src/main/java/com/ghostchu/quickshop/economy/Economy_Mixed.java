package com.ghostchu.quickshop.economy;///*
// * This file is a part of project QuickShop, the name is Economy_Mixed.java
// *  Copyright (C) PotatoCraft Studio and contributors
// *
// *  This program is free software: you can redistribute it and/or modify it
// *  under the terms of the GNU General Public License as published by the
// *  Free Software Foundation, either version 3 of the License, or
// *  (at your option) any later version.
// *
// *  This program is distributed in the hope that it will be useful, but WITHOUT
// *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
// *  for more details.
// *
// *  You should have received a copy of the GNU General Public License
// *  along with this program. If not, see <http://www.gnu.org/licenses/>.
// *
// */
//
//package com.ghostchu.quickshop.economy;
//
//import org.bukkit.Bukkit;
//import org.bukkit.OfflinePlayer;
//import org.bukkit.plugin.Plugin;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//import com.ghostchu.quickshop.QuickShop;
//import com.ghostchu.quickshop.util.MsgUtil;
//import com.ghostchu.quickshop.util.Util;
//
//import java.util.UUID;
//
//public class Economy_Mixed implements EconomyCore {
//    private final EconomyCore core;
//    private final QuickShop plugin;
//
//    public Economy_Mixed(@NotNull QuickShop plugin) {
//        this.plugin = plugin;
//        core = new Economy_Vault(plugin);
//    }
//
//    @Override
//    public boolean deposit(@NotNull UUID name, double amount, @Nullable String currency) {
//        if (getBalance(name, currency) < amount) {
//            return false;
//        }
//        Bukkit.dispatchCommand(
//                Bukkit.getConsoleSender(),
//                MsgUtil.fillArgs(
//                        plugin.getConfig().getString("mixedeconomy.deposit"),
//                        Bukkit.getOfflinePlayer(name).getName(),
//                        String.valueOf(amount)));
//        return true;
//    }
//
//    @Override
//    public boolean deposit(@NotNull OfflinePlayer trader, double amount, @Nullable String currency) {
//        if (getBalance(trader, currency) < amount) {
//            return false;
//        }
//        Bukkit.dispatchCommand(
//                Bukkit.getConsoleSender(),
//                MsgUtil.fillArgs(
//                        plugin.getConfig().getString("mixedeconomy.deposit"),
//                        trader.getName(),
//                        String.valueOf(amount)));
//        return true;
//    }
//
//    @Override
//    public String format(double balance, @Nullable String currency) {
//        return Util.format(balance, null);
//    }
//
//    @Override
//    public double getBalance(@NotNull UUID name, @Nullable String currency) {
//        return core.getBalance(name, currency);
//    }
//
//    @Override
//    public double getBalance(@NotNull OfflinePlayer player, @Nullable String currency) {
//        return core.getBalance(player, currency);
//    }
//
//    @Override
//    public boolean transfer(@NotNull UUID from, @NotNull UUID to, double amount, @Nullable String currency) {
//        boolean result;
//        result = withdraw(from, amount, currency);
//        if (!result) {
//            deposit(from, amount, currency);
//        }
//        result = deposit(to, amount, currency);
//        if (!result) {
//            withdraw(to, amount, currency);
//        }
//        return true;
//    }
//
//    @Override
//    public boolean withdraw(@NotNull UUID name, double amount, @Nullable String currency) {
//        if (getBalance(name, currency) > amount) {
//            return false;
//        }
//        Bukkit.dispatchCommand(
//                Bukkit.getConsoleSender(),
//                MsgUtil.fillArgs(
//                        plugin.getConfig().getString("mixedeconomy.withdraw"),
//                        Bukkit.getOfflinePlayer(name).getName(),
//                        String.valueOf(amount)));
//        return true;
//    }
//
//    @Override
//    public boolean withdraw(@NotNull OfflinePlayer trader, double amount, @Nullable String currency) {
//        if (getBalance(trader, currency) > amount) {
//            return false;
//        }
//        Bukkit.dispatchCommand(
//                Bukkit.getConsoleSender(),
//                MsgUtil.fillArgs(
//                        plugin.getConfig().getString("mixedeconomy.withdraw"),
//                        trader.getName(),
//                        String.valueOf(amount)));
//        return true;
//    }
//
//    /**
//     * Gets the currency does exists
//     *
//     * @param currency Currency name
//     * @return exists
//     */
//    @Override
//    public boolean hasCurrency(@NotNull String currency) {
//        return false;
//    }
//
//    /**
//     * Gets currency supports status
//     *
//     * @return true if supports
//     */
//    @Override
//    public boolean supportCurrency() {
//        return false;
//    }
//
//    @Override
//    public boolean isValid() {
//        return core.isValid();
//    }
//
//    @Override
//    public @NotNull String getName() {
//        return "BuiltIn-Mixed";
//    }
//
//    @Override
//    public @NotNull Plugin getPlugin() {
//        return this.plugin;
//    }
//
//}
