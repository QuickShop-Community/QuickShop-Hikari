package com.ghostchu.quickshop.util.logging.container;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class EconomyTransactionLog {
    private static int v = 2;
    private boolean success;
    private Object from;
    private Object to;
    private String currency;
    private double tax;
    private Object taxAccount;
    private double amount;
    private String lastError;

    public EconomyTransactionLog(boolean success, Object from, Object to, String currency, double tax, Object taxAccount, double amount, String lastError) {
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
