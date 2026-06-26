package com.ghostchu.quickshop.shop.layout.item;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.layout.ItemComponent;
import com.ghostchu.quickshop.api.shop.layout.RenderComponent;
import com.ghostchu.quickshop.shop.layout.inline.LevelRenderComponent;
import com.ghostchu.quickshop.shop.layout.line.LevelLineRenderComponent;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class EnchantedItemComponent implements ItemComponent {

  @Override
  public boolean enabled() {

    return QuickShop.getInstance().getConfig().getBoolean("shop.use-enchantment-for-enchanted-book");
  }

  @Override
  public boolean appliesTo(final @NotNull ItemStack item) {

    return (item.getItemMeta() instanceof final EnchantmentStorageMeta enchantmentStorageMeta && enchantmentStorageMeta.hasStoredEnchants());
  }

  @Override
  public boolean appliesTo(final @NotNull RenderComponent renderComponent, final @NotNull ItemStack item) {

    return (renderComponent instanceof LevelRenderComponent);
  }

  @Override
  public Component renderName(final @NotNull ItemStack item) {

    if (!(item.getItemMeta() instanceof final EnchantmentStorageMeta enchantmentStorageMeta)) {
      return Component.empty();
    }

    if(!enchantmentStorageMeta.hasStoredEnchants()) {
      return Component.empty();
    }

    final Map.Entry<Enchantment, Integer> entry = enchantmentStorageMeta.getStoredEnchants().entrySet().iterator().next();
    return enchantmentDataToComponent(entry.getKey(), entry.getValue());

    return null;
  }

  @Override
  public Component renderFor(final @NotNull RenderComponent renderComponent, final @NotNull ItemStack item) {

    return null;
  }
}