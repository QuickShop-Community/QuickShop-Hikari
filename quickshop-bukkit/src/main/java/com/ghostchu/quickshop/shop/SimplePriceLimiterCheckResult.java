package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.api.shop.PriceLimiterCheckResult;
import com.ghostchu.quickshop.api.shop.PriceLimiterStatus;
import lombok.Data;

@Data
public class SimplePriceLimiterCheckResult implements PriceLimiterCheckResult {
    PriceLimiterStatus status;
    double min;
    double max;

    public SimplePriceLimiterCheckResult(PriceLimiterStatus status, double min, double max) {
        this.status = status;
        this.min = min;
        this.max = max;
    }
}
