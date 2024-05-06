package com.ghostchu.quickshop.platform.spigot.v1_20_4;

import com.ghostchu.quickshop.platform.Platform;
import com.ghostchu.quickshop.platform.Util;
import com.ghostchu.quickshop.platform.spigot.AbstractSpigotPlatform;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBTList;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.Tag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.craftbukkit.v1_20_R4.CraftServer;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Spigot1205Platform extends AbstractSpigotPlatform implements Platform {

    public Spigot1205Platform(@NotNull Plugin plugin) {
        super(plugin);
    }

    @Override
    public @NotNull HoverEvent<HoverEvent.ShowItem> getItemStackHoverEvent(@NotNull ItemStack stack) {
        NamespacedKey namespacedKey = stack.getType().getKey();
        Key key = Key.key(namespacedKey.toString());
        RegistryAccess access = ((CraftServer) Bukkit.getServer()).getServer().registryAccess();
        Tag tag = CraftItemStack.asNMSCopy(stack).save(access);
        BinaryTagHolder holder;
        if (Util.methodExists(BinaryTagHolder.class, "binaryTagHolder")) {
            holder = BinaryTagHolder.binaryTagHolder(tag.toString());
        } else {
            //noinspection UnstableApiUsage
            holder = BinaryTagHolder.of(tag.toString());
        }
        return HoverEvent.showItem(key, stack.getAmount(), holder);
    }

    @Override
    public @NotNull String getMinecraftVersion() {
        try {
            return ((CraftServer) Bukkit.getServer()).getServer().getServerVersion();
        } catch (Exception e) {
            return super.getMinecraftVersion();
        }
    }

    @Override
    public void registerCommand(@NotNull String prefix, @NotNull Command command) {
        ((CraftServer) Bukkit.getServer()).getCommandMap().register(prefix, command);
        command.register(((CraftServer) Bukkit.getServer()).getCommandMap());
        ((CraftServer) Bukkit.getServer()).syncCommands();
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull Material material) {
        return postProcessingTranslationKey(material.getTranslationKey());
    }

    @Override
    public void setDisplayName(@NotNull ItemStack stack, @Nullable Component component) {
        NBT.modify(stack, nbt->{
            ReadWriteNBT itemComponents = nbt.getOrCreateCompound("components");
            if (component == null) {
                itemComponents.removeKey("minecraft:custom_name");
            } else {
                itemComponents.setString("minecraft:custom_name", GsonComponentSerializer.gson().serialize(component));
            }
        });
    }


    @Override
    public void setLore(@NotNull ItemStack stack, @NotNull Collection<Component> components) {
        NBT.modify(stack, nbt->{
            ReadWriteNBT itemComponents = nbt.getOrCreateCompound("components");
            if (components.isEmpty()) {
                itemComponents.removeKey("minecraft:lores");
            } else {
                List<String> gson = components.stream().map(c->GsonComponentSerializer.gson().serialize(c)).toList();
                ReadWriteNBTList<String > list = itemComponents.getStringList("minecraft:lores");
                list.clear();
                list.addAll(gson);
            }
        });
    }

    private String postProcessingTranslationKey(String key) {
        return this.translationMapping.getOrDefault(key, key);
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull EntityType type) {
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
    public @NotNull String getTranslationKey(@NotNull PotionEffectType potionEffectType) {
        return postProcessingTranslationKey(potionEffectType.getTranslationKey());
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull Enchantment enchantment) {
        return postProcessingTranslationKey(enchantment.getTranslationKey());
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull ItemStack stack) {
        return postProcessingTranslationKey(stack.getTranslationKey());
    }
}
