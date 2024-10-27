package com.ghostchu.quickshop.api.event.display;

import com.ghostchu.quickshop.api.event.AbstractQSEvent;
import com.ghostchu.quickshop.api.shop.Shop;

import java.util.UUID;

public class DisplayApplicableCheckEvent extends AbstractQSEvent {

  private final Shop shop;
  private final UUID player;
  private boolean applicable;

  public DisplayApplicableCheckEvent(final Shop shop, final UUID player) {

    this.shop = shop;
    this.player = player;
  }

  public Shop getShop() {

    return shop;
  }

  public UUID getPlayer() {

    return player;
  }

  public boolean isApplicable() {

    return applicable;
  }

  public void setApplicable(final boolean applicable) {

    this.applicable = applicable;
  }
}
