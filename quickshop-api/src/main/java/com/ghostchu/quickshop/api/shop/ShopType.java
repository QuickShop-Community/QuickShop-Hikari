package com.ghostchu.quickshop.api.shop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The shop trading type *SELLING* or *BUYING*
 */
public enum ShopType {
  SELLING(0), // Sell Mode
  BUYING(1), // Buy Mode
  FROZEN(2); //Locked so no mode
  private final int id;

  ShopType(final int id) {

    this.id = id;
  }

  public static @Nullable ShopType fromString(@NotNull final String string) {

    for(final ShopType type : ShopType.values()) {
      if(type.name().equalsIgnoreCase(string)) {
        return type;
      }
    }
    return null;
  }


  public static @NotNull ShopType fromID(final int id) {

    for(final ShopType type : ShopType.values()) {
      if(type.id == id) {
        return type;
      }
    }
    return SELLING;
  }

  public static int toID(@NotNull final ShopType shopType) {

    return shopType.id;
  }

  public int toID() {

    return id;
  }
}
