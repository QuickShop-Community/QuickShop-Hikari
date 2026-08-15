package com.ghostchu.quickshop.util.skin;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.tnemc.item.providers.SkullProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BukkitSkullProvider implements com.ghostchu.quickshop.api.inventory.SkullProvider {

  private final Cache<Object, ItemStack> profileCache = CacheBuilder.newBuilder()
          .expireAfterAccess(12, TimeUnit.HOURS)
          .maximumSize(512)
          .build();

  @Override
  public CompletableFuture<ItemStack> provide(final UUID owner) {

    return CompletableFuture.supplyAsync(()->{
      try {
        return profileCache.get(owner, ()->load(owner));
      } catch(final ExecutionException e) {
        e.printStackTrace();
        return new ItemStack(Material.PLAYER_HEAD);
      }
    });
  }


  @Override
  public CompletableFuture<ItemStack> provide(final String owner) {

    return CompletableFuture.supplyAsync(()->{
      try {
        return profileCache.get(owner, ()->load(owner));
      } catch(final ExecutionException e) {
        e.printStackTrace();
        return new ItemStack(Material.PLAYER_HEAD);
      }
    });
  }

  /**
   * Asynchronously provides a SkullProfile associated with the given UUID.
   *
   * @param uuid The unique identifier of the player whose SkullProfile is to be retrieved.
   *
   * @return A CompletableFuture that, when completed, provides the SkullProfile for the given
   * UUID.
   *
   * @since 6.3.0.0
   */
  @Override
  public CompletableFuture<SkullProfile> provideProfile(final UUID uuid) {

    return null;
  }

  /**
   * Asynchronously provides a SkullProfile associated with the given player name.
   *
   * @param name The name of the player whose SkullProfile is to be retrieved.
   *
   * @return A CompletableFuture that, when completed, provides the SkullProfile for the given
   * player name.
   *
   * @since 6.3.0.0
   */
  @Override
  public CompletableFuture<SkullProfile> provideProfile(final String name) {

    return null;
  }

  /**
   * Asynchronously provides a SkullProfile associated with the given UUID and the player's last
   * known name.
   *
   * @param uniqueId      The unique identifier of the player whose SkullProfile is to be
   *                      retrieved.
   * @param lastKnownName The last known name of the player associated with the UUID.
   *
   * @return A CompletableFuture that, when completed, provides the SkullProfile for the given
   * UUID and name.
   *
   * @since 6.3.0.0
   */
  @Override
  public CompletableFuture<SkullProfile> provideProfile(final UUID uniqueId, final String lastKnownName) {

    return null;
  }

  /**
   * Retrieves a cached SkullProfile associated with the given UUID. This method does not perform
   * any asynchronous operations and returns the profile if it is already cached.
   *
   * @param uuid The unique identifier of the player whose SkullProfile is to be retrieved. Must
   *             not be null.
   *
   * @return The cached SkullProfile associated with the given UUID, or null if no cached profile
   * exists.
   *
   * @since 6.3.0.0
   */
  @Override
  public SkullProfile getCachedProfile(final @NotNull UUID uuid) {

    return null;
  }

  @NotNull
  private ItemStack load(final OfflinePlayer offlinePlayer) {

    final ItemStack head = new ItemStack(Material.PLAYER_HEAD);
    final ItemMeta meta = head.getItemMeta();
    PlayerProfile profile = offlinePlayer.getPlayerProfile();
    if(!profile.isComplete()) {
      try {
        profile = profile.update().get(10, TimeUnit.SECONDS);
      } catch(final InterruptedException | ExecutionException | TimeoutException ignored) {
      }
    }
    if(meta instanceof final SkullMeta skullMeta) {
      skullMeta.setOwnerProfile(profile);
      head.setItemMeta(meta);
    }
    return head;
  }

  private ItemStack load(final UUID uuid) {

    return load(Bukkit.getOfflinePlayer(uuid));
  }

  private ItemStack load(final String owner) {

    return load(Bukkit.getOfflinePlayer(owner));
  }
}
