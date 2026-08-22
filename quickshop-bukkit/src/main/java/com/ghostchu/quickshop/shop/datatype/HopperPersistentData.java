package com.ghostchu.quickshop.shop.datatype;

import com.google.gson.annotations.Expose;

import java.util.UUID;

public class HopperPersistentData {

  @Expose
  private final UUID player;

  public HopperPersistentData(final UUID player) {

    this.player = player;
  }

  public UUID getPlayer() {

    return this.player;
  }
}
