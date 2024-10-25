package com.ghostchu.quickshop.platform.spigot.v1_21_3;

import com.ghostchu.quickshop.platform.Platform;
import com.ghostchu.quickshop.platform.spigot.AbstractSpigotPlatform;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.craftbukkit.v1_21_R2.CraftServer;
import org.bukkit.craftbukkit.v1_21_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class Spigot1231Platform extends AbstractSpigotPlatform implements Platform {

  public Spigot1231Platform(@NotNull final Plugin plugin) {

    super(plugin);
  }

  @Override
  public @NotNull Component setItemStackHoverEvent(@NotNull final Component oldComponent, @NotNull final ItemStack stack) {

    return oldComponent.hoverEvent(HoverEvent.showText(Component.text("Click to preview the item")
                                                               .appendNewline()
                                                               .append(Component.text("× Item hover preview are not available at this time (Spigot 1.20.5+) due major item nbt related changes.").color(NamedTextColor.RED))
                                                               .appendNewline()
                                                               .append(Component.text("💡 Consider switch to Paper to use PaperAPI instead.").color(NamedTextColor.GRAY))));

  }

  @Override
  public @NotNull String getMinecraftVersion() {

    try {
      return ((CraftServer)Bukkit.getServer()).getServer().getServerVersion();
    } catch(final Exception e) {
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

    return postProcessingTranslationKey(material.getTranslationKey());
  }

  @Override
  public void setDisplayName(@NotNull final ItemStack stack, @Nullable final Component component) {

    final net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(stack);
    if(component == null) {
      nmsItemStack.remove(DataComponents.CUSTOM_NAME);
      return;
    }
    final String json = GsonComponentSerializer.gson().serialize(component);
    nmsItemStack.set(DataComponents.CUSTOM_NAME,
                     net.minecraft.network.chat.Component.Serializer
                             .fromJson(json,
                                       ((CraftServer)Bukkit.getServer())
                                               .getServer()
                                               .registryAccess()));
    stack.setItemMeta(CraftItemStack.asBukkitCopy(nmsItemStack).getItemMeta());
  }


  @Override
  public void setLore(@NotNull final ItemStack stack, @NotNull final Collection<Component> components) {

    final net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(stack);
    if(components.isEmpty()) {
      nmsItemStack.remove(DataComponents.LORE);
    } else {
      final List<net.minecraft.network.chat.Component> componentsList = components.stream()
              .map(c->GsonComponentSerializer.gson().serialize(c))
              .map(json->(net.minecraft.network.chat.Component)
                      net.minecraft.network.chat.Component.Serializer
                              .fromJson(json,
                                        ((CraftServer)Bukkit.getServer())
                                                .getServer()
                                                .registryAccess()))
              .toList();
      nmsItemStack.set(DataComponents.LORE, new ItemLore(componentsList));
    }
    stack.setItemMeta(CraftItemStack.asBukkitCopy(nmsItemStack).getItemMeta());
  }

  private String postProcessingTranslationKey(final String key) {

    return this.translationMapping.getOrDefault(key, key);
  }

  @Override
  public @NotNull String getTranslationKey(@NotNull final EntityType type) {
    //noinspection deprecation
    return postProcessingTranslationKey(type.getTranslationKey());
//        Optional<net.minecraft.world.entity.EntityType<?>> op = net.minecraft.world.entity.EntityType.byString(type.getKey().toString());
//        if (op.isPresent()) {
//            return postProcessingTranslationKey(op.get().getDescriptionId());
//        } else {
//            return postProcessingTranslationKey("entity." + type.getKey());
//        }
  }

  @Override
  public @NotNull String getTranslationKey(@NotNull final PotionEffectType potionEffectType) {

    return postProcessingTranslationKey(potionEffectType.getTranslationKey());
  }

  @Override
  public @NotNull String getTranslationKey(@NotNull final Enchantment enchantment) {

    return postProcessingTranslationKey(enchantment.getTranslationKey());
  }

  @Override
  public @NotNull String getTranslationKey(@NotNull final ItemStack stack) {

    return postProcessingTranslationKey(stack.getTranslationKey());
  }
}
