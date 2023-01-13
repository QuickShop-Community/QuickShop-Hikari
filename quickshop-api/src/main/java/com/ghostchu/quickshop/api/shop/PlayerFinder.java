package com.ghostchu.quickshop.api.shop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface PlayerFinder {
    @Nullable String uuid2Name(@NotNull UUID uuid);

    @NotNull UUID name2Uuid(@NotNull String name);

    void cache(@NotNull UUID uuid, @NotNull String name);

    boolean isCached(@NotNull UUID uuid);
}
