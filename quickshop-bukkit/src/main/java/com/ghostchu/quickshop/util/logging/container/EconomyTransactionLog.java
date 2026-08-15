package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.api.obj.QUser;

import java.util.Objects;

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

  public EconomyTransactionLog(final boolean success, final QUser from, final QUser to, final String currency, final double tax, final QUser taxAccount, final double amount, final String lastError) {

    this.success = success;
    this.from = from.serialize();
    this.to = to.serialize();
    this.currency = currency;
    this.tax = tax;
    this.taxAccount = taxAccount.serialize();
    this.amount = amount;
    this.lastError = lastError;
  }

  public boolean isSuccess() {

    return this.success;
  }

  public String getFrom() {

    return this.from;
  }

  public String getTo() {

    return this.to;
  }

  public String getCurrency() {

    return this.currency;
  }

  public double getTax() {

    return this.tax;
  }

  public String getTaxAccount() {

    return this.taxAccount;
  }

  public double getAmount() {

    return this.amount;
  }

  public String getLastError() {

    return this.lastError;
  }

  public void setSuccess(final boolean success) {

    this.success = success;
  }

  public void setFrom(final String from) {

    this.from = from;
  }

  public void setTo(final String to) {

    this.to = to;
  }

  public void setCurrency(final String currency) {

    this.currency = currency;
  }

  public void setTax(final double tax) {

    this.tax = tax;
  }

  public void setTaxAccount(final String taxAccount) {

    this.taxAccount = taxAccount;
  }

  public void setAmount(final double amount) {

    this.amount = amount;
  }

  public void setLastError(final String lastError) {

    this.lastError = lastError;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof EconomyTransactionLog)) return false;
    final EconomyTransactionLog other = (EconomyTransactionLog)o;
    return this.isSuccess() == other.isSuccess()
           && Double.compare(this.getTax(), other.getTax()) == 0
           && Double.compare(this.getAmount(), other.getAmount()) == 0
           && Objects.equals(this.getFrom(), other.getFrom())
           && Objects.equals(this.getTo(), other.getTo())
           && Objects.equals(this.getCurrency(), other.getCurrency())
           && Objects.equals(this.getTaxAccount(), other.getTaxAccount())
           && Objects.equals(this.getLastError(), other.getLastError());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.isSuccess(), this.getTax(), this.getAmount(), this.getFrom(), this.getTo(), this.getCurrency(), this.getTaxAccount(), this.getLastError());
  }

  @Override
  public String toString() {

    return "EconomyTransactionLog(success=" + this.isSuccess() + ", from=" + this.getFrom() + ", to=" + this.getTo() + ", currency=" + this.getCurrency() + ", tax=" + this.getTax() + ", taxAccount=" + this.getTaxAccount() + ", amount=" + this.getAmount() + ", lastError=" + this.getLastError() + ")";
  }
}
