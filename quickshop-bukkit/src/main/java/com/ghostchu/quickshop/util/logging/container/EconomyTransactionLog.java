package com.ghostchu.quickshop.util.logging.container;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class EconomyTransactionLog {
    private static int v = 1;
    private boolean success;
    private UUID from;
    private UUID to;
    private String currency;
    private double tax;
    private UUID taxAccount;
    private double amount;
    private String lastError;

    public EconomyTransactionLog(boolean success, UUID from, UUID to, String currency, double tax, UUID taxAccount, double amount, String lastError) {
        this.success = success;
        this.from = from;
        this.to = to;
        this.currency = currency;
        this.tax = tax;
        this.taxAccount = taxAccount;
        this.amount = amount;
        this.lastError = lastError;
    }
}
