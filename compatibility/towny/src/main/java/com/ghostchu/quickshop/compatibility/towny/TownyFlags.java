package com.ghostchu.quickshop.compatibility.towny;

import java.util.ArrayList;
import java.util.List;

public enum TownyFlags {
  OWN,
  MODIFY,
  SHOPTYPE,
  BANKTYPE;

  public static List<TownyFlags> deserialize(final List<String> list) {

    final List<TownyFlags> result = new ArrayList<>();
    list.forEach(v->result.add(TownyFlags.valueOf(v.toUpperCase())));
    return result;
  }

  public static List<String> serialize(final List<TownyFlags> list) {

    final List<String> result = new ArrayList<>();
    list.forEach(v->result.add(v.name()));
    return result;
  }
}