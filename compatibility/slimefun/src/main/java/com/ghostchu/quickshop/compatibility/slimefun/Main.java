package com.ghostchu.quickshop.compatibility.slimefun;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.registry.BuiltInRegistry;
import com.ghostchu.quickshop.api.registry.Registry;
import com.ghostchu.quickshop.api.registry.builtin.itemexpression.ItemExpressionHandler;
import com.ghostchu.quickshop.api.registry.builtin.itemexpression.ItemExpressionRegistry;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.events.ExplosiveToolBreakBlocksEvent;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class Main extends CompatibilityModule implements SlimefunAddon, ItemExpressionHandler {

  @Override
  public void init() {

    final Registry registry = QuickShop.getInstance().getRegistry().getRegistry(BuiltInRegistry.ITEM_EXPRESSION);
    if(registry instanceof ItemExpressionRegistry itemExpressionRegistry) {
      if(itemExpressionRegistry.registerHandlerSafely(this)) {
        getLogger().info("Register Slimefun ItemExpressionHandler successfully!");
      }
    }
  }

  @NotNull
  @Override
  public JavaPlugin getJavaPlugin() {

    return this;
  }

  @Override
  public String getBugTrackerURL() {

    return "https://github.com/Quickshop-Community/QuickShop-Hikari/issues";
  }

  @Override
  public @NotNull Plugin getPlugin() {

    return this;
  }

  @Override
  public String getPrefix() {

    return "slimefun";
  }

  @EventHandler
  public void onExplosionPickaxeEvent(final ExplosiveToolBreakBlocksEvent event) {
    // get the primary block that player tries to break
    Block primaryBlock = event.getPrimaryBlock();
    Shop primaryShop = QuickShop.getInstance().getShopManager().getShop(primaryBlock.getLocation());

    // check if it's not null
    if (primaryShop != null) {
      // cancel the explosion
      event.setCancelled(true);
      return;
    }

    // get all additional block such as it's radius.
    for (Block block : event.getAdditionalBlocks()) {
      // get variable for shop
      Shop shop = QuickShop.getInstance().getShopManager().getShop(block.getLocation());

      // check if it's not null
      if (shop != null) {
        // cancel the explosion on that block
        event.setCancelled(true);
        return;
      }
    }
  }

  @Override
  public boolean match(final ItemStack stack, final String expression) {

    final Map<String, SlimefunItem> slimefunItemMap = Slimefun.getRegistry().getSlimefunItemIds();
    final SlimefunItem slimefunItem = slimefunItemMap.get(expression);
    if(slimefunItem == null) {
      return false;
    }
    return slimefunItem.isItem(stack);
  }
}
