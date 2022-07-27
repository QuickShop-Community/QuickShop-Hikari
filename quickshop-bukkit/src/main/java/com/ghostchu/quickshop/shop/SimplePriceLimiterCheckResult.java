package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.api.shop.PriceLimiterCheckResult;
import com.ghostchu.quickshop.api.shop.PriceLimiterStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class SimplePriceLimiterCheckResult implements PriceLimiterCheckResult {
    PriceLimiterStatus status;
    double min;
    double max;
}
