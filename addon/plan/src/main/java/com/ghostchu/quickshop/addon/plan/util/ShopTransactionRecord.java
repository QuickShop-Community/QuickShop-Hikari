package com.ghostchu.quickshop.addon.plan.util;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class ShopTransactionRecord {

  private Date time;
  private UUID from;
  private UUID to;
  private String currency;
  private double amount;
  private UUID taxAccount;
  private double taxAmount;
  private String error;

  public static class ShopTransactionRecordBuilder {

    private Date time;
    private UUID from;
    private UUID to;
    private String currency;
    private double amount;
    private UUID taxAccount;
    private double taxAmount;
    private String error;

    ShopTransactionRecordBuilder() {

  }

    public ShopTransactionRecord.ShopTransactionRecordBuilder time(final Date time) {

      this.time = time;
      return this;
    }

    public ShopTransactionRecord.ShopTransactionRecordBuilder from(final UUID from) {

      this.from = from;
      return this;
    }

    public ShopTransactionRecord.ShopTransactionRecordBuilder to(final UUID to) {

      this.to = to;
      return this;
    }

    public ShopTransactionRecord.ShopTransactionRecordBuilder currency(final String currency) {

      this.currency = currency;
      return this;
    }

    public ShopTransactionRecord.ShopTransactionRecordBuilder amount(final double amount) {

      this.amount = amount;
      return this;
    }

    public ShopTransactionRecord.ShopTransactionRecordBuilder taxAccount(final UUID taxAccount) {

      this.taxAccount = taxAccount;
      return this;
    }

    public ShopTransactionRecord.ShopTransactionRecordBuilder taxAmount(final double taxAmount) {

      this.taxAmount = taxAmount;
      return this;
    }

    public ShopTransactionRecord.ShopTransactionRecordBuilder error(final String error) {

      this.error = error;
      return this;
    }

    public ShopTransactionRecord build() {

      return new ShopTransactionRecord(this.time, this.from, this.to, this.currency, this.amount, this.taxAccount, this.taxAmount, this.error);
    }

    @Override
    public String toString() {

      return "ShopTransactionRecord.ShopTransactionRecordBuilder(time=" + this.time + ", from=" + this.from + ", to=" + this.to + ", currency=" + this.currency + ", amount=" + this.amount + ", taxAccount=" + this.taxAccount + ", taxAmount=" + this.taxAmount + ", error=" + this.error + ")";
    }
  }

  public static ShopTransactionRecord.ShopTransactionRecordBuilder builder() {

    return new ShopTransactionRecord.ShopTransactionRecordBuilder();
  }

  public Date getTime() {

    return this.time;
  }

  public UUID getFrom() {

    return this.from;
  }

  public UUID getTo() {

    return this.to;
  }

  public String getCurrency() {

    return this.currency;
  }

  public double getAmount() {

    return this.amount;
  }

  public UUID getTaxAccount() {

    return this.taxAccount;
  }

  public double getTaxAmount() {

    return this.taxAmount;
  }

  public String getError() {

    return this.error;
  }

  public void setTime(final Date time) {

    this.time = time;
  }

  public void setFrom(final UUID from) {

    this.from = from;
  }

  public void setTo(final UUID to) {

    this.to = to;
  }

  public void setCurrency(final String currency) {

    this.currency = currency;
  }

  public void setAmount(final double amount) {

    this.amount = amount;
  }

  public void setTaxAccount(final UUID taxAccount) {

    this.taxAccount = taxAccount;
  }

  public void setTaxAmount(final double taxAmount) {

    this.taxAmount = taxAmount;
  }

  public void setError(final String error) {

    this.error = error;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof ShopTransactionRecord)) return false;
    final ShopTransactionRecord other = (ShopTransactionRecord)o;
    return Double.compare(this.getAmount(), other.getAmount()) == 0
           && Double.compare(this.getTaxAmount(), other.getTaxAmount()) == 0
           && Objects.equals(this.getTime(), other.getTime())
           && Objects.equals(this.getFrom(), other.getFrom())
           && Objects.equals(this.getTo(), other.getTo())
           && Objects.equals(this.getCurrency(), other.getCurrency())
           && Objects.equals(this.getTaxAccount(), other.getTaxAccount())
           && Objects.equals(this.getError(), other.getError());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.getAmount(), this.getTaxAmount(), this.getTime(), this.getFrom(), this.getTo(), this.getCurrency(), this.getTaxAccount(), this.getError());
  }

  @Override
  public String toString() {

    return "ShopTransactionRecord(time=" + this.getTime() + ", from=" + this.getFrom() + ", to=" + this.getTo() + ", currency=" + this.getCurrency() + ", amount=" + this.getAmount() + ", taxAccount=" + this.getTaxAccount() + ", taxAmount=" + this.getTaxAmount() + ", error=" + this.getError() + ")";
  }

  public ShopTransactionRecord(final Date time, final UUID from, final UUID to, final String currency, final double amount, final UUID taxAccount, final double taxAmount, final String error) {

    this.time = time;
    this.from = from;
    this.to = to;
    this.currency = currency;
    this.amount = amount;
    this.taxAccount = taxAccount;
    this.taxAmount = taxAmount;
    this.error = error;
  }
}
