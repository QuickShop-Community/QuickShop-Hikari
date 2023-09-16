package com.ghostchu.quickshop.api.economy;

import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import com.ghostchu.simplereloadlib.Reloadable;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Abstract Economy Core
 */
public abstract class AbstractEconomy implements EconomyCore, Reloadable {

    protected AbstractEconomy() {
    }

    @Override
    public @NotNull String getName() {
        return "BuiltIn-Economy Processor";
    }

    @Override
    public boolean withdraw(@NotNull Object obj, double amount, @NotNull World world, @Nullable String currency) {
        if (obj instanceof QUser qUser) {
            // Handle UUID - Player
            UUID uuid = qUser.getUniqueIdIfRealPlayer().orElse(null);
            if (uuid != null) {
                return withdraw(uuid, amount, world, currency);
            }
            // Handle Username - System or Player
            if (qUser.getUsernameOptional().isPresent()) {
                return withdraw(qUser.getUsernameOptional().get(), amount, world, currency);
            }
            // Handle UUID - System
            if (qUser.getUniqueIdOptional().isPresent()) {
                return withdraw(qUser.getUniqueIdOptional().get(), amount, world, currency);
            }
            // All failure
            throw new IllegalArgumentException("Failed to determine the type of this QUser: " + qUser.serialize());

        } else if (obj instanceof UUID uuid) {
            return withdraw(uuid, amount, world, currency);
        } else if (obj instanceof String username) {
            return withdraw(username, amount, world, currency);
        } else if (obj instanceof OfflinePlayer offlinePlayer) {
            return withdraw(offlinePlayer, amount, world, currency);
        }
        throw new IllegalArgumentException("The obj argument can only accept one of those type: " +
                "QUser = player quickshop user object, " +
                "UUID = player uuid, String = player username, " +
                "OfflinePlayer = player bukkit offline player object." +
                " Your given: " + obj.getClass());
    }

    @Override
    public boolean deposit(@NotNull Object obj, double amount, @NotNull World world, @Nullable String currency) {
        if (obj instanceof QUser qUser) {
            // Handle UUID - Player
            UUID uuid = qUser.getUniqueIdIfRealPlayer().orElse(null);
            if (uuid != null) {
                return deposit(uuid, amount, world, currency);
            }
            // Handle Username - System or Player
            if (qUser.getUsernameOptional().isPresent()) {
                return deposit(qUser.getUsernameOptional().get(), amount, world, currency);
            }
            // Handle UUID - System
            if (qUser.getUniqueIdOptional().isPresent()) {
                return deposit(qUser.getUniqueIdOptional().get(), amount, world, currency);
            }
            // All failure
            throw new IllegalArgumentException("Failed to determine the type of this QUser: " + qUser.serialize());
        } else if (obj instanceof UUID uuid) {
            return deposit(uuid, amount, world, currency);
        } else if (obj instanceof String username) {
            return deposit(username, amount, world, currency);
        } else if (obj instanceof OfflinePlayer offlinePlayer) {
            return deposit(offlinePlayer, amount, world, currency);
        }
        throw new IllegalArgumentException("The obj argument can only accept one of those type: " +
                "QUser = player quickshop user object, " +
                "UUID = player uuid, String = player username, " +
                "OfflinePlayer = player bukkit offline player object." +
                " Your given: " + obj.getClass());
    }

    @Override
    public double getBalance(@NotNull Object obj, @NotNull World world, @Nullable String currency) {
        if (obj instanceof QUser qUser) {
            // Handle UUID - Player
            UUID uuid = qUser.getUniqueIdIfRealPlayer().orElse(null);
            if (uuid != null) {
                return getBalance(uuid, world, currency);
            }
            // Handle Username - System or Player
            if (qUser.getUsernameOptional().isPresent()) {
                return getBalance(qUser.getUsernameOptional().get(), world, currency);
            }
            // Handle UUID - System
            if (qUser.getUniqueIdOptional().isPresent()) {
                return getBalance(qUser.getUniqueIdOptional().get(), world, currency);
            }
            // All failure
            throw new IllegalArgumentException("Failed to determine the type of this QUser: " + qUser.serialize());

        } else if (obj instanceof UUID uuid) {
            return getBalance(uuid, world, currency);
        } else if (obj instanceof String username) {
            return getBalance(username, world, currency);
        } else if (obj instanceof OfflinePlayer offlinePlayer) {
            return getBalance(offlinePlayer, world, currency);
        }
        throw new IllegalArgumentException("The obj argument can only accept one of those type: " +
                "QUser = player quickshop user object, " +
                "UUID = player uuid, String = player username, " +
                "OfflinePlayer = player bukkit offline player object." +
                " Your given: " + obj.getClass());
    }

    @Override
    public boolean transfer(@NotNull Object from, @NotNull Object to, double amount, @NotNull World world, @Nullable String currency) {
        if (!isValid()) {
            return false;
        }
        if (this.getBalance(from, world, currency) >= amount) {
            if (this.withdraw(from, amount, world, currency)) {
                if (this.deposit(to, amount, world, currency)) {
                    this.deposit(from, amount, world, currency);
                    return false;
                }
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * Transfer specific amount of currency from A to B
     * (Developer: This is low layer of Economy System, use EconomyTransaction if possible)
     *
     * @param from     The player who is paying money
     * @param to       The player who is receiving money
     * @param amount   The amount to transfer
     * @param world    The transaction world
     * @param currency The currency name
     * @return successed
     */
    @Override
    public boolean transfer(@NotNull UUID from, @NotNull UUID to, double amount, @NotNull World world, @Nullable String currency) {
        if (!isValid()) {
            return false;
        }
        if (this.getBalance(from, world, currency) >= amount) {
            if (this.withdraw(from, amount, world, currency)) {
                if (this.deposit(to, amount, world, currency)) {
                    this.deposit(from, amount, world, currency);
                    return false;
                }
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean transfer(@NotNull String from, @NotNull String to, double amount, @NotNull World world, @Nullable String currency) {
        if (!isValid()) {
            return false;
        }
        if (this.getBalance(from, world, currency) >= amount) {
            if (this.withdraw(from, amount, world, currency)) {
                if (this.deposit(to, amount, world, currency)) {
                    this.deposit(from, amount, world, currency);
                    return false;
                }
                return true;
            }
            return false;
        }
        return false;
    }

    public abstract String getProviderName();

    /**
     * Callback for reloading
     *
     * @return Reloading success
     */
    @Override
    public ReloadResult reloadModule() {
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
    }

    @Override
    public abstract String toString();
}
