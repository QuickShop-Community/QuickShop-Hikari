package com.ghostchu.quickshop.util;

import com.ghostchu.quickshop.QuickShop;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.StringJoiner;

public class ServerInternalExporter {
    @SneakyThrows
    public ServerInternalExporter(QuickShop plugin) {
        File exportFolder = new File(plugin.getDataFolder(), "server-internal-export");
        if (!exportFolder.exists()) {
            exportFolder.mkdirs();
        }
        exportMaterial().save(new File(exportFolder, "material.yml"));
        exportEnchantments().save(new File(exportFolder, "enchantment.yml"));
        exportPotionEffectType().save(new File(exportFolder, "potion-effect.yml"));
    }
    public YamlConfiguration exportMaterial() {
        YamlConfiguration config = new YamlConfiguration();
        for (Material value : Material.values()) {
            StringJoiner joiner = new StringJoiner("; ");
            joiner.add("MinecraftKey=" + value.getKey());
            joiner.add("QS-Valid=" + !(value.isAir() || value.isLegacy()));
            joiner.add("IsAir=" + value.isAir());
            joiner.add("IsBlock=" + value.isBlock());
            joiner.add("IsItem=" + value.isItem());
            joiner.add("IsSolid=" + value.isSolid());
            joiner.add("IsOccluding=" + value.isOccluding());
            joiner.add("IsInteractable=" + value.isInteractable());
            joiner.add("IsTransparent=" + value.isTransparent());
            joiner.add("IsBurnable=" + value.isBurnable());
            joiner.add("IsFlammable=" + value.isFlammable());
            joiner.add("IsEdible=" + value.isEdible());
            joiner.add("IsLegacy=" + value.isLegacy());
            joiner.add("BlastResistance=" + value.getBlastResistance());
            joiner.add("Hardness=" + value.getHardness());
            joiner.add("MaxDurability=" + value.getMaxDurability());
            joiner.add("Slipperiness=" + value.getSlipperiness());
            config.set("material." + value.name(), joiner);
        }
        List<String> comments = new ArrayList<>();
        comments.add("This is all material and it's properly on your server.");
        comments.add("Note: Some other plugins (or mods on Hybrid) may add custom materials.");
        comments.add("Note: and it will show raw translatable string on client (because no resource pack to add translations to client).");
        comments.add("Note: you can translate them by yourself in config.yml in custom-translation-key");
        config.setComments("material",comments);
        return config;
    }
    public YamlConfiguration exportEnchantments() {
        YamlConfiguration config = new YamlConfiguration();
        for (Enchantment enchantment : Enchantment.values()) {
            StringJoiner joiner = new StringJoiner("; ");
            joiner.add("MinecraftKey=" + enchantment.getKey());
            joiner.add("Target=" + enchantment.getItemTarget().name());
            joiner.add("StartLevel=" + enchantment.getStartLevel());
            joiner.add("MaxLevel=" + enchantment.getMaxLevel());
            joiner.add("IsTreasure=" + enchantment.isTreasure());
            config.set("enchantment." + enchantment.getKey(), joiner);
        }
        List<String> comments = new ArrayList<>();
        comments.add("This is all enchantments and it's properly on your server.");
        comments.add("Note: EcoEnchants or other plugins (or mods on Hybrid server) may add custom enchantments.");
        comments.add("Note: and it will show raw translatable string on client (because no resource pack to add translations to client).");
        comments.add("Note: you can translate them by yourself in config.yml in custom-translation-key");
        config.setComments("enchantment",comments);
        return config;
    }
    public YamlConfiguration exportPotionEffectType() {
        YamlConfiguration config = new YamlConfiguration();
        for (PotionEffectType type: PotionEffectType.values()) {
            StringJoiner joiner = new StringJoiner("; ");
            joiner.add("MinecraftKey=" + type.getKey());
            joiner.add("Name=" + type.getName());
            joiner.add("Instant=" + type.isInstant());
            joiner.add("Color(0xRRGGBB)=" + "0x" + HexFormat.of().toHexDigits(type.getColor().asRGB()));
            config.set("potioneffecttype." + type.getKey(), joiner);
        }
        List<String> comments = new ArrayList<>();
        comments.add("This is all potion effect types and it's properly on your server.");
        comments.add("Note: Some other plugins (or mods on Hybrid) may add custom potion effect types.");
        comments.add("Note: and it will show raw translatable string on client (because no resource pack to add translations to client).");
        comments.add("Note: you can translate them by yourself in config.yml in custom-translation-key");
        config.setComments("enchantment",comments);
        return config;
    }
}
