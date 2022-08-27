package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.Shop;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Calling when shop tax calculation
 */
public class ShopTaxEvent extends AbstractQSEvent {
    private final UUID user;
    private final Shop shop;
    private double tax;

    /**
     * Call when shop calc shop tax that will pay to system account and remove from shop owner/player received money
     *
     * @param shop The shop
     * @param tax  The tax
     * @param user The user (buyer/seller)
     */
    public ShopTaxEvent(@NotNull Shop shop, double tax, @NotNull UUID user) {
        this.shop = shop;
        this.tax = tax;
        this.user = user;
    }

    /**
     * Gets the shop
     *
     * @return the shop
     */
    public Shop getShop() {
        return this.shop;
    }

    /**
     * Gets the tax in purchase
     *
     * @return tax
     */
    public double getTax() {
        return this.tax;
    }

    /**
     * Sets the new tax in purchase
     *
     * @param tax New tax
     */
    public void setTax(double tax) {
        this.tax = tax;
    }

    /**
     * Gets the user (buyer or seller)
     *
     * @return User
     */
    public UUID getUser() {
        return this.user;
    }
}
