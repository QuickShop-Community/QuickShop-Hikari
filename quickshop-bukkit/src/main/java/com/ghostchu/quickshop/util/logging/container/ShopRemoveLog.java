package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.ShopInfoStorage;
import lombok.Data;

@Data
public class ShopRemoveLog {

  private static int v = 2;
  private String player;
  private String reason;
  private ShopInfoStorage shop;

  public ShopRemoveLog(QUser player, String reason, ShopInfoStorage shop) {

    this.player = player.serialize();
    this.reason = reason;
    this.shop = shop;
  }
}
