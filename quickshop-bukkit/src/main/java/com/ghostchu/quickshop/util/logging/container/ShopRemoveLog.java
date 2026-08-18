package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.ShopInfoStorage;

import java.util.Objects;

public class ShopRemoveLog {

  private static int v = 2;
  private String player;
  private String reason;
  private ShopInfoStorage shop;

  public ShopRemoveLog(final QUser player, final String reason, final ShopInfoStorage shop) {

    this.player = player.serialize();
    this.reason = reason;
    this.shop = shop;
  }

  public String getPlayer() {

    return this.player;
  }

  public String getReason() {

    return this.reason;
  }

  public ShopInfoStorage getShop() {

    return this.shop;
  }

  public void setPlayer(final String player) {

    this.player = player;
  }

  public void setReason(final String reason) {

    this.reason = reason;
  }

  public void setShop(final ShopInfoStorage shop) {

    this.shop = shop;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof ShopRemoveLog)) return false;
    final ShopRemoveLog other = (ShopRemoveLog)o;
    return Objects.equals(this.getPlayer(), other.getPlayer())
           && Objects.equals(this.getReason(), other.getReason())
           && Objects.equals(this.getShop(), other.getShop());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.getPlayer(), this.getReason(), this.getShop());
  }

  @Override
  public String toString() {

    return "ShopRemoveLog(player=" + this.getPlayer() + ", reason=" + this.getReason() + ", shop=" + this.getShop() + ")";
  }
}
