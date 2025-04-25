package com.ghostchu.quickshop.compatibility.matcherplus;

import com.ghostchu.quickshop.api.event.general.ShopItemMatchEvent;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public final class Main extends CompatibilityModule implements Listener {


  @Override
  public void init() {
  }

  @EventHandler
  public void onMatch(final ShopItemMatchEvent event) {

    if(event.original() == null) {
      return;
    }

    if(event.comparison() == null) {
      return;
    }

    final int originalFish = fishData(event.original());
    final int testerFish = fishData(event.comparison());
    if(originalFish > -1 || testerFish > -1) {

      event.matches(originalFish == testerFish);
      return;
    }

    final int originalValhalla = valhallaItem(event.original());
    final int compareValhalla = valhallaItem(event.comparison());
    if(originalValhalla > -1 || compareValhalla > -1) {

      event.matches(originalValhalla == compareValhalla);
      return;
    }
  }

  public Integer valhallaItem(final ItemStack stack) {

    if(stack.getItemMeta() != null) {
      return stack.getItemMeta().getPersistentDataContainer().getOrDefault(new NamespacedKey("valhallammo", "id"), PersistentDataType.INTEGER, -1);
    }
    return -1;
  }

  public Integer fishData(final ItemStack stack) {

    if(stack.getItemMeta() != null) {
      return stack.getItemMeta().getPersistentDataContainer().getOrDefault(new NamespacedKey("pyrofishingpro", "fishnumber"), PersistentDataType.INTEGER, -1);
    }
    return -1;
  }
}