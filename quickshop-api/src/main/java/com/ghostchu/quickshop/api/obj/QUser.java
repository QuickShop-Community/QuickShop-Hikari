package com.ghostchu.quickshop.api.obj;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public interface QUser {

  @Nullable
  String getUsername();

  void setUsername(String username);

  @NotNull
  Optional<String> getUsernameOptional();

  @NotNull
  String getDisplay();

  @Nullable
  UUID getUniqueId();

  void setUniqueId(UUID uuid);

  @NotNull
  Optional<UUID> getUniqueIdOptional();

  boolean isRealPlayer();

  void setRealPlayer(boolean isRealPlayer);

  boolean isFull();

  Optional<UUID> getUniqueIdIfRealPlayer();

  Optional<String> getUsernameIfRealPlayer();

  String serialize();

  Optional<Player> getBukkitPlayer();
}
