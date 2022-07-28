package com.ghostchu.quickshop.api.shop;

/**
 * Gets the PriceLimiter status.
 */
public enum PriceLimiterStatus {
    PASS,
    REACHED_PRICE_MAX_LIMIT,
    REACHED_PRICE_MIN_LIMIT,
    PRICE_RESTRICTED,
    NOT_A_WHOLE_NUMBER,
    NOT_VALID
}
