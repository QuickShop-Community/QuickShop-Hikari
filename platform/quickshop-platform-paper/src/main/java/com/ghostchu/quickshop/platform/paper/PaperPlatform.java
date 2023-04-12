package com.ghostchu.quickshop.platform.paper;

import com.ghostchu.quickshop.platform.Platform;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

@SuppressWarnings("removal")
public class PaperPlatform implements Platform {

    private Map<String, String> translationMapping;

    public PaperPlatform() {
        this.translationMapping = new HashMap<>();
    }

    @Override
    public @NotNull Component getDisplayName(@NotNull ItemStack stack) {
        return stack.displayName();
    }

    @Override
    public @NotNull Component getDisplayName(@NotNull ItemMeta meta) {
        Component displayName = meta.displayName();
        if (displayName == null) {
            return Component.empty();
        }
        return displayName;
    }

    @Override
    public @NotNull HoverEvent<HoverEvent.ShowItem> getItemStackHoverEvent(@NotNull ItemStack stack) {
        return stack.asHoverEvent();
    }

    @Override
    public @NotNull Component getLine(@NotNull Sign sign, int line) {
        return sign.line(line);
    }

    @Override
    public @Nullable List<Component> getLore(@NotNull ItemStack stack) {
        return stack.lore();
    }

    @Override
    public @Nullable List<Component> getLore(@NotNull ItemMeta meta) {
        return meta.lore();
    }

    @Override
    public @NotNull String getMinecraftVersion() {
        return Bukkit.getMinecraftVersion();
    }

    @Override
    public @NotNull Component getTranslation(@NotNull Material material) {
        return Component.translatable(getTranslationKey(material));
    }

    private String postProcessingTranslationKey(String key) {
        return this.translationMapping.getOrDefault(key, key);
    }

    @Override
    public @NotNull Component getTranslation(@NotNull EntityType entity) {
        return Component.translatable(getTranslationKey(entity));
    }

    @Override
    public @NotNull Component getTranslation(@NotNull PotionEffectType potionEffectType) {
        return Component.translatable(getTranslationKey(potionEffectType));
    }

    @Override
    public @NotNull Component getTranslation(@NotNull Enchantment enchantment) {
        return Component.translatable(getTranslationKey(enchantment));
    }

    @Override
    public @NotNull Component getTranslation(@NotNull ItemStack itemStack) {
        return Component.translatable(getTranslationKey(itemStack));
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull Material material) {
        String key;
        try {
            key = material.translationKey();
        } catch (Throwable error) {
            key = material.getTranslationKey();
        }
        return postProcessingTranslationKey(key);
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull EntityType type) {
        String key;
        try {
            key = type.translationKey();
        } catch (Throwable error) {
            key = type.getTranslationKey();
        }
        return postProcessingTranslationKey(key);
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull PotionEffectType potionEffectType) {
        String key = potionEffectType.translationKey();
        return postProcessingTranslationKey(key);
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull Enchantment enchantment) {
        String key = enchantment.translationKey();
        return postProcessingTranslationKey(key);
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull ItemStack stack) {
        String key;
        try {
            key = stack.getTranslationKey();
        } catch (Throwable error) {
            key = stack.translationKey();
        }
        return postProcessingTranslationKey(key);
    }

    @Override
    public @NotNull MiniMessage miniMessage() {
        return MiniMessage.miniMessage();
    }

    @Override
    public void registerCommand(@NotNull String prefix, @NotNull Command command) {
        Bukkit.getCommandMap().register(prefix, command);
        command.register(Bukkit.getCommandMap());
        Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
    }

    @Override
    public void sendMessage(@NotNull CommandSender sender, @NotNull Component component) {
        sender.sendMessage(component);
    }

    @Override
    public void sendSignTextChange(@NotNull Player player, @NotNull Sign sign, boolean glowing, @NotNull List<Component> components) {
        player.sendSignChange(sign.getLocation(), components);
    }

    @Override
    public void setDisplayName(@NotNull ItemStack stack, @Nullable Component component) {
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(component);
        stack.setItemMeta(meta);
    }

    @Override
    public void setDisplayName(@NotNull Item stack, @Nullable Component component) {
        stack.customName(component);
    }

    @Override
    public void setLine(@NotNull Sign sign, int line, @NotNull Component component) {
        sign.line(line, component);
        sign.update(true, false);
    }

    @Override
    public void setLines(@NotNull Sign sign, @NotNull List<Component> component) {
        for (int i = 0; i < Math.min(component.size(), 4); i++) {
            sign.line(i, component.get(i));
        }
        sign.update(true, false);
    }

    @Override
    public void setLore(@NotNull ItemStack stack, @NotNull Collection<Component> components) {
        stack.lore(new ArrayList<>(components));
    }


    @Override
    public void updateTranslationMappingSection(@NotNull Map<String, String> mapping) {
        this.translationMapping = mapping;
    }

    @Override
    public @NotNull Logger getSlf4jLogger(@NotNull Plugin parent) {
        return parent.getSLF4JLogger();
    }
}
