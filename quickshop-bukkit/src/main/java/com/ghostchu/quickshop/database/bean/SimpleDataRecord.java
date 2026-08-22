package com.ghostchu.quickshop.database.bean;

import com.ghostchu.quickshop.api.database.bean.DataRecord;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.PlayerFinder;
import com.ghostchu.quickshop.common.util.QuickExecutor;
import com.ghostchu.quickshop.obj.QUserImpl;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class SimpleDataRecord implements DataRecord {

  private final QUser owner;
  private final String item;
  private final String encoded;
  private final String name;
  private final int type;
  private final String state;
  private final String currency;
  private final double price;
  private final boolean unlimited;
  private final boolean hologram;
  private final QUser taxAccount;
  private final String permissions;
  private final String extra;
  private final String inventoryWrapper;
  private final String inventorySymbolLink;
  private final Date createTime;

  private final String benefit;

  public SimpleDataRecord(final QUser owner, final String item, final String encoded, final String name,
                          final int type, final String state, final String currency, final double price, final boolean unlimited,
                          final boolean hologram, final QUser taxAccount, final String permissions,
                          final String extra, final String inventoryWrapper, final String inventorySymbolLink,
                          final Date createTime, final String benefit) {

    this.owner = owner;
    this.item = item;
    this.encoded = encoded;
    this.name = name;
    this.type = type;
    this.state = state;
    this.currency = currency;
    this.price = price;
    this.unlimited = unlimited;
    this.hologram = hologram;
    this.taxAccount = taxAccount;
    this.permissions = permissions;
    this.extra = extra;
    this.inventoryWrapper = inventoryWrapper;
    this.inventorySymbolLink = inventorySymbolLink;
    this.createTime = createTime;
    this.benefit = benefit;
  }

  public SimpleDataRecord(final PlayerFinder finder, final ResultSet set) throws SQLException {

    this.owner = QUserImpl.deserialize(finder, set.getString("owner"), QuickExecutor.getSecondaryProfileIoExecutor());
    this.item = set.getString("item");

    final String encodedRead = set.getString("encoded");
    if(encodedRead == null) {
      this.encoded = "";
    } else {
      this.encoded = encodedRead;
    }

    this.name = set.getString("name");
    this.type = set.getInt("type");
    this.state = set.getString("shop_state");
    this.currency = set.getString("currency");
    this.price = set.getDouble("price");
    this.unlimited = set.getBoolean("unlimited");
    this.hologram = set.getBoolean("hologram");
    final String taxAccountString = set.getString("tax_account");
    this.taxAccount = taxAccountString == null? null : QUserImpl.deserialize(finder, taxAccountString, QuickExecutor.getSecondaryProfileIoExecutor());
    this.permissions = set.getString("permissions");
    this.extra = set.getString("extra");
    this.inventorySymbolLink = set.getString("inv_symbol_link");
    this.inventoryWrapper = set.getString("inv_wrapper");
    this.createTime = set.getTimestamp("create_time");
    this.benefit = set.getString("benefit");
  }

  @NotNull
  public Map<String, Object> generateLookupParams() {

    final Map<String, Object> map = new HashMap<>(generateParams());
    map.remove("create_time");
    return map;
  }

  @NotNull
  public Map<String, Object> generateParams() {

    final Map<String, Object> map = new LinkedHashMap<>();
    map.put("owner", owner.serialize());
    map.put("item", item);
    map.put("encoded", encoded);
    map.put("name", name);
    map.put("type", type);
    map.put("shop_state", state);
    map.put("currency", currency);
    map.put("price", price);
    map.put("unlimited", unlimited);
    map.put("hologram", hologram);
    if(taxAccount != null) {
      map.put("tax_account", taxAccount.serialize());
    } else {
      map.put("tax_account", null);
    }
    map.put("permissions", permissions);
    map.put("extra", extra);
    map.put("inv_wrapper", inventoryWrapper);
    map.put("inv_symbol_link", inventorySymbolLink);
    map.put("create_time", createTime);
    map.put("benefit", benefit);
    return map;
  }

  @Override
  @NotNull
  public Date getCreateTime() {

    return createTime;
  }

  @Override
  public String getCurrency() {

    return currency;
  }

  @Override
  @NotNull
  public String getExtra() {

    return extra;
  }

  @Override
  @NotNull
  public String getInventorySymbolLink() {

    return inventorySymbolLink;
  }

  @Override
  @NotNull
  public String getInventoryWrapper() {

    return inventoryWrapper;
  }

  @Override
  @NotNull
  public String getItem() {

    return item;
  }

  @Override
  @NotNull
  public String getEncoded() {

    return encoded;
  }

  @Override
  public String getName() {

    return name;
  }

  @Override
  @NotNull
  public QUser getOwner() {

    return owner;
  }

  @Override
  @NotNull
  public String getPermissions() {

    return permissions;
  }

  @Override
  public double getPrice() {

    return price;
  }

  @Override
  public QUser getTaxAccount() {

    return taxAccount;
  }

  @Override
  public int getType() {

    return type;
  }

  @Override
  public String getState() {

    return state;
  }

  @Override
  public boolean isHologram() {

    return hologram;
  }

  @Override
  public boolean isUnlimited() {

    return unlimited;
  }

  @Override
  @NotNull
  public String getBenefit() {

    return benefit;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof SimpleDataRecord)) return false;
    final SimpleDataRecord other = (SimpleDataRecord)o;
    return this.getType() == other.getType()
           && Double.compare(this.getPrice(), other.getPrice()) == 0
           && this.isUnlimited() == other.isUnlimited()
           && this.isHologram() == other.isHologram()
           && Objects.equals(this.getOwner(), other.getOwner())
           && Objects.equals(this.getItem(), other.getItem())
           && Objects.equals(this.getEncoded(), other.getEncoded())
           && Objects.equals(this.getName(), other.getName())
           && Objects.equals(this.getState(), other.getState())
           && Objects.equals(this.getCurrency(), other.getCurrency())
           && Objects.equals(this.getTaxAccount(), other.getTaxAccount())
           && Objects.equals(this.getPermissions(), other.getPermissions())
           && Objects.equals(this.getExtra(), other.getExtra())
           && Objects.equals(this.getInventoryWrapper(), other.getInventoryWrapper())
           && Objects.equals(this.getInventorySymbolLink(), other.getInventorySymbolLink())
           && Objects.equals(this.getCreateTime(), other.getCreateTime())
           && Objects.equals(this.getBenefit(), other.getBenefit());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.getType(), this.getPrice(), this.isUnlimited(), this.isHologram(), this.getOwner(), this.getItem(), this.getEncoded(), this.getName(), this.getState(), this.getCurrency(), this.getTaxAccount(), this.getPermissions(), this.getExtra(), this.getInventoryWrapper(), this.getInventorySymbolLink(), this.getCreateTime(), this.getBenefit());
  }

  @Override
  public String toString() {

    return "SimpleDataRecord(owner=" + this.getOwner() + ", item=" + this.getItem() + ", encoded=" + this.getEncoded() + ", name=" + this.getName() + ", type=" + this.getType() + ", state=" + this.getState() + ", currency=" + this.getCurrency() + ", price=" + this.getPrice() + ", unlimited=" + this.isUnlimited() + ", hologram=" + this.isHologram() + ", taxAccount=" + this.getTaxAccount() + ", permissions=" + this.getPermissions() + ", extra=" + this.getExtra() + ", inventoryWrapper=" + this.getInventoryWrapper() + ", inventorySymbolLink=" + this.getInventorySymbolLink() + ", createTime=" + this.getCreateTime() + ", benefit=" + this.getBenefit() + ")";
  }
}
