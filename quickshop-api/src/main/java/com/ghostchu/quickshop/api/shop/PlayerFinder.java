package com.ghostchu.quickshop.api.shop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public interface PlayerFinder {

  @Nullable
  String uuid2Name(@NotNull UUID uuid);

  @Nullable
  String uuid2Name(@NotNull UUID uuid, boolean writeCache, @NotNull ExecutorService executorService);

  @Nullable
  UUID name2Uuid(@NotNull String name);

  @Nullable
  UUID name2Uuid(@NotNull String name, boolean writeCache, @NotNull ExecutorService executorService);

  @NotNull
  CompletableFuture<String> uuid2NameFuture(@NotNull UUID uuid);

  @NotNull
  CompletableFuture<String> uuid2NameFuture(@NotNull UUID uuid, boolean writeCache, @NotNull ExecutorService executorService);

  @NotNull
  CompletableFuture<UUID> name2UuidFuture(@NotNull String name);

  @NotNull
  CompletableFuture<UUID> name2UuidFuture(@NotNull String name, boolean writeCache, @NotNull ExecutorService executorService);

  void cache(@NotNull UUID uuid, @NotNull String name);

  boolean isCached(@NotNull UUID uuid);
}
