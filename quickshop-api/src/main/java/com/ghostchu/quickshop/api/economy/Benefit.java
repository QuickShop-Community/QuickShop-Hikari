package com.ghostchu.quickshop.api.economy;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public interface Benefit {
    /**
     * Gets the overflowed benefit after added new benifit
     *
     * @param newAdded The benefit percentage that will add
     * @return 0.0d if no overflow or the amount of overflowed
     */
    double getOverflow(double newAdded);

    /**
     * Add a benefit to this ShopBenefit registry
     *
     * @param player  The player unique id
     * @param benefit The benefit percentage (1.0d = 100%)
     * @throws BenefitOverflowException If benefit overflowed after added
     * @throws BenefitExistsException   If benefit already exists
     */
    void addBenefit(@NotNull UUID player, double benefit) throws BenefitOverflowException, BenefitExistsException;

    /**
     * Remove a benefit from this Benefit registry
     *
     * @param player The player unique id
     */
    void removeBenefit(@NotNull UUID player);

    /**
     * Gets a benefit registry copy
     *
     * @return Benefit registry copy
     */
    @NotNull
    Map<UUID, Double> getRegistry();

    /**
     * Checks if this shop has benefit registered.
     *
     * @return Is benefit registry is empty.
     */
    boolean isEmpty();

    /**
     * Serialize the Benefit to Json
     *
     * @return The json string
     */
    @NotNull
    String serialize();

    class BenefitExistsException extends Exception {
    }

    class BenefitOverflowException extends Exception {
        private final double overflow;

        public BenefitOverflowException(double overflow) {
            this.overflow = overflow;
        }

        public double getOverflow() {
            return overflow;
        }
    }
}
