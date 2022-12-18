package com.ghostchu.quickshop.addon.discount;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DiscountStatusManager {
    private final Map<UUID, String> PLAYER_STATUS = new HashMap<>();

    public void set(@NotNull UUID player, @NotNull DiscountCode code) {
        PLAYER_STATUS.put(player, code.getCode());
    }

    public void unset(@NotNull UUID player) {
        PLAYER_STATUS.remove(player);
    }

    @Nullable
    public DiscountCode get(UUID player, @NotNull DiscountCodeManager codeManager) {
        String codeStr = PLAYER_STATUS.get(player);
        return codeManager.getCode(codeStr);
    }

    @Nullable
    public String get(UUID player) {
        return PLAYER_STATUS.get(player);
    }
}
