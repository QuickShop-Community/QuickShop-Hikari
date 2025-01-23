package com.ghostchu.quickshop.platform.spigot.v1_18_2;

import com.ghostchu.quickshop.platform.Platform;
import com.ghostchu.quickshop.platform.spigot.AbstractSpigotPlatform;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.v1_18_R2.potion.CraftPotionEffectType;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class Spigot1182Platform extends AbstractSpigotPlatform implements Platform {

  public Spigot1182Platform(@NotNull final Plugin plugin) {

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
      return CraftMagicNumbers.getBlock(material).getDescriptionId();
    } else {
      return postProcessingTranslationKey(CraftMagicNumbers.getItem(material).getDescriptionId());
    }
  }

  private String postProcessingTranslationKey(final String key) {

    return this.translationMapping.getOrDefault(key, key);
  }

  @Override
  public @NotNull String getTranslationKey(@NotNull final EntityType type) {

    final Optional<net.minecraft.world.entity.EntityType<?>> op = net.minecraft.world.entity.EntityType.byString(type.getKey().toString());
    if(op.isPresent()) {
      return postProcessingTranslationKey(op.get().getDescriptionId());
    } else {
      return postProcessingTranslationKey("entity." + type.getKey());
    }
  }

  @Override
  public @NotNull String getTranslationKey(@NotNull final PotionEffectType potionEffectType) {

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

    return getTranslationKey(stack.getType());
  }
}
