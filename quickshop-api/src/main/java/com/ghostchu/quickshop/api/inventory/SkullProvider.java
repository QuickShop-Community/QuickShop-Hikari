package com.ghostchu.quickshop.api.inventory;

import net.tnemc.item.providers.SkullProfile;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface SkullProvider {

  CompletableFuture<ItemStack> provide(UUID owner);

  CompletableFuture<ItemStack> provide(String owner);

  /**
   * Asynchronously provides a SkullProfile associated with the given UUID.
   *
   * @param uuid The unique identifier of the player whose SkullProfile is to be retrieved.
   * @return A CompletableFuture that, when completed, provides the SkullProfile for the given UUID.
   *
   * @since 6.3.0.0
   */
  CompletableFuture<SkullProfile> provideProfile(UUID uuid);

  /**
   * Asynchronously provides a SkullProfile associated with the given player name.
   *
   * @param name The name of the player whose SkullProfile is to be retrieved.
   * @return A CompletableFuture that, when completed, provides the SkullProfile for the given player name.
   *
   * @since 6.3.0.0
   */
  CompletableFuture<SkullProfile> provideProfile(String name);

  /**
   * Asynchronously provides a SkullProfile associated with the given UUID and the player's last known name.
   *
   * @param uniqueId The unique identifier of the player whose SkullProfile is to be retrieved.
   * @param lastKnownName The last known name of the player associated with the UUID.
   * @return A CompletableFuture that, when completed, provides the SkullProfile for the given UUID and name.
   *
   * @since 6.3.0.0
   */
  CompletableFuture<SkullProfile> provideProfile(UUID uniqueId, String lastKnownName);

  /**
   * Retrieves a cached SkullProfile associated with the given UUID.
   * This method does not perform any asynchronous operations and returns
   * the profile if it is already cached.
   *
   * @param uuid The unique identifier of the player whose SkullProfile is to be retrieved.
   *             Must not be null.
   * @return The cached SkullProfile associated with the given UUID, or null if no cached profile exists.
   *
   * @since 6.3.0.0
   */
  SkullProfile getCachedProfile(@NotNull final UUID uuid);
}