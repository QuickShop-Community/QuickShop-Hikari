package com.ghostchu.quickshop.compatibility.towny.compat.gringotts.towny;

import com.ghostchu.quickshop.compatibility.towny.compat.UuidConversion;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;

import java.util.UUID;

public class GringottsTownyConversion implements UuidConversion {

  @Override
  public UUID convertTownyAccount(final Town town) {

    return town.getUUID();
  }

  @Override
  public UUID convertTownyAccount(final Nation nation) {

    return nation.getUUID();
  }
}
