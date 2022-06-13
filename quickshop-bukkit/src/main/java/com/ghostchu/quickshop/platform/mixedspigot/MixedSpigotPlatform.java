package com.ghostchu.quickshop.platform.mixedspigot;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.localization.game.game.GameLanguage;
import com.ghostchu.quickshop.localization.game.game.MojangGameLanguageImpl;
import com.ghostchu.quickshop.platform.spigot.SpigotPlatform;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import com.ghostchu.simplereloadlib.Reloadable;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class MixedSpigotPlatform extends SpigotPlatform implements Reloadable {
    private final GameLanguage gameLanguage = new MojangGameLanguageImpl(QuickShop.getInstance(), MsgUtil.getDefaultGameLanguageCode());

    public MixedSpigotPlatform(@NotNull Map<String, String> mapping) {
        super(mapping);
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull EntityType type) {
        return gameLanguage.getEntity(type);
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull Material material) {
        return gameLanguage.getItem(material);
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull Enchantment enchantment) {
        return gameLanguage.getEnchantment(enchantment);
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull PotionEffectType potionEffectType) {
        return gameLanguage.getPotion(potionEffectType);
    }

    @Override
    public ReloadResult reloadModule() throws Exception {
        return new ReloadResult(ReloadStatus.REQUIRE_RESTART, "The gamelanguage has been changed, please restart the server.", null);
    }
}
