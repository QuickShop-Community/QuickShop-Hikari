package com.ghostchu.quickshop.compatibility.towny.compat.general;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.compatibility.towny.Main;
import com.ghostchu.quickshop.compatibility.towny.compat.UuidConversion;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;

import java.util.UUID;

public class GeneralConversion implements UuidConversion {
    @Override
    public UUID convertTownyAccount(Town town) {
        String vaultAccountName = Main.processTownyAccount(town.getAccount().getName());
        return QuickShop.getInstance().getPlayerFinder().name2Uuid(vaultAccountName);
    }

    @Override
    public UUID convertTownyAccount(Nation nation) {
        String vaultAccountName = Main.processTownyAccount(nation.getAccount().getName());
        return QuickShop.getInstance().getPlayerFinder().name2Uuid(vaultAccountName);
    }
}
