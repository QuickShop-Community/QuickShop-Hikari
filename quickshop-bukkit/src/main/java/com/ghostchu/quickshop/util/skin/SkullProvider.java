package com.ghostchu.quickshop.util.skin;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface SkullProvider {

  CompletableFuture<ItemStack> provide(UUID owner);

  CompletableFuture<ItemStack> provide(String owner);
}
