package com.ghostchu.quickshop.platform.paper;

import com.ghostchu.quickshop.common.util.QuickSLF4JLogger;
import com.ghostchu.quickshop.platform.Platform;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("removal")
public class PaperPlatform implements Platform {

  private Map<String, String> translationMapping;

  public PaperPlatform() {

    this.translationMapping = new HashMap<>();
  }

  @Override
  public void shutdown() {

  }

  @Override
  public @NotNull String encodeStack(@NotNull final ItemStack stack) {

    return Base64.getEncoder().encodeToString(stack.serializeAsBytes());
  }

  @Override
  public ItemStack decodeStack(@NotNull final String serialized) {

    return ItemStack.deserializeBytes(Base64.getDecoder().decode(serialized));
  }

  @Override
  public @NotNull Component getDisplayName(@NotNull final ItemStack stack) {

    return stack.displayName();
  }

  @Override
  public @NotNull Component getDisplayName(@NotNull final ItemMeta meta) {

    final Component displayName = meta.displayName();
    if(displayName == null) {
      return Component.empty();
    }
    return displayName;
  }


  @Override
  public @NotNull Component getLine(@NotNull final Sign sign, final int line) {

    return sign.line(line);
  }

  @Override
  public @Nullable List<Component> getLore(@NotNull final ItemStack stack) {

    return stack.lore();
  }

  @Override
  public @Nullable List<Component> getLore(@NotNull final ItemMeta meta) {

    return meta.lore();
  }

  @Override
  public @NotNull String getMinecraftVersion() {

    return Bukkit.getMinecraftVersion();
  }

  @Override
  public @NotNull Component setItemStackHoverEvent(@NotNull final Component oldComponent, @NotNull final ItemStack stack) {

    return oldComponent.hoverEvent(stack.asHoverEvent());
  }

  @Override
  public @NotNull Component getTranslation(@NotNull final Material material) {

    return Component.translatable(getTranslationKey(material));
  }

  private String postProcessingTranslationKey(final String key) {

    return this.translationMapping.getOrDefault(key, key);
  }

  @Override
  public @NotNull Component getTranslation(@NotNull final EntityType entity) {

    return Component.translatable(getTranslationKey(entity));
  }

  @Override
  public @NotNull Component getTranslation(@NotNull final PotionEffectType potionEffectType) {

    return Component.translatable(getTranslationKey(potionEffectType));
  }

  @Override
  public @NotNull Component getTranslation(@NotNull final Enchantment enchantment) {

    return Component.translatable(getTranslationKey(enchantment));
  }

  @Override
  public @NotNull Component getTranslation(@NotNull final ItemStack itemStack) {

    return Component.translatable(getTranslationKey(itemStack));
  }

  @Override
  public @NotNull String getTranslationKey(@NotNull final Material material) {

    String key;
    try {
      key = material.translationKey();
    } catch(final Throwable error) {
      key = material.getTranslationKey();
    }
    return postProcessingTranslationKey(key);
  }

  @Override
  public @NotNull String getTranslationKey(@NotNull final EntityType type) {

    String key;
    try {
      key = type.translationKey();
    } catch(final Throwable error) {
      key = type.getTranslationKey();
    }
    return postProcessingTranslationKey(key);
  }

  @Override
  public @NotNull String getTranslationKey(@NotNull final PotionEffectType potionEffectType) {

    final String key = potionEffectType.translationKey();
    return postProcessingTranslationKey(key);
  }

  @Override
  public @NotNull String getTranslationKey(@NotNull final Enchantment enchantment) {

    final String key = enchantment.translationKey();
    return postProcessingTranslationKey(key);
  }

  @Override
  public @NotNull String getTranslationKey(@NotNull final ItemStack stack) {

    String key;
    try {
      key = stack.getTranslationKey();
    } catch(final Throwable error) {
      key = stack.translationKey();
    }
    return postProcessingTranslationKey(key);
  }

  @Override
  public @NotNull MiniMessage miniMessage() {

    return MiniMessage.miniMessage();
  }

  @Override
  public void registerCommand(@NotNull final String prefix, @NotNull final Command command) {

    Bukkit.getCommandMap().register(prefix, command);
    command.register(Bukkit.getCommandMap());
    Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
  }

  @Override
  public void sendMessage(@NotNull final CommandSender sender, @NotNull final Component component) {

    sender.sendMessage(component);
  }

  @Override
  public void sendSignTextChange(@NotNull final Player player, @NotNull final Sign sign, final boolean glowing, @NotNull final List<Component> components) {

    player.sendSignChange(sign.getLocation(), components);
  }

  @Override
  public void setDisplayName(@NotNull final ItemStack stack, @Nullable final Component component) {

    final ItemMeta meta = stack.getItemMeta();
    meta.displayName(component);
    stack.setItemMeta(meta);
  }

  @Override
  public void setDisplayName(@NotNull final Entity entity, @Nullable final Component component) {

    entity.customName(component);
  }

  @Override
  public void setLines(@NotNull final Sign sign, @NotNull final List<Component> component) {

    for(int i = 0; i < Math.min(component.size(), 4); i++) {
      sign.line(i, component.get(i));
    }
    sign.update(true, false);
  }

  @Override
  public void setLore(@NotNull final ItemStack stack, @NotNull final Collection<Component> components) {

    final ItemMeta meta = stack.getItemMeta();
    meta.lore(new ArrayList<>(components));
    stack.setItemMeta(meta);
  }


  @Override
  public void updateTranslationMappingSection(@NotNull final Map<String, String> mapping) {

    this.translationMapping = mapping;
  }

  @Override
  public @NotNull Logger getSlf4jLogger(@NotNull final Plugin parent) {

    try {
      return parent.getSLF4JLogger();
    } catch(final Throwable th) {
      return QuickSLF4JLogger.initializeLoggerService(parent.getLogger());
    }
  }
}
