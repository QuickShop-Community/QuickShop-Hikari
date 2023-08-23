package com.ghostchu.quickshop.compatibility.ecoenchants;

import com.ghostchu.quickshop.api.event.ItemPreviewComponentPrePopulateEvent;
import com.ghostchu.quickshop.api.event.QSConfigurationReloadEvent;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.util.logger.Log;
import com.willfp.eco.core.display.DisplayProperties;
import com.willfp.ecoenchants.EcoEnchantsPlugin;
import com.willfp.ecoenchants.display.EnchantDisplay;
import com.willfp.ecoenchants.enchants.EcoEnchant;
import com.willfp.ecoenchants.enchants.EcoEnchants;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public final class Main extends CompatibilityModule implements Listener {
    private EnchantDisplay display;

    @Override
    public void init() {
        // There no init stuffs need to do
        this.display = new EnchantDisplay(EcoEnchantsPlugin.getInstance());
        initEcoEnchantEnchantmentTranslationKeys();
    }

    @Override
    public void onQuickShopReload(QSConfigurationReloadEvent event) {
        initEcoEnchantEnchantmentTranslationKeys();
    }

    private void initEcoEnchantEnchantmentTranslationKeys() {
        Set<EcoEnchant> enchantSet = EcoEnchants.values();
        for (EcoEnchant value : enchantSet) {
            getApi().registerLocalizedTranslationKeyMapping(value.translationKey(), value.getDisplayName());
            Log.debug("Registered EcoEnchant " + value.getId() + " with translation key override mapping: " + value.translationKey() + " -> " + value.getDisplayName());
        }
        getLogger().info("Initialized " + enchantSet.size() + " EcoEnchants translation keys");
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemPreviewPreparing(ItemPreviewComponentPrePopulateEvent event) {
        if (event.getPlayer() == null) return;
        ItemStack stack = event.getItemStack().clone();
        display.display(stack, event.getPlayer(), display.generateVarArgs(stack));
        display.display(stack, event.getPlayer(), new DisplayProperties(false, false, stack), display.generateVarArgs(stack));
        event.setItemStack(stack);
    }
}
