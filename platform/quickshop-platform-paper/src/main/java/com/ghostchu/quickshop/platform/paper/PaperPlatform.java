package com.ghostchu.quickshop.platform.paper;

import com.ghostchu.quickshop.platform.Platform;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class PaperPlatform implements Platform {

    private Map<String, String> translationMapping;

    public PaperPlatform(Map<String, String> mapping) {
        this.translationMapping = mapping;
    }


    @Override
    public void setLine(@NotNull Sign sign, int line, @NotNull Component component) {
        sign.line(line, component);
    }

    @Override
    public @NotNull Component getLine(@NotNull Sign sign, int line) {
        return sign.line(line);
    }

    @Override
    public @NotNull HoverEvent<HoverEvent.ShowItem> getItemStackHoverEvent(@NotNull ItemStack stack) {
        return stack.asHoverEvent();
    }

    @Override
    public void registerCommand(@NotNull String prefix, @NotNull PluginCommand command) {
        Bukkit.getCommandMap().register(prefix, command);
        Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
    }

    @Override
    public @NotNull String getMinecraftVersion() {
        return Bukkit.getMinecraftVersion();
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull Material material) {
        String key;
        try {
            return material.translationKey();
        } catch (Error error) {
            try {
                key = material.getTranslationKey();
            } catch (Error error2) {
                if (!material.isBlock())
                    key = "item." + material.getKey().getNamespace() + "." + material.getKey().getKey();
                else
                    key = "block." + material.getKey().getNamespace() + "." + material.getKey().getKey();
            }
        }
        return postProcessingTranslationKey(key);
    }

    private String postProcessingTranslationKey(String key) {
        return this.translationMapping.getOrDefault(key, key);
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull EntityType type) {
        String key;
        try {
            key = type.translationKey();
        } catch (Error error) {
            try {
                key = type.getTranslationKey();
            } catch (Error error2) {
                key = "entity." + type.getKey().getNamespace() + "." + type.getKey().getKey();
            }
        }
        return postProcessingTranslationKey(key);
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull PotionEffectType potionEffectType) {
        String key;
        try {
            key = potionEffectType.translationKey();
        } catch (Error error) {
            key = "effect." + potionEffectType.getKey().getNamespace() + "." + potionEffectType.getKey().getKey();
        }
        return postProcessingTranslationKey(key);
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull Enchantment enchantment) {
        String key;
        try {
            key = enchantment.translationKey();
        } catch (Error error) {
            key = enchantment.getKey().getNamespace() + "." + enchantment.getKey().getKey();
        }
        return postProcessingTranslationKey(key);
    }

    @Override
    public @NotNull Component getTranslation(@NotNull Material material) {
        return Component.translatable(getTranslationKey(material));
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
    public @NotNull Component getDisplayName(@NotNull ItemStack stack) {
        return stack.displayName();
    }

    @Override
    public @NotNull Component getDisplayName(@NotNull ItemMeta meta) {
        Component displayName = meta.displayName();
        if (displayName == null)
            return Component.empty();
        return displayName;
    }

    @Override
    public void setDisplayName(@NotNull ItemMeta meta, @Nullable Component component) {
        meta.displayName(component);
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
    public void updateTranslationMappingSection(@NotNull Map<String, String> mapping) {
        this.translationMapping = mapping;
    }

    @Override
    public void setLore(@NotNull ItemStack stack, @NotNull Collection<Component> components) {
        stack.lore(new ArrayList<>(components));
    }

    @Override
    public void setLore(@NotNull ItemMeta meta, @NotNull Collection<Component> components) {
        meta.lore(new ArrayList<>(components));
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
    public void sendMessage(@NotNull CommandSender sender, @NotNull Component component) {
        sender.sendMessage(component);
    }

    @Override
    public @NotNull MiniMessage miniMessage() {
        return MiniMessage.miniMessage();
    }
}
