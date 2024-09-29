package com.ghostchu.quickshop.platform.spigot.v1_19_2;

import com.ghostchu.quickshop.platform.Platform;
import com.ghostchu.quickshop.platform.spigot.AbstractSpigotPlatform;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.craftbukkit.v1_19_R2.CraftServer;
import org.bukkit.craftbukkit.v1_19_R2.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.v1_19_R2.potion.CraftPotionEffectType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeWrapper;
import org.jetbrains.annotations.NotNull;

public class Spigot1193Platform extends AbstractSpigotPlatform implements Platform {

  public Spigot1193Platform(@NotNull final Plugin plugin) {

    super(plugin);
  }

  @Override
  public @NotNull String getMinecraftVersion() {

    try {
      return ((CraftServer)Bukkit.getServer()).getServer().getServerVersion();
    } catch(Exception e) {
      return super.getMinecraftVersion();
    }
  }

  @Override
  public void registerCommand(@NotNull final String prefix, @NotNull final Command command) {

    ((CraftServer)Bukkit.getServer()).getCommandMap().register(prefix, command);
    command.register(((CraftServer)Bukkit.getServer()).getCommandMap());
    ((CraftServer)Bukkit.getServer()).syncCommands();
  }

  @Override
  public @NotNull String getTranslationKey(@NotNull final Material material) {

    if(material.isBlock()) {
      //noinspection deprecation
      return postProcessingTranslationKey(Bukkit.getUnsafe().getBlockTranslationKey(material));
    } else {
      //noinspection deprecation
      return postProcessingTranslationKey(Bukkit.getUnsafe().getItemTranslationKey(material));
    }
  }

  private String postProcessingTranslationKey(final String key) {

    return this.translationMapping.getOrDefault(key, key);
  }

  @Override
  public @NotNull String getTranslationKey(@NotNull final EntityType type) {
    //noinspection deprecation
    return postProcessingTranslationKey(Bukkit.getUnsafe().getTranslationKey(type));
  }

  @Override
  public @NotNull String getTranslationKey(@NotNull PotionEffectType potionEffectType) {

    if(potionEffectType instanceof PotionEffectTypeWrapper wrapper) {
      potionEffectType = wrapper.getType();
    }
    final CraftPotionEffectType craftPotionEffectType = (CraftPotionEffectType)potionEffectType;
    return postProcessingTranslationKey(craftPotionEffectType.getHandle().getDescriptionId());
  }

  @Override
  public @NotNull String getTranslationKey(@NotNull final Enchantment enchantment) {

    final CraftEnchantment craftEnchantment = (CraftEnchantment)enchantment;
    return postProcessingTranslationKey(craftEnchantment.getHandle().getDescriptionId());
  }

  @Override
  public @NotNull String getTranslationKey(@NotNull final ItemStack stack) {

    return postProcessingTranslationKey(stack.getTranslationKey());
  }
}
