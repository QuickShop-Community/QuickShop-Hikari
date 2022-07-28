package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.economy.EconomyTransaction;
import org.jetbrains.annotations.NotNull;

public class EconomyTransactionEvent extends AbstractQSEvent {
    private final EconomyTransaction transaction;

    /**
     * Called when a transaction created
     *
     * @param transaction The transaction
     */
    public EconomyTransactionEvent(@NotNull EconomyTransaction transaction) {
        this.transaction = transaction;
    }

    @NotNull
    public EconomyTransaction getTransaction() {
        return transaction;
    }
}
