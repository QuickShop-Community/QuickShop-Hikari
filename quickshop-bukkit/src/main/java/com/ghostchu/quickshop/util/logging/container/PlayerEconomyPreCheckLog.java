package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.api.obj.QUser;
import lombok.Data;

import java.math.BigDecimal;

@Data
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
}
