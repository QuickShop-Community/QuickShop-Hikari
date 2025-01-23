package com.ghostchu.quickshop.compatibility.angelchest;

import com.ghostchu.quickshop.api.event.modification.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.modification.ShopPreCreateEvent;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import de.jeff_media.angelchest.AngelChest;
import de.jeff_media.angelchest.AngelChestPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class Main extends CompatibilityModule implements Listener {

  private AngelChestPlugin angelChestPlugin;

  @Override
  public void init() {

    angelChestPlugin = (AngelChestPlugin)getServer().getPluginManager().getPlugin("AngelChest");
  }

  @EventHandler(ignoreCancelled = true)
  public void onPreCreation(final ShopPreCreateEvent event) {

    final AngelChest ac = angelChestPlugin.getAngelChestAtBlock(event.getLocation().getBlock());
    if(ac != null) {
      event.setCancelled(true, (Component)null);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onPreCreation(final ShopCreateEvent event) {

    final AngelChest ac = angelChestPlugin.getAngelChestAtBlock(event.getShop().getLocation().getBlock());
    if(ac != null) {
      event.setCancelled(true, "You can't create a shop on a AngelChest block!");
    }
  }

}
