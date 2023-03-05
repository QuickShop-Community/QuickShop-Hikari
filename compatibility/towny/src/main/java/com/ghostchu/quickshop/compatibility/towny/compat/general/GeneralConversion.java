package com.ghostchu.quickshop.compatibility.towny.compat.general;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.compatibility.towny.compat.UuidConversion;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;

import java.util.UUID;

public class GeneralConversion implements UuidConversion {
    @Override
    public UUID convertTownyAccount(Town town) {
        return QuickShop.getInstance().getPlayerFinder().name2Uuid(town.getAccount().getName());
    }

    @Override
    public UUID convertTownyAccount(Nation nation) {
        return QuickShop.getInstance().getPlayerFinder().name2Uuid(nation.getAccount().getName());
    }
}
