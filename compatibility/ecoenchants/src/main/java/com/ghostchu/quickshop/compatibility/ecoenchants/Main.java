package com.ghostchu.quickshop.compatibility.ecoenchants;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.QSConfigurationReloadEvent;
import com.ghostchu.quickshop.api.event.display.ItemPreviewComponentPrePopulateEvent;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.util.logger.Log;
import com.willfp.eco.core.display.DisplayProperties;
import com.willfp.ecoenchants.EcoEnchantsPlugin;
import com.willfp.ecoenchants.display.EnchantDisplay;
import com.willfp.ecoenchants.enchant.EcoEnchant;
import com.willfp.ecoenchants.enchant.EcoEnchants;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public final class Main extends CompatibilityModule implements Listener {

  private EnchantDisplay display;

  @Override
  public void init() {
    // There no init stuffs need to do
    this.display = new EnchantDisplay(EcoEnchantsPlugin.getPlugin(EcoEnchantsPlugin.class));
    initEcoEnchantEnchantmentTranslationKeys();
  }

  @EventHandler(ignoreCancelled = true)
  public void onPluginLoad(final PluginEnableEvent event) {

    if("EcoEnchants".equalsIgnoreCase(event.getPlugin().getName())
       || "libreforge".equalsIgnoreCase(event.getPlugin().getName())
       || "eco".equalsIgnoreCase(event.getPlugin().getName())) {
      QuickShop.folia().getImpl().runLater(this::initEcoEnchantEnchantmentTranslationKeys, 1);
    }
  }

  @Override
  public void onQuickShopReload(final QSConfigurationReloadEvent event) {

    initEcoEnchantEnchantmentTranslationKeys();
  }

  private void initEcoEnchantEnchantmentTranslationKeys() {

    final Set<EcoEnchant> enchantSet = EcoEnchants.INSTANCE.values();
    getLogger().info("Found " + enchantSet.size() + " enchantments from EcoEnchants");
    for(final EcoEnchant value : enchantSet) {
      final String key = "ecoenchants:enchantment." + value.getId();
      getApi().registerLocalizedTranslationKeyMapping(key, value.getRawDisplayName());
      Log.debug("Registered EcoEnchant " + value.getId() + " with translation key override mapping: " + key + " -> " + value.getRawDisplayName());
    }
    getLogger().info("Initialized " + enchantSet.size() + " EcoEnchants translation keys");
  }

  @EventHandler(ignoreCancelled = true)
  public void onItemPreviewPreparing(final ItemPreviewComponentPrePopulateEvent event) {

    if(event.getPlayer() == null) {
      return;
    }
    final ItemStack stack = event.getItemStack().clone();
    display.display(stack, event.getPlayer(), display.generateVarArgs(stack));
    display.display(stack, event.getPlayer(), new DisplayProperties(false, false, stack), display.generateVarArgs(stack));
    event.setItemStack(stack);
  }
}
