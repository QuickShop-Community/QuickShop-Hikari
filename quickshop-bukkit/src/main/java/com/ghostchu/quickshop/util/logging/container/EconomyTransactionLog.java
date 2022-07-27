package com.ghostchu.quickshop.util.logging.container;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
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
}
