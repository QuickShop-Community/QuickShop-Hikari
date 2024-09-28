package com.ghostchu.quickshop.platform.spigot;

import com.ghostchu.quickshop.common.util.QuickSLF4JLogger;
import com.ghostchu.quickshop.platform.Platform;
import com.ghostchu.quickshop.platform.Util;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBTList;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class AbstractSpigotPlatform implements Platform {

  public final Plugin plugin;
  protected final Logger logger = Logger.getLogger("QuickShop-Hikari");
  protected Map<String, String> translationMapping;
  private BukkitAudiences audience;

  public AbstractSpigotPlatform(@NotNull final Plugin instance) {

    this.plugin = instance;
    //TODO use method to replace
    if(Bukkit.getPluginManager().getPlugin("NBTAPI") == null) {
      throw new IllegalStateException("Must install NBT-API if you're running on Spigot server");
    }
    //this.translationMapping = mapping;
  }

  @NotNull
  @ApiStatus.Internal
  @Deprecated
  public static String getNMSVersion() {
    // Should only works on Spigot, Paper are breaking this, do not use if possible
    final String name = Bukkit.getServer().getClass().getPackage().getName();
    return name.substring(name.lastIndexOf('.') + 1);
  }

  @Override
  public @NotNull Component getDisplayName(@NotNull final ItemStack stack) {

    if(stack.getItemMeta() != null) {
      return LegacyComponentSerializer.legacySection().deserialize(stack.getItemMeta().getDisplayName());
    }
    return Component.empty();
  }

  @Override
  public @NotNull Component getDisplayName(@NotNull final ItemMeta meta) {

    if(meta.hasDisplayName()) {
      return LegacyComponentSerializer.legacySection().deserialize(meta.getDisplayName());
    }
    return Component.empty();
  }

  @Override
  public @NotNull Component getLine(@NotNull final Sign sign, final int line) {

    return LegacyComponentSerializer.legacySection().deserialize(sign.getLine(line));
  }

  @Override
  public @Nullable List<Component> getLore(@NotNull final ItemStack stack) {

    if(!stack.hasItemMeta()) {
      return null;
    }
    if(!stack.getItemMeta().hasLore()) {
      return null;
    }
    return stack.getItemMeta().getLore().stream().map(LegacyComponentSerializer.legacySection()::deserialize).collect(Collectors.toList());
  }

  @Override
  public @Nullable List<Component> getLore(@NotNull final ItemMeta meta) {

    if(!meta.hasLore()) {
      return null;
    }
    return meta.getLore().stream().map(LegacyComponentSerializer.legacySection()::deserialize).collect(Collectors.toList());
  }

  @Override
  public @NotNull Component getTranslation(@NotNull final Material material) {

    return Component.translatable(getTranslationKey(material));
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
  public @NotNull MiniMessage miniMessage() {

    return MiniMessage.miniMessage();
  }

  @Override
  public void sendMessage(@NotNull final CommandSender sender, @NotNull final Component component) {

    if(this.audience == null) {
      this.audience = BukkitAudiences.create(this.plugin);
    }
    //this.audience.sender(sender).sendMessage(component);
    sender.spigot().sendMessage(BungeeComponentSerializer.get().serialize(component));
  }

  @Override
  public void shutdown() {

    if(this.audience != null) {
      this.audience.close();
    }
  }

  @Override
  public void sendSignTextChange(@NotNull final Player player, @NotNull final Sign sign, final boolean glowing, @NotNull final List<Component> components) {
    //player.sendSignChange(sign.getLocation(), components.stream().map(com -> LegacyComponentSerializer.legacySection().serialize(com)).toArray(String[]::new));
  }

  @Override
  public @NotNull Component setItemStackHoverEvent(@NotNull final Component oldComponent, @NotNull final ItemStack stack) {

    final NamespacedKey namespacedKey = stack.getType().getKey();
    final Key key = Key.key(namespacedKey.toString());
    final ReadWriteNBT nbt = NBT.itemStackToNBT(stack);
    final BinaryTagHolder holder;
    if(Util.methodExists(BinaryTagHolder.class, "binaryTagHolder")) {
      holder = BinaryTagHolder.binaryTagHolder(nbt.toString());
    } else {
      //noinspection UnstableApiUsage
      holder = BinaryTagHolder.of(nbt.toString());
    }
    final HoverEvent he = HoverEvent.showItem(key, stack.getAmount(), holder);
    return oldComponent.hoverEvent(he);
  }

  @Override
  public void setLore(@NotNull final ItemStack stack, @NotNull final Collection<Component> components) {

    NBT.modify(stack, nbt->{
      final ReadWriteNBT display = nbt.getOrCreateCompound("display");
      if(components.isEmpty()) {
        display.removeKey("Lore");
      } else {
        final List<String> gson = components.stream().map(c->GsonComponentSerializer.gson().serialize(c)).toList();
        final ReadWriteNBTList<String> list = display.getStringList("Lore");
        list.clear();
        list.addAll(gson);
      }
    });
  }

  @Override
  public void setDisplayName(@NotNull final ItemStack stack, @Nullable final Component component) {

    NBT.modify(stack, nbt->{
      final ReadWriteNBT display = nbt.getOrCreateCompound("display");
      if(component == null) {
        display.removeKey("Name");
      } else {
        display.setString("Name", GsonComponentSerializer.gson().serialize(component));
      }
    });
  }


  @Override
  public void setDisplayName(@NotNull final Entity entity, @Nullable final Component component) {

    NBT.modify(entity, nbt->{
      if(component == null) {
        nbt.removeKey("CustomName");
      } else {
        nbt.setString("CustomName", GsonComponentSerializer.gson().serialize(component));
      }
    });
  }

  @Override
  public void setLines(@NotNull final Sign sign, @NotNull final List<Component> component) {

    final String EMPTY_LINE_NBT = "{\"text\":\"\"}";
    final ReadWriteNBT root = NBT.createNBTObject();
    final ReadWriteNBT front_text = root.getOrCreateCompound("front_text"); // > 1.20
    final ReadWriteNBTList<String> messages = front_text.getStringList("messages"); // > 1.20
    for(int i = 0; i < 4; i++) {
      final Component com = component.get(i);
      final String json = com == null? EMPTY_LINE_NBT : GsonComponentSerializer.gson().serialize(com);
      root.setString("Text" + (i + 1), json);
      messages.add(json); // > 1.20
    }
    // ==== Apply the changes ====
    NBT.modify(sign, nbt->{
      nbt.mergeCompound(root);
    });
  }

  @Override
  public void updateTranslationMappingSection(@NotNull final Map<String, String> mapping) {

    this.translationMapping = mapping;
  }

  @Override
  @NotNull
  public org.slf4j.Logger getSlf4jLogger(@NotNull final Plugin parent) {

    return QuickSLF4JLogger.initializeLoggerService(parent.getLogger());
  }

  private String postProcessingTranslationKey(final String key) {

    return this.translationMapping.getOrDefault(key, key);
  }
}
