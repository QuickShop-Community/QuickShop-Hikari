package com.ghostchu.quickshop.api.economy;

import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import com.ghostchu.simplereloadlib.Reloadable;
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
