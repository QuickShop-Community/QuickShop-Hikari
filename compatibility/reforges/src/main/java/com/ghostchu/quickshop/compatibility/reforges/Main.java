package com.ghostchu.quickshop.compatibility.reforges;

import com.ghostchu.quickshop.api.event.display.ItemPreviewComponentPrePopulateEvent;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.willfp.eco.core.display.DisplayProperties;
import com.willfp.reforges.display.ReforgesDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public final class Main extends CompatibilityModule implements Listener {

  @Override
  public void init() {
    // There no init stuffs need to do
  }

  @EventHandler(ignoreCancelled = true)
  public void onItemPreviewPreparing(final ItemPreviewComponentPrePopulateEvent event) {

    if(event.getPlayer() == null) {
      return;
    }
    final ItemStack stack = event.getItemStack().clone();
    ReforgesDisplay.INSTANCE.display(stack, event.getPlayer(), ReforgesDisplay.INSTANCE.generateVarArgs(stack));
    ReforgesDisplay.INSTANCE.display(stack, event.getPlayer(), new DisplayProperties(false, false, stack), ReforgesDisplay.INSTANCE.generateVarArgs(stack));
    event.setItemStack(stack);
  }
}