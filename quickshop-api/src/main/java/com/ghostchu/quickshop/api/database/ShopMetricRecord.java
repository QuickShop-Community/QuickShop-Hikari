package com.ghostchu.quickshop.api.database;

import com.ghostchu.quickshop.api.obj.QUser;

import java.util.Objects;

public class ShopMetricRecord {

  private final long v = 3;
  private long time;
  private long shopId;
  private ShopOperationEnum type;
  private double total;
  private double tax;
  private int amount;
  private String player;

  public ShopMetricRecord(final long time, final long shopId, final ShopOperationEnum type, final double total, final double tax, final int amount, final QUser player) {

    this.time = time;
    this.shopId = shopId;
    this.type = type;
    this.total = total;
    this.tax = tax;
    this.amount = amount;
    this.player = player.serialize();
  }

  public static class ShopMetricRecordBuilder {

    private long time;
    private long shopId;
    private ShopOperationEnum type;
    private double total;
    private double tax;
    private int amount;
    private QUser player;

    ShopMetricRecordBuilder() {

  }

    public ShopMetricRecord.ShopMetricRecordBuilder time(final long time) {

      this.time = time;
      return this;
    }

    public ShopMetricRecord.ShopMetricRecordBuilder shopId(final long shopId) {

      this.shopId = shopId;
      return this;
    }

    public ShopMetricRecord.ShopMetricRecordBuilder type(final ShopOperationEnum type) {

      this.type = type;
      return this;
    }

    public ShopMetricRecord.ShopMetricRecordBuilder total(final double total) {

      this.total = total;
      return this;
    }

    public ShopMetricRecord.ShopMetricRecordBuilder tax(final double tax) {

      this.tax = tax;
      return this;
    }

    public ShopMetricRecord.ShopMetricRecordBuilder amount(final int amount) {

      this.amount = amount;
      return this;
    }

    public ShopMetricRecord.ShopMetricRecordBuilder player(final QUser player) {

      this.player = player;
      return this;
    }

    public ShopMetricRecord build() {

      return new ShopMetricRecord(this.time, this.shopId, this.type, this.total, this.tax, this.amount, this.player);
    }

    @Override
    public String toString() {

      return "ShopMetricRecord.ShopMetricRecordBuilder(time=" + this.time + ", shopId=" + this.shopId + ", type=" + this.type + ", total=" + this.total + ", tax=" + this.tax + ", amount=" + this.amount + ", player=" + this.player + ")";
    }
  }

  public static ShopMetricRecord.ShopMetricRecordBuilder builder() {

    return new ShopMetricRecord.ShopMetricRecordBuilder();
  }

  public long getV() {

    return this.v;
  }

  public long getTime() {

    return this.time;
  }

  public long getShopId() {

    return this.shopId;
  }

  public ShopOperationEnum getType() {

    return this.type;
  }

  public double getTotal() {

    return this.total;
  }

  public double getTax() {

    return this.tax;
  }

  public int getAmount() {

    return this.amount;
  }

  public String getPlayer() {

    return this.player;
  }

  public void setTime(final long time) {

    this.time = time;
  }

  public void setShopId(final long shopId) {

    this.shopId = shopId;
  }

  public void setType(final ShopOperationEnum type) {

    this.type = type;
  }

  public void setTotal(final double total) {

    this.total = total;
  }

  public void setTax(final double tax) {

    this.tax = tax;
  }

  public void setAmount(final int amount) {

    this.amount = amount;
  }

  public void setPlayer(final String player) {

    this.player = player;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof ShopMetricRecord)) return false;
    final ShopMetricRecord other = (ShopMetricRecord)o;
    return this.getV() == other.getV()
           && this.getTime() == other.getTime()
           && this.getShopId() == other.getShopId()
           && Double.compare(this.getTotal(), other.getTotal()) == 0
           && Double.compare(this.getTax(), other.getTax()) == 0
           && this.getAmount() == other.getAmount()
           && Objects.equals(this.getType(), other.getType())
           && Objects.equals(this.getPlayer(), other.getPlayer());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.getV(), this.getTime(), this.getShopId(), this.getTotal(), this.getTax(), this.getAmount(), this.getType(), this.getPlayer());
  }

  @Override
  public String toString() {

    return "ShopMetricRecord(v=" + this.getV() + ", time=" + this.getTime() + ", shopId=" + this.getShopId() + ", type=" + this.getType() + ", total=" + this.getTotal() + ", tax=" + this.getTax() + ", amount=" + this.getAmount() + ", player=" + this.getPlayer() + ")";
  }
}
