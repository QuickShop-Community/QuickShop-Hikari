package com.ghostchu.quickshop.api.shop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerFinder {
    @Nullable String uuid2Name(@NotNull UUID uuid);

    @NotNull UUID name2Uuid(@NotNull String name);

    @Nullable CompletableFuture<String> uuid2NameFuture(@NotNull UUID uuid);

    @NotNull CompletableFuture<UUID> name2UuidFuture(@NotNull String name);

    void cache(@NotNull UUID uuid, @NotNull String name);

    boolean isCached(@NotNull UUID uuid);
}
