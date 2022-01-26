/*
 * This file is a part of project QuickShop, the name is EconomyTransactionTest.java
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

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.maxgamer.quickshop.api.economy.EconomyCore;
import org.maxgamer.quickshop.api.economy.EconomyTransaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EconomyTransactionTest {


    static final EconomyCore economy = new TestEconomy();
    static final Trader taxAccount = Trader.adapt(new OfflinePlayer() {
        private final UUID uuid = UUID.randomUUID();

        @Override
        public boolean isOnline() {
            return false;
        }

        @Override
        public @Nullable String getName() {
            return null;
        }

        @Override
        public @NotNull UUID getUniqueId() {
            return uuid;
        }

        @Override
        public boolean isBanned() {
            return false;
        }

        @Override
        public boolean isWhitelisted() {
            return false;
        }

        @Override
        public void setWhitelisted(boolean value) {

        }

        @Override
        public @Nullable Player getPlayer() {
            return null;
        }

        @Override
        public long getFirstPlayed() {
            return 0;
        }

        @Override
        public long getLastPlayed() {
            return 0;
        }

        @Override
        public boolean hasPlayedBefore() {
            return false;
        }

        @Override
        public @Nullable Location getBedSpawnLocation() {
            return null;
        }

        @Override
        public void incrementStatistic(@NotNull Statistic statistic) throws IllegalArgumentException {

        }

        @Override
        public void decrementStatistic(@NotNull Statistic statistic) throws IllegalArgumentException {

        }

        @Override
        public void incrementStatistic(@NotNull Statistic statistic, int amount) throws IllegalArgumentException {

        }

        @Override
        public void decrementStatistic(@NotNull Statistic statistic, int amount) throws IllegalArgumentException {

        }

        @Override
        public void setStatistic(@NotNull Statistic statistic, int newValue) throws IllegalArgumentException {

        }

        @Override
        public int getStatistic(@NotNull Statistic statistic) throws IllegalArgumentException {
            return 0;
        }

        @Override
        public void incrementStatistic(@NotNull Statistic statistic, @NotNull Material material) throws IllegalArgumentException {

        }

        @Override
        public void decrementStatistic(@NotNull Statistic statistic, @NotNull Material material) throws IllegalArgumentException {

        }

        @Override
        public int getStatistic(@NotNull Statistic statistic, @NotNull Material material) throws IllegalArgumentException {
            return 0;
        }

        @Override
        public void incrementStatistic(@NotNull Statistic statistic, @NotNull Material material, int amount) throws IllegalArgumentException {

        }

        @Override
        public void decrementStatistic(@NotNull Statistic statistic, @NotNull Material material, int amount) throws IllegalArgumentException {

        }

        @Override
        public void setStatistic(@NotNull Statistic statistic, @NotNull Material material, int newValue) throws IllegalArgumentException {

        }

        @Override
        public void incrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) throws IllegalArgumentException {

        }

        @Override
        public void decrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) throws IllegalArgumentException {

        }

        @Override
        public int getStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) throws IllegalArgumentException {
            return 0;
        }

        @Override
        public void incrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int amount) throws IllegalArgumentException {

        }

        @Override
        public void decrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int amount) {

        }

        @Override
        public void setStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int newValue) {

        }

        @Override
        public @NotNull Map<String, Object> serialize() {
            return null;
        }

        @Override
        public boolean isOp() {
            return false;
        }

        @Override
        public void setOp(boolean value) {

        }
    });

    static {
        economy.getBalance(taxAccount, null, null);
    }

    private static EconomyTransaction genTransaction(UUID from, UUID to, double amount, double taxModifier, boolean allowLoan) {
        return EconomyTransaction.builder().core(economy).from(from).to(to).amount(amount).taxAccount(taxAccount).taxModifier(taxModifier).allowLoan(allowLoan).build();
    }

    @Test
    public void testTransaction() {
        List<UUID> UUIDList = Stream.generate(UUID::randomUUID).limit(20).collect(Collectors.toList());
        for (UUID account : UUIDList) {
            genTransaction(null, account, 1000, 0.06, false).commit(new EconomyTransaction.TransactionCallback() {
                @Override
                public void onTaxFailed(@NotNull EconomyTransaction economyTransaction) {

                }

                @Override
                public boolean onCommit(@NotNull EconomyTransaction economyTransaction) {
                    return true;
                }

                @Override
                public void onSuccess(@NotNull EconomyTransaction economyTransaction) {

                }

                @Override
                public void onFailed(@NotNull EconomyTransaction economyTransaction) {
                    throw new RuntimeException("Deposit Test Failed");
                }
            });
        }
        Assertions.assertEquals((Double) (20 * 1000 * 0.06D), (Double) economy.getBalance(taxAccount, null, null));

        Assertions.assertEquals((Double) (1000 * 0.94D), (Double) economy.getBalance(UUIDList.get(0), null, null));

//        genTransaction(UUIDList.get(5), null, 1000, 0.0, true).commit(new EconomyTransaction.TransactionCallback() {
//            @Override
//            public void onSuccess(@NotNull EconomyTransaction economyTransaction) {
//                assertEquals(-1000 * 0.06D, economy.getBalance(economyTransaction.getFrom(), null, null));
//            }
//
//            @Override
//            public void onFailed(@NotNull EconomyTransaction economyTransaction) {
//                throw new RuntimeException("Loan Test Failed");
//            }
//        });
//
//        genTransaction(UUIDList.get(4), UUIDList.get(5), 1000, 0.06, true).commit(new EconomyTransaction.TransactionCallback() {
//            @Override
//            public void onSuccess(@NotNull EconomyTransaction economyTransaction) {
//                assertEquals(-1000 * 0.06D, economy.getBalance(economyTransaction.getFrom(), null, null));
//                assertEquals(-1000 * 0.06D + 1000 * 0.94D, economy.getBalance(economyTransaction.getTo(), null, null));
//                assertEquals(20 * 1000 * 0.06D + (1000 * 0.06D), economy.getBalance(taxAccount, null, null));
//            }
//
//            @Override
//            public void onFailed(@NotNull EconomyTransaction economyTransaction) {
//                throw new RuntimeException("Transfer Test Failed");
//            }
//        });
//    }
    }

    @Test
    public void testNull() {
        try {
            EconomyTransaction.builder().core(economy).from(null).to(null).amount(100).core(economy).taxAccount(taxAccount).taxModifier(0.0).build().failSafeCommit();
        } catch (IllegalArgumentException ignored) {
            return;
        }
        throw new RuntimeException("Null Test Failed!");
    }

    static class TestEconomy implements EconomyCore {

        final Map<UUID, Double> playerBalanceMap = new HashMap<>(10);

        private Double getOrCreateAccount(UUID uuid) {
            if (!playerBalanceMap.containsKey(uuid)) {
                playerBalanceMap.put(uuid, 0.0);
                return 0.0;
            }
            return playerBalanceMap.get(uuid);
        }

        @Override
        public boolean deposit(@NotNull UUID name, double amount, @NotNull World world, @Nullable String currency) {
            playerBalanceMap.put(name, amount + getBalance(name, null, null));
            return true;
        }

        @Override
        public boolean deposit(@NotNull OfflinePlayer trader, double amount, @NotNull World world, @Nullable String currency) {
            return deposit(trader.getUniqueId(), amount, null, null);
        }

        @Override
        public String format(double balance, @NotNull World world, @Nullable String currency) {
            return Double.toString(balance);
        }

        @Override
        public double getBalance(@NotNull UUID name, @NotNull World world, @Nullable String currency) {
            return getOrCreateAccount(name);
        }

        @Override
        public double getBalance(@NotNull OfflinePlayer player, @NotNull World world, @Nullable String currency) {
            return getBalance(player.getUniqueId(), null, null);
        }

        @Override
        public boolean transfer(@NotNull UUID from, @NotNull UUID to, double amount, @NotNull World world, @Nullable String currency) {
            double formBalance = getBalance(from, null, null);
            playerBalanceMap.put(from, 0.0);
            playerBalanceMap.put(to, getBalance(from, null, null) + formBalance);
            return true;
        }

        @Override
        public boolean withdraw(@NotNull UUID name, double amount, @NotNull World world, @Nullable String currency) {
            playerBalanceMap.put(name, getBalance(name, null, null) - amount);
            return true;
        }

        @Override
        public boolean withdraw(@NotNull OfflinePlayer trader, double amount, @NotNull World world, @Nullable String currency) {
            return withdraw(trader.getUniqueId(), amount, null, null);
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
            return "ErrorTracing: Unit Test";
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public @NotNull String getName() {
            return "TestEconomy";
        }

        @Override
        public @NotNull Plugin getPlugin() {
            throw new UnsupportedOperationException();
        }
    }
}