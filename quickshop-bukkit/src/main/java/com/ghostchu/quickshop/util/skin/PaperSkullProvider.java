package com.ghostchu.quickshop.util.skin;

/*
 * QuickShop-Hikari
 * Copyright (C) 2026 Daniel "creatorfromhell" Vidmar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.ghostchu.quickshop.api.inventory.SkullProvider;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.tnemc.item.providers.SkullProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PaperSkullProvider
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public final class PaperSkullProvider implements SkullProvider, AutoCloseable {

    private static final int DEFAULT_CACHE_SIZE = 5_000;
    private static final long DEFAULT_CACHE_DURATION_HOURS = 168;
    private static final int DEFAULT_THREAD_COUNT = 2;
    private static final int DEFAULT_QUEUE_SIZE = 128;
    private static final long DEFAULT_PROFILE_TIMEOUT_SECONDS = 10;

    private final Cache<ProfileKey, SkullProfile> profileCache;
    private final Cache<ProfileKey, ItemStack> itemCache;

    private final ConcurrentMap<ProfileKey, CompletableFuture<SkullProfile>> loadingProfiles = new ConcurrentHashMap<>();
    private final ConcurrentMap<ProfileKey, CompletableFuture<ItemStack>> loadingItems = new ConcurrentHashMap<>();

    private final ExecutorService executor;
    private final long profileTimeoutSeconds;

    /**
     * Creates a provider using the default cache and executor settings.
     */
    public PaperSkullProvider() {

        this(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_DURATION_HOURS, DEFAULT_THREAD_COUNT, DEFAULT_QUEUE_SIZE, DEFAULT_PROFILE_TIMEOUT_SECONDS);
    }

    /**
     * Creates a configurable Paper skull provider.
     *
     * @param cacheSize maximum number of cached profiles and skull items
     * @param cacheDurationHours number of hours since last access before a cache entry expires
     * @param threadCount maximum number of concurrently running profile tasks
     * @param queueSize maximum number of queued profile tasks
     * @param profileTimeoutSeconds maximum time allowed for profile completion
     */
    public PaperSkullProvider(final int cacheSize, final long cacheDurationHours, final int threadCount, final int queueSize, final long profileTimeoutSeconds) {

        if(cacheSize <= 0) {
            throw new IllegalArgumentException("cacheSize must be greater than zero");
        }

        if(cacheDurationHours <= 0) {
            throw new IllegalArgumentException("cacheDurationHours must be greater than zero");
        }

        if(threadCount <= 0) {
            throw new IllegalArgumentException("threadCount must be greater than zero");
        }

        if(queueSize <= 0) {
            throw new IllegalArgumentException("queueSize must be greater than zero");
        }

        if(profileTimeoutSeconds <= 0) {
            throw new IllegalArgumentException("profileTimeoutSeconds must be greater than zero");
        }

        this.profileTimeoutSeconds = profileTimeoutSeconds;

        this.profileCache = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .expireAfterAccess(cacheDurationHours, TimeUnit.HOURS)
                .build();

        this.itemCache = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .expireAfterAccess(cacheDurationHours, TimeUnit.HOURS)
                .build();

        this.executor = new ThreadPoolExecutor(threadCount, threadCount, 0L, TimeUnit.MILLISECONDS,
                                               new ArrayBlockingQueue<>(queueSize), new SkullThreadFactory(),
                                               new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * Provides a player-head item for a UUID.
     *
     * @param owner player UUID
     * @return future containing the player head
     */
    @Override
    public CompletableFuture<ItemStack> provide(@NotNull final UUID owner) {

        Objects.requireNonNull(owner, "owner");

        return provideItem(ProfileKey.of(owner, null));
    }

    /**
     * Provides a player-head item for a player name.
     *
     * @param owner player name
     * @return future containing the player head
     */
    @Override
    public CompletableFuture<ItemStack> provide(@NotNull final String owner) {

        final String normalizedName = normalizeName(owner);

        if(normalizedName == null) {
            return CompletableFuture.completedFuture(createFallbackHead());
        }

        return provideItem(ProfileKey.of(null, normalizedName));
    }

    /**
     * Asynchronously provides a SkullProfile associated with the given UUID.
     *
     * @param uuid The unique identifier of the player whose SkullProfile is to be retrieved.
     * @return A CompletableFuture that, when completed, provides the SkullProfile for the given UUID.
     *
     * @since 6.3.0.0
     */
    @Override
    public CompletableFuture<SkullProfile> provideProfile(@NotNull final UUID uuid) {

        Objects.requireNonNull(uuid, "uuid");

        return provideProfile(ProfileKey.of(uuid, null));
    }

    /**
     * Asynchronously provides a SkullProfile associated with the given player name.
     *
     * @param name The name of the player whose SkullProfile is to be retrieved.
     * @return A CompletableFuture that, when completed, provides the SkullProfile for the given player name.
     *
     * @since 6.3.0.0
     */
    @Override
    public CompletableFuture<SkullProfile> provideProfile(@NotNull final String name) {

        final String normalizedName = normalizeName(name);

        if(normalizedName == null) {
            return CompletableFuture.completedFuture(new SkullProfile());
        }

        return provideProfile(ProfileKey.of(null, normalizedName));
    }

    /**
     * Asynchronously provides a SkullProfile associated with the given UUID and the player's last known name.
     *
     * @param uniqueId The unique identifier of the player whose SkullProfile is to be retrieved.
     * @param lastKnownName The last known name of the player associated with the UUID.
     * @return A CompletableFuture that, when completed, provides the SkullProfile for the given UUID and name.
     *
     * @since 6.3.0.0
     */
    @Override
    public CompletableFuture<SkullProfile> provideProfile(@NotNull final UUID uniqueId, @Nullable final String lastKnownName) {

        Objects.requireNonNull(uniqueId, "uniqueId");

        return provideProfile(ProfileKey.of(uniqueId, normalizeName(lastKnownName)));
    }

    /**
     * Returns a cached profile without starting a remote profile lookup.
     *
     * @param uuid player UUID
     * @return cached profile or {@code null}
     */
    @Nullable
    public SkullProfile getCachedProfile(@NotNull final UUID uuid) {

        Objects.requireNonNull(uuid, "uuid");

        final SkullProfile profile = profileCache.getIfPresent(ProfileKey.of(uuid, null));
        return profile == null? null : copyProfile(profile);
    }

    /**
     * Returns a cached skull item without starting a remote profile lookup.
     *
     * @param uuid player UUID
     * @return cached skull or a generic player head
     */
    @NotNull
    public ItemStack getCachedItemOrFallback(@NotNull final UUID uuid) {

        Objects.requireNonNull(uuid, "uuid");

        final ItemStack item = itemCache.getIfPresent(ProfileKey.of(uuid, null));
        return (item == null)? createFallbackHead() : item.clone();
    }

    /**
     * Invalidates cached and in-flight entries for a UUID.
     *
     * @param uuid player UUID
     */
    public void invalidate(@NotNull final UUID uuid) {

        Objects.requireNonNull(uuid, "uuid");

        invalidateMatching(uuid);
    }

    /**
     * Clears all profile and item caches.
     */
    public void invalidateAll() {

        profileCache.invalidateAll();
        itemCache.invalidateAll();

        loadingProfiles.values().forEach(future->future.cancel(false));
        loadingItems.values().forEach(future->future.cancel(false));

        loadingProfiles.clear();
        loadingItems.clear();
    }

    @NotNull
    private CompletableFuture<ItemStack> provideItem(@NotNull final ProfileKey key) {

        final ItemStack cached = findCachedItem(key);

        if(cached != null) {
            return CompletableFuture.completedFuture(cached.clone());
        }

        final CompletableFuture<ItemStack> future = loadingItems.computeIfAbsent(key, this::startItemLoad);
        return future.thenApply(ItemStack::clone);
    }

    @NotNull
    private CompletableFuture<ItemStack> startItemLoad(@NotNull final ProfileKey key) {

        final CompletableFuture<ItemStack> future;

        try {
            future = provideProfile(key)
                    .thenApplyAsync(this::createHead, executor)
                    .thenApply(head->{
                        cacheItem(key, head);
                        return head;
                    })
                    .exceptionally(throwable->createFallbackHead());
        } catch(final RejectedExecutionException exception) {
          return CompletableFuture.completedFuture(createFallbackHead());
        }

        future.whenComplete((item, throwable)->loadingItems.remove(key, future));
        return future;
    }

    @NotNull
    private CompletableFuture<SkullProfile> provideProfile(@NotNull final ProfileKey key) {

        final SkullProfile cached = findCachedProfile(key);

        if(cached != null) {
            return CompletableFuture.completedFuture(copyProfile(cached));
        }

        final CompletableFuture<SkullProfile> future = loadingProfiles.computeIfAbsent(key, this::startProfileLoad);
        return future.thenApply(this::copyProfile);
    }

    @NotNull
    private CompletableFuture<SkullProfile> startProfileLoad(@NotNull final ProfileKey key) {

        final CompletableFuture<SkullProfile> future;

        try {
            future = CompletableFuture.supplyAsync(()->createPlayerProfile(key), executor)
                    .thenCompose(this::updateProfile)
                    .thenApply(this::toSkullProfile)
                    .thenApply(profile->{
                        cacheProfile(key, profile);
                        return profile;
                    })
                    .exceptionally(throwable->createFallbackProfile(key));
        } catch(final RejectedExecutionException exception) {
            return CompletableFuture.completedFuture(createFallbackProfile(key));
        }

        future.whenComplete((profile, throwable)->loadingProfiles.remove(key, future));
        return future;
    }

    @NotNull
    private PlayerProfile createPlayerProfile(@NotNull final ProfileKey key) {

        if(key.uuid() != null) {
            return Bukkit.createProfile(key.uuid(), key.name());
        }

        return Bukkit.createProfile(key.name());
    }

    @NotNull
    private CompletableFuture<PlayerProfile> updateProfile(@NotNull final PlayerProfile profile) {

        if(hasTexture(profile)) {
            return CompletableFuture.completedFuture(profile);
        }

        return profile.update()
                .thenApply(updated->updated)
                .orTimeout(profileTimeoutSeconds, TimeUnit.SECONDS)
                .exceptionally(throwable->profile);
    }

    private boolean hasTexture(@NotNull final PlayerProfile profile) {

        final PlayerTextures textures = profile.getTextures();
        return textures != null && textures.getSkin() != null;
    }

    @NotNull
    private SkullProfile toSkullProfile(@NotNull final PlayerProfile profile) {

        final SkullProfile skullProfile = new SkullProfile();

        if(profile.getUniqueId() != null) {
            skullProfile.uuid(profile.getUniqueId());
        }

        if(profile.getName() != null && !profile.getName().isBlank()) {
            skullProfile.name(profile.getName());
        }

        final String texture = extractTexture(profile);

        if(texture != null) {
            skullProfile.texture(texture);
        }

        return skullProfile;
    }

    @Nullable
    private String extractTexture(@NotNull final PlayerProfile profile) {

        return profile.getProperties()
                .stream()
                .filter(property->"textures".equals(property.getName()))
                .map(property->property.getValue())
                .filter(value->value != null && !value.isBlank())
                .findFirst()
                .orElse(null);
    }

    @NotNull
    private ItemStack createHead(@NotNull final SkullProfile profile) {

        final ItemStack item = new ItemStack(Material.PLAYER_HEAD);

        if(!(item.getItemMeta() instanceof final SkullMeta skullMeta)) {
            return item;
        }

        final UUID uuid = profile.uuid();
        final String name = normalizeName(profile.name());

        if(uuid == null && name == null) {
            return item;
        }

        final PlayerProfile playerProfile;

        if(uuid != null) {
            playerProfile = Bukkit.createProfile(uuid, name);
        } else {
            playerProfile = Bukkit.createProfile(name);
        }

        final String texture = profile.texture();

        if(texture != null && !texture.isBlank()) {
            playerProfile.getProperties().add(new ProfileProperty("textures", texture));
        }

        skullMeta.setOwnerProfile(playerProfile);
        item.setItemMeta(skullMeta);

        return item;
    }

    @NotNull
    private SkullProfile createFallbackProfile(@NotNull final ProfileKey key) {

        final SkullProfile profile = new SkullProfile();

        if(key.uuid() != null) {
            profile.uuid(key.uuid());
        }

        if(key.name() != null) {
            profile.name(key.name());
        }

        return profile;
    }

    @NotNull
    private SkullProfile copyProfile(@NotNull final SkullProfile source) {

        final SkullProfile copy = new SkullProfile();

        if(source.uuid() != null) {
            copy.uuid(source.uuid());
        }

        if(source.name() != null) {
            copy.name(source.name());
        }

        if(source.texture() != null) {
            copy.texture(source.texture());
        }

        return copy;
    }

    private void cacheProfile(@NotNull final ProfileKey requestedKey, @NotNull final SkullProfile profile) {

        profileCache.put(requestedKey, copyProfile(profile));

        if(profile.uuid() != null) {
            profileCache.put(ProfileKey.of(profile.uuid(), null), copyProfile(profile));
        }

        final String name = normalizeName(profile.name());

        if(name != null) {
            profileCache.put(ProfileKey.of(null, name), copyProfile(profile));
        }

        if(profile.uuid() != null && name != null) {
            profileCache.put(ProfileKey.of(profile.uuid(), name), copyProfile(profile));
        }
    }

    private void cacheItem(@NotNull final ProfileKey requestedKey, @NotNull final ItemStack item) {

        itemCache.put(requestedKey, item.clone());

        final SkullProfile profile = findCachedProfile(requestedKey);

        if(profile == null) {
            return;
        }

        if(profile.uuid() != null) {
            itemCache.put(ProfileKey.of(profile.uuid(), null), item.clone());
        }

        final String name = normalizeName(profile.name());

        if(name != null) {
            itemCache.put(ProfileKey.of(null, name), item.clone());
        }

        if(profile.uuid() != null && name != null) {
            itemCache.put(ProfileKey.of(profile.uuid(), name), item.clone());
        }
    }

    @Nullable
    private SkullProfile findCachedProfile(@NotNull final ProfileKey key) {

        SkullProfile profile = profileCache.getIfPresent(key);

        if(profile == null && key.uuid() != null) {
            profile = profileCache.getIfPresent(ProfileKey.of(key.uuid(), null));
        }

        if(profile == null && key.name() != null) {
            profile = profileCache.getIfPresent(ProfileKey.of(null, key.name()));
        }

        return profile;
    }

    @Nullable
    private ItemStack findCachedItem(@NotNull final ProfileKey key) {

        ItemStack item = itemCache.getIfPresent(key);

        if(item == null && key.uuid() != null) {
            item = itemCache.getIfPresent(ProfileKey.of(key.uuid(), null));
        }

        if(item == null && key.name() != null) {
            item = itemCache.getIfPresent(ProfileKey.of(null, key.name()));
        }

        return item;
    }

    private void invalidateMatching(@NotNull final UUID uuid) {

        profileCache.asMap().keySet().removeIf(key->uuid.equals(key.uuid()));
        itemCache.asMap().keySet().removeIf(key->uuid.equals(key.uuid()));

        loadingProfiles.entrySet().removeIf(entry->{
            if(!uuid.equals(entry.getKey().uuid())) {
                return false;
            }

            entry.getValue().cancel(false);
            return true;
        });

        loadingItems.entrySet().removeIf(entry->{
            if(!uuid.equals(entry.getKey().uuid())) {
                return false;
            }

            entry.getValue().cancel(false);
            return true;
        });
    }

    @Nullable
    private static String normalizeName(@Nullable final String name) {

        if(name == null) {
            return null;
        }

        final String normalized = name.strip();

        return normalized.isEmpty()? null : normalized;
    }

    @NotNull
    private static ItemStack createFallbackHead() {

        return new ItemStack(Material.PLAYER_HEAD);
    }

    /**
     * Stops queued profile operations and clears provider-owned state.
     */
    @Override
    public void close() {

        invalidateAll();
        executor.shutdownNow();
    }

    private record ProfileKey(@Nullable UUID uuid, @Nullable String name) {

        private ProfileKey {

            if(uuid == null && name == null) {
                throw new IllegalArgumentException("A profile key requires either a UUID or player name");
            }

            if(name != null) {
                name = name.toLowerCase(Locale.ROOT);
            }
        }

        @NotNull
        private static ProfileKey of(@Nullable final UUID uuid, @Nullable final String name) {

            return new ProfileKey(uuid, normalizeName(name));
        }
    }

    private static final class SkullThreadFactory implements ThreadFactory {

        private final AtomicInteger sequence = new AtomicInteger();

        @Override
        public Thread newThread(@NotNull final Runnable runnable) {

            final Thread thread = new Thread(runnable, "QuickShop-Skull-Provider-" + sequence.incrementAndGet());

            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler((ignored, throwable)->throwable.printStackTrace());

            return thread;
        }
    }
}