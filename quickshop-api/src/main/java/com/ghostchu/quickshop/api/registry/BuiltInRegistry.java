package com.ghostchu.quickshop.api.registry;

public enum BuiltInRegistry {
  ITEM_EXPRESSION("quickshop-hikari:item_expression");

  private final String name;

  BuiltInRegistry(final String name) {

    this.name = name;
  }

  public String getName() {

    return name;
  }
}
