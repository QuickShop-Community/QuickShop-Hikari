package com.ghostchu.quickshop.compatibility.towny.compat.essentials;

import com.ghostchu.quickshop.compatibility.towny.EssStringUtil;
import com.ghostchu.quickshop.compatibility.towny.compat.UuidConversion;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class EssentialsConversion implements UuidConversion {
    @Override
    public UUID convertTownyAccount(Town town) {
        String vaultAccountName = processAccount(town.getAccount().getName());
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + vaultAccountName).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public UUID convertTownyAccount(Nation nation) {
        String vaultAccountName = processAccount(nation.getAccount().getName());
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + vaultAccountName).getBytes(StandardCharsets.UTF_8));
    }

    private String processAccount(String accountName) {
        return EssStringUtil.safeString(accountName);
    }
}
