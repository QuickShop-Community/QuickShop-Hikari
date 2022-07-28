package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.InventoryTransaction;
import org.jetbrains.annotations.NotNull;

public class InventoryTransactionEvent extends AbstractQSEvent {
    private final InventoryTransaction transaction;

    /**
     * Called when a transaction created
     *
     * @param transaction The transaction
     */
    public InventoryTransactionEvent(@NotNull InventoryTransaction transaction) {
        this.transaction = transaction;
    }

    @NotNull
    public InventoryTransaction getTransaction() {
        return transaction;
    }
}
