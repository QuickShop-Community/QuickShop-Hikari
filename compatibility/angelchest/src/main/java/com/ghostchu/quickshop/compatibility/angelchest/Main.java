package com.ghostchu.quickshop.compatibility.angelchest;

import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.management.ShopCreateEvent;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import de.jeff_media.angelchest.AngelChest;
import de.jeff_media.angelchest.AngelChestPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class Main extends CompatibilityModule implements Listener {

  private AngelChestPlugin angelChestPlugin;

  @Override
  public void init() {

    angelChestPlugin = (AngelChestPlugin)getServer().getPluginManager().getPlugin("AngelChest");
  }

  @EventHandler(ignoreCancelled = true)
  public void onPreCreation(final ShopCreateEvent event) {

    if(!event.isPhase(Phase.PRE_CANCELLABLE)) {

      return;
    }

    final AngelChest ac = angelChestPlugin.getAngelChestAtBlock(event.location().getBlock());
    if(ac != null) {

      event.setCancelled(true, "You can't create a shop on a AngelChest block!");
    }
  }

}
