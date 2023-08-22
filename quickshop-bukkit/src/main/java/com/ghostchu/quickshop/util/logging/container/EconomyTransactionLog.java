package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.obj.QUserSimpleRecord;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class EconomyTransactionLog {
    private static int v = 2;
    private boolean success;
    private QUserSimpleRecord from;
    private QUserSimpleRecord to;
    private String currency;
    private double tax;
    private QUserSimpleRecord taxAccount;
    private double amount;
    private String lastError;

    public EconomyTransactionLog(boolean success, QUser from, QUser to, String currency, double tax, QUser taxAccount, double amount, String lastError) {
        this.success = success;
        this.from = QUserSimpleRecord.wrap(from);
        this.to = QUserSimpleRecord.wrap(to);
        this.currency = currency;
        this.tax = tax;
        this.taxAccount = QUserSimpleRecord.wrap(taxAccount);
        this.amount = amount;
        this.lastError = lastError;
    }
}
