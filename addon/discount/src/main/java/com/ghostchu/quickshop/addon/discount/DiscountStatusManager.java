package com.ghostchu.quickshop.addon.discount;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DiscountStatusManager {

  private final Map<UUID, String> PLAYER_STATUS = new HashMap<>();

  public void set(@NotNull final UUID player, @NotNull final DiscountCode code) {

    PLAYER_STATUS.put(player, code.getCode());
  }

  public void unset(@NotNull final UUID player) {

    PLAYER_STATUS.remove(player);
  }

  @Nullable
  public DiscountCode get(final UUID player, @NotNull final DiscountCodeManager codeManager) {

    final String codeStr = PLAYER_STATUS.get(player);
    return codeManager.getCode(codeStr);
  }

  @Nullable
  public String get(final UUID player) {

    return PLAYER_STATUS.get(player);
  }
}
