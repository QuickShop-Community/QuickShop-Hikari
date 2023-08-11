package com.ghostchu.quickshop.common.obj;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public interface QUser {
    @Nullable String getUsername();

    @NotNull Optional<String> getUsernameOptional();

    @NotNull String getDisplay();

    @Nullable UUID getUniqueId();

    @NotNull Optional<UUID> getUniqueIdOptional();

    boolean isRealPlayer();

    boolean isFull();

    Optional<UUID> getUniqueIdIfRealPlayer();

    Optional<String> getUsernameIfRealPlayer();

    void setUsername(String username);

    void setUniqueId(UUID uuid);

    void setRealPlayer(boolean isRealPlayer);

    String serialize();
}
