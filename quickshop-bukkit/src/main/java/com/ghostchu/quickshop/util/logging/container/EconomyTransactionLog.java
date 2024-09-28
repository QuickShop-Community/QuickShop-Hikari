package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.api.obj.QUser;
import lombok.Data;

@Data
public class EconomyTransactionLog {

  private static int v = 2;
  private boolean success;
  private String from;
  private String to;
  private String currency;
  private double tax;
  private String taxAccount;
  private double amount;
  private String lastError;

  public EconomyTransactionLog(boolean success, QUser from, QUser to, String currency, double tax, QUser taxAccount, double amount, String lastError) {

    this.success = success;
    this.from = from.serialize();
    this.to = to.serialize();
    this.currency = currency;
    this.tax = tax;
    this.taxAccount = taxAccount.serialize();
    this.amount = amount;
    this.lastError = lastError;
  }
}
