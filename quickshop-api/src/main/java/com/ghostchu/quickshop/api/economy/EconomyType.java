package com.ghostchu.quickshop.api.economy;

import org.jetbrains.annotations.NotNull;

public enum EconomyType {
  /*
   * UNKNOWN = FALLBACK TO VAULT
   * VAULT = USE VAULT API
   * RESERVE = USE RESERVE API
   * */
  UNKNOWN(-1),
  VAULT(0),
  RESERVE(1),
  //MIXED(2),
  GEMS_ECONOMY(3),
  TNE(4),
  COINS_ENGINE(5),
  TREASURY(6);

  private final int id;

  EconomyType(final int id) {

    this.id = id;
  }

  @NotNull
  public static EconomyType fromID(final int id) {

    for(final EconomyType type : EconomyType.values()) {
      if(type.id == id) {
        return type;
      }
    }
    return UNKNOWN;
  }

  public static int toID(@NotNull final EconomyType economyType) {

    return economyType.id;
  }

  public int toID() {

    return id;
  }
}
