package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.api.obj.QUser;

import java.math.BigDecimal;
import java.util.Objects;

public class PlayerEconomyPreCheckLog {

  private static int v = 2;
  private boolean beforeTrading;
  private String player;
  private BigDecimal holding;

  public PlayerEconomyPreCheckLog(final boolean beforeTrading, final QUser player, final BigDecimal holding) {

    this.beforeTrading = beforeTrading;
    this.player = player.serialize();
    this.holding = holding;
  }

  public boolean isBeforeTrading() {

    return this.beforeTrading;
  }

  public String getPlayer() {

    return this.player;
  }

  public BigDecimal getHolding() {

    return this.holding;
  }

  public void setBeforeTrading(final boolean beforeTrading) {

    this.beforeTrading = beforeTrading;
  }

  public void setPlayer(final String player) {

    this.player = player;
  }

  public void setHolding(final BigDecimal holding) {

    this.holding = holding;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof PlayerEconomyPreCheckLog)) return false;
    final PlayerEconomyPreCheckLog other = (PlayerEconomyPreCheckLog)o;
    return this.isBeforeTrading() == other.isBeforeTrading()
           && Objects.equals(this.getPlayer(), other.getPlayer())
           && Objects.equals(this.getHolding(), other.getHolding());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.isBeforeTrading(), this.getPlayer(), this.getHolding());
  }

  @Override
  public String toString() {

    return "PlayerEconomyPreCheckLog(beforeTrading=" + this.isBeforeTrading() + ", player=" + this.getPlayer() + ", holding=" + this.getHolding() + ")";
  }
}
