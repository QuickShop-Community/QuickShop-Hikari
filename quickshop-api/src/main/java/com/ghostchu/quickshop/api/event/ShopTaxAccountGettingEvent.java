package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.Shop;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Fire when quickshop processing shop tax account.
 */
@AllArgsConstructor
public class ShopTaxAccountGettingEvent extends AbstractQSEvent {
    private final Shop shop;
    @Nullable
    private UUID taxAccount;

    /**
     * Getting the tax account
     *
     * @return The tax account, null if tax has been disabled
     */
    @Nullable
    public UUID getTaxAccount() {
        return taxAccount;
    }

    /**
     * Sets the tax account
     *
     * @param taxAccount The tax account
     */
    public void setTaxAccount(@Nullable UUID taxAccount) {
        this.taxAccount = taxAccount;
    }

    /**
     * Gets the shop
     *
     * @return The shop
     */
    public Shop getShop() {
        return shop;
    }
}
