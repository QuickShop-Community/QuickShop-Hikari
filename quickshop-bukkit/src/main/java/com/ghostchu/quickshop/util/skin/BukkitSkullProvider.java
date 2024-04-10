package com.ghostchu.quickshop.util.skin;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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

public class BukkitSkullProvider implements SkullProvider {
    private final Cache<Object, ItemStack> profileCache = CacheBuilder.newBuilder()
            .expireAfterAccess(12, TimeUnit.HOURS)
            .maximumSize(512)
            .build();

    @Override
    public CompletableFuture<ItemStack> provide(UUID owner) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return profileCache.get(owner, () -> load(owner));
            } catch (ExecutionException e) {
                e.printStackTrace();
                return new ItemStack(Material.PLAYER_HEAD);
            }
        });
    }


    @Override
    public CompletableFuture<ItemStack> provide(String owner) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return profileCache.get(owner, () -> load(owner));
            } catch (ExecutionException e) {
                e.printStackTrace();
                return new ItemStack(Material.PLAYER_HEAD);
            }
        });
    }

    @NotNull
    private ItemStack load(OfflinePlayer offlinePlayer) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = head.getItemMeta();
        PlayerProfile profile = offlinePlayer.getPlayerProfile();
        if (!profile.isComplete()) {
            try {
                profile = profile.update().get(10, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ignored) {
            }
        }
        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwnerProfile(profile);
            head.setItemMeta(meta);
        }
        return head;
    }

    private ItemStack load(UUID uuid) {
        return load(Bukkit.getOfflinePlayer(uuid));
    }

    private ItemStack load(String owner) {
        return load(Bukkit.getOfflinePlayer(owner));
    }
}
