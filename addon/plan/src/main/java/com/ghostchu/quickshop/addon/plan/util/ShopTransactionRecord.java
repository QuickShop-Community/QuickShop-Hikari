package com.ghostchu.quickshop.addon.plan.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class ShopTransactionRecord {
    private Date time;
    private UUID from;
    private UUID to;
    private String currency;
    private double amount;
    private UUID taxAccount;
    private double taxAmount;
    private String error;
}
