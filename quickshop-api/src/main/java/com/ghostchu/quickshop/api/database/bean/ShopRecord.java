package com.ghostchu.quickshop.api.database.bean;

import java.util.Objects;

public class ShopRecord {

  private DataRecord dataRecord;
  private InfoRecord infoRecord;

  // Extra fields
  private int cachedStock;
  private int cachedSpace;

  public ShopRecord(final DataRecord dataRecord, final InfoRecord infoRecord, final int cachedStock, final int cachedSpace) {

    this.dataRecord = dataRecord;
    this.infoRecord = infoRecord;
    this.cachedStock = cachedStock;
    this.cachedSpace = cachedSpace;
  }

  public DataRecord getDataRecord() {

    return this.dataRecord;
  }

  public InfoRecord getInfoRecord() {

    return this.infoRecord;
  }

  public int getCachedStock() {

    return this.cachedStock;
  }

  public int getCachedSpace() {

    return this.cachedSpace;
  }

  public void setDataRecord(final DataRecord dataRecord) {

    this.dataRecord = dataRecord;
  }

  public void setInfoRecord(final InfoRecord infoRecord) {

    this.infoRecord = infoRecord;
  }

  public void setCachedStock(final int cachedStock) {

    this.cachedStock = cachedStock;
  }

  public void setCachedSpace(final int cachedSpace) {

    this.cachedSpace = cachedSpace;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof ShopRecord)) return false;
    final ShopRecord other = (ShopRecord)o;
    return this.getCachedStock() == other.getCachedStock()
           && this.getCachedSpace() == other.getCachedSpace()
           && Objects.equals(this.getDataRecord(), other.getDataRecord())
           && Objects.equals(this.getInfoRecord(), other.getInfoRecord());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.getCachedStock(), this.getCachedSpace(), this.getDataRecord(), this.getInfoRecord());
  }

  @Override
  public String toString() {

    return "ShopRecord(dataRecord=" + this.getDataRecord() + ", infoRecord=" + this.getInfoRecord() + ", cachedStock=" + this.getCachedStock() + ", cachedSpace=" + this.getCachedSpace() + ")";
  }
}
