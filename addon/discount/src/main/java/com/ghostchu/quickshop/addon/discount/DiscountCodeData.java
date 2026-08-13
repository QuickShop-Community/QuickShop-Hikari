package com.ghostchu.quickshop.addon.discount;

import com.ghostchu.quickshop.addon.discount.type.CodeType;
import com.ghostchu.quickshop.addon.discount.type.RateType;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class DiscountCodeData {

  private UUID owner;
  private String code;
  private CodeType codeType;
  private RateType rateType;
  private String rate;
  private long expire;
  private double threshold;
  private int maxUsage;
  private Map<UUID, Integer> usages;
  private Set<Long> shopScope;

  public UUID getOwner() {

    return this.owner;
  }

  public String getCode() {

    return this.code;
  }

  public CodeType getCodeType() {

    return this.codeType;
  }

  public RateType getRateType() {

    return this.rateType;
  }

  public String getRate() {

    return this.rate;
  }

  public long getExpire() {

    return this.expire;
  }

  public double getThreshold() {

    return this.threshold;
  }

  public int getMaxUsage() {

    return this.maxUsage;
  }

  public Map<UUID, Integer> getUsages() {

    return this.usages;
  }

  public Set<Long> getShopScope() {

    return this.shopScope;
  }

  public void setOwner(final UUID owner) {

    this.owner = owner;
  }

  public void setCode(final String code) {

    this.code = code;
  }

  public void setCodeType(final CodeType codeType) {

    this.codeType = codeType;
  }

  public void setRateType(final RateType rateType) {

    this.rateType = rateType;
  }

  public void setRate(final String rate) {

    this.rate = rate;
  }

  public void setExpire(final long expire) {

    this.expire = expire;
  }

  public void setThreshold(final double threshold) {

    this.threshold = threshold;
  }

  public void setMaxUsage(final int maxUsage) {

    this.maxUsage = maxUsage;
  }

  public void setUsages(final Map<UUID, Integer> usages) {

    this.usages = usages;
  }

  public void setShopScope(final Set<Long> shopScope) {

    this.shopScope = shopScope;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof DiscountCodeData)) return false;
    final DiscountCodeData other = (DiscountCodeData)o;
    return this.getExpire() == other.getExpire()
           && Double.compare(this.getThreshold(), other.getThreshold()) == 0
           && this.getMaxUsage() == other.getMaxUsage()
           && Objects.equals(this.getOwner(), other.getOwner())
           && Objects.equals(this.getCode(), other.getCode())
           && Objects.equals(this.getCodeType(), other.getCodeType())
           && Objects.equals(this.getRateType(), other.getRateType())
           && Objects.equals(this.getRate(), other.getRate())
           && Objects.equals(this.getUsages(), other.getUsages())
           && Objects.equals(this.getShopScope(), other.getShopScope());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.getExpire(), this.getThreshold(), this.getMaxUsage(), this.getOwner(), this.getCode(), this.getCodeType(), this.getRateType(), this.getRate(), this.getUsages(), this.getShopScope());
  }

  @Override
  public String toString() {

    return "DiscountCodeData(owner=" + this.getOwner() + ", code=" + this.getCode() + ", codeType=" + this.getCodeType() + ", rateType=" + this.getRateType() + ", rate=" + this.getRate() + ", expire=" + this.getExpire() + ", threshold=" + this.getThreshold() + ", maxUsage=" + this.getMaxUsage() + ", usages=" + this.getUsages() + ", shopScope=" + this.getShopScope() + ")";
  }

  public DiscountCodeData(final UUID owner, final String code, final CodeType codeType, final RateType rateType, final String rate, final long expire, final double threshold, final int maxUsage, final Map<UUID, Integer> usages, final Set<Long> shopScope) {

    this.owner = owner;
    this.code = code;
    this.codeType = codeType;
    this.rateType = rateType;
    this.rate = rate;
    this.expire = expire;
    this.threshold = threshold;
    this.maxUsage = maxUsage;
    this.usages = usages;
    this.shopScope = shopScope;
  }

  public DiscountCodeData() {

  }
}
