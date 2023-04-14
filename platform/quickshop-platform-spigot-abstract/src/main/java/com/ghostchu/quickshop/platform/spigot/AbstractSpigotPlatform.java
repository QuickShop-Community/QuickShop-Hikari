package com.ghostchu.quickshop.platform.spigot;

import com.ghostchu.quickshop.common.util.QuickSLF4JLogger;
import com.ghostchu.quickshop.platform.Platform;
import de.tr7zw.nbtapi.NBT;
import me.pikamug.localelib.LocaleManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class AbstractSpigotPlatform implements Platform {
    protected final Logger logger = Logger.getLogger("QuickShop-Hikari");
    protected final LocaleManager localeManager = new LocaleManager();
    private final Plugin plugin;
    protected Map<String, String> translationMapping;
    private BukkitAudiences audience;

    public AbstractSpigotPlatform(@NotNull Plugin instance) {
        this.plugin = instance;
        //TODO use method to replace
        if (Bukkit.getPluginManager().getPlugin("NBTAPI") == null) {
            throw new IllegalStateException("Must install NBT-API if you're running on Spigot server");
        }
        //this.translationMapping = mapping;
    }

    @NotNull
    public static String getNMSVersion() {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    @Override
    public @NotNull Component getDisplayName(@NotNull ItemStack stack) {
        if (stack.getItemMeta() != null) {
            return LegacyComponentSerializer.legacySection().deserialize(stack.getItemMeta().getDisplayName());
        }
        return Component.empty();
    }

    @Override
    public @NotNull Component getDisplayName(@NotNull ItemMeta meta) {
        if (meta.hasDisplayName()) {
            return LegacyComponentSerializer.legacySection().deserialize(meta.getDisplayName());
        }
        return Component.empty();
    }

    @Override
    public @NotNull Component getLine(@NotNull Sign sign, int line) {
        return LegacyComponentSerializer.legacySection().deserialize(sign.getLine(line));
    }

    @Override
    public @Nullable List<Component> getLore(@NotNull ItemStack stack) {
        if (!stack.hasItemMeta()) {
            return null;
        }
        if (!stack.getItemMeta().hasLore()) {
            return null;
        }
        return stack.getItemMeta().getLore().stream().map(LegacyComponentSerializer.legacySection()::deserialize).collect(Collectors.toList());
    }

    @Override
    public @Nullable List<Component> getLore(@NotNull ItemMeta meta) {
        if (!meta.hasLore()) {
            return null;
        }
        return meta.getLore().stream().map(LegacyComponentSerializer.legacySection()::deserialize).collect(Collectors.toList());
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
    public @NotNull Component getTranslation(@NotNull ItemStack itemStack) throws Throwable {
        return Component.translatable(getTranslationKey(itemStack));
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull Material material) {
        return postProcessingTranslationKey(localeManager.queryMaterial(material));
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull EntityType type) {
        return postProcessingTranslationKey(localeManager.queryEntityType(type, null));
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull PotionEffectType potionEffectType) {
        String key;
        key = "effect." + potionEffectType.getKey().getNamespace() + "." + potionEffectType.getKey().getKey();
        return postProcessingTranslationKey(key);
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull Enchantment enchantment) {
        return postProcessingTranslationKey(localeManager.queryEnchantments(Map.of(enchantment, 1)).getOrDefault(enchantment, "Unknown"));
    }

    @Override
    public @NotNull MiniMessage miniMessage() {
        return MiniMessage.miniMessage();
    }


    @Override
    public void sendMessage(@NotNull CommandSender sender, @NotNull Component component) {
        if (this.audience == null) {
            this.audience = BukkitAudiences.create(this.plugin);
        }
        this.audience.sender(sender).sendMessage(component);
    }

    @Override
    public void sendSignTextChange(@NotNull Player player, @NotNull Sign sign, boolean glowing, @NotNull List<Component> components) {
        //player.sendSignChange(sign.getLocation(), components.stream().map(com -> LegacyComponentSerializer.legacySection().serialize(com)).toArray(String[]::new));
    }

    @Override
    public void setDisplayName(@NotNull ItemStack stack, @Nullable Component component) {
        if (stack.getItemMeta() == null) {
            return;
        }
        ItemMeta meta = stack.getItemMeta();
        if (component == null) {
            meta.setDisplayName(null);
        } else {
            meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(component));
        }
        stack.setItemMeta(meta);
    }

    @Override
    public void setDisplayName(@NotNull Item stack, @Nullable Component component) {
        if (component == null) {
            stack.setCustomName(null);
        } else {
            stack.setCustomName(LegacyComponentSerializer.legacySection().serialize(component));
        }
    }

    @Override
    public void setLine(@NotNull Sign sign, int line, @NotNull Component component) {
        NBT.modify(sign, nbt -> {
            nbt.setString("Text" + (line + 1), GsonComponentSerializer.gson().serialize(component));
        });
    }

    @Override
    public void setLines(@NotNull Sign sign, @NotNull List<Component> component) {
        NBT.modify(sign, nbt -> {
            for (int i = 0; i < Math.min(component.size(),4); i++) {
                nbt.setString("Text" + (i + 1), GsonComponentSerializer.gson().serialize(component.get(i)));
            }
        });
    }

    @Override
    public void setLore(@NotNull ItemStack stack, @NotNull Collection<Component> components) {
        if (!stack.hasItemMeta()) {
            return;
        }
        ItemMeta meta = stack.getItemMeta();
        meta.setLore(components.stream().map(LegacyComponentSerializer.legacySection()::serialize).collect(Collectors.toList()));
        stack.setItemMeta(meta);
    }

    @Override
    public void updateTranslationMappingSection(@NotNull Map<String, String> mapping) {
        this.translationMapping = mapping;
    }

    @Override
    @NotNull
    public org.slf4j.Logger getSlf4jLogger(@NotNull Plugin parent) {
        return QuickSLF4JLogger.initializeLoggerService(parent.getLogger());
    }

    private String postProcessingTranslationKey(String key) {
        return this.translationMapping.getOrDefault(key, key);
    }
}
