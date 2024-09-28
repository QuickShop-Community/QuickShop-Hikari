package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.api.obj.QUser;
import lombok.Data;

@Data
public class PlayerEconomyPreCheckLog {

  private static int v = 2;
  private boolean beforeTrading;
  private String player;
  private double holding;

  public PlayerEconomyPreCheckLog(boolean beforeTrading, QUser player, double holding) {

    this.beforeTrading = beforeTrading;
    this.player = player.serialize();
    this.holding = holding;
  }
}
