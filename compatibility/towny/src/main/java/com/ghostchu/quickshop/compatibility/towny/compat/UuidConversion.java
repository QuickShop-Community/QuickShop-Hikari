package com.ghostchu.quickshop.compatibility.towny.compat;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;

import java.util.UUID;

public interface UuidConversion {
    UUID convertTownyAccount(Town town);

    UUID convertTownyAccount(Nation nation);
}
