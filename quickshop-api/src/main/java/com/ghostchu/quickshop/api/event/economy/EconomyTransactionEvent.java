package com.ghostchu.quickshop.api.event.economy;

import com.ghostchu.quickshop.api.economy.EconomyTransaction;
import com.ghostchu.quickshop.api.event.AbstractQSEvent;
import org.jetbrains.annotations.NotNull;

public class EconomyTransactionEvent extends AbstractQSEvent {

  private final EconomyTransaction transaction;

  /**
   * Called when a transaction created
   *
   * @param transaction The transaction
   */
  public EconomyTransactionEvent(@NotNull final EconomyTransaction transaction) {

    this.transaction = transaction;
  }

  @NotNull
  public EconomyTransaction getTransaction() {

    return transaction;
  }
}
