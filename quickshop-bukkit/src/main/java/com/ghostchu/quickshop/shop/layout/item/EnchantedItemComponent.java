package com.ghostchu.quickshop.shop.layout.item;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.layout.ItemComponent;
import com.ghostchu.quickshop.api.shop.layout.RenderComponent;
import com.ghostchu.quickshop.common.util.RomanNumber;
import com.ghostchu.quickshop.shop.layout.inline.LevelRenderComponent;
import com.ghostchu.quickshop.shop.layout.line.LevelLineRenderComponent;
import com.ghostchu.quickshop.util.MsgUtil;
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

    final QuickShop plugin = QuickShop.getInstance();

    if (!(item.getItemMeta() instanceof final EnchantmentStorageMeta enchantmentStorageMeta)) {
      return Component.empty();
    }

    if(!enchantmentStorageMeta.hasStoredEnchants()) {
      return Component.empty();
    }

    final Map.Entry<Enchantment, Integer> entry = enchantmentStorageMeta.getStoredEnchants().entrySet().iterator().next();
    final Enchantment enchantment = entry.getKey();
    final int level = entry.getValue();

    Component name;
    try {
      name = plugin.platform().getTranslation(enchantment);
    } catch(final Throwable throwable) {
      name = MsgUtil.setHandleFailedHover(null, Component.text(enchantment.getKey().getKey()));
      plugin.logger().warn("Failed to handle translation for Enchantment {}", enchantment.getKey(), throwable);
    }

    if(enchantment.getMaxLevel() > 1 || level > 1) {

      final String levelString = (plugin.getConfig().getBoolean("shop.use-roman-numeral-for-enchantments", true))? RomanNumber.toRoman(level) : "" + level;
      name = name.append(Component.text(levelString));
    }

    return name;
  }

  @Override
  public Component renderFor(final @NotNull RenderComponent renderComponent, final @NotNull ItemStack item) {

    if(!(renderComponent instanceof LevelLineRenderComponent)) {
      return Component.empty();
    }

    return null;
  }
}