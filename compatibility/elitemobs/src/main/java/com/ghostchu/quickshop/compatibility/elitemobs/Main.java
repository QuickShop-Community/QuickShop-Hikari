package com.ghostchu.quickshop.compatibility.elitemobs;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.details.ShopItemChangeEvent;
import com.ghostchu.quickshop.api.event.economy.ShopPurchaseEvent;
import com.ghostchu.quickshop.api.event.modification.ShopCreateEvent;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public final class Main extends CompatibilityModule implements Listener {

  @Override
  public void init() {
    // There no init stuffs need to do
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onShopCreation(final ShopCreateEvent event) {

    if(isSoulBoundItem(event.getShop().getItem())) {
      event.setCancelled(true, getDisallowedMessage(event.getCreator()));
    }
  }

  private boolean isSoulBoundItem(final ItemStack stack) {

    if(stack == null) {
      return false;
    }
    if(stack.getItemMeta() == null) {
      return false;
    }
    return EliteItemManager.getSoulboundPlayer(stack.getItemMeta()) != null;
  }

  private Component getDisallowedMessage(final QUser sender) {

    return QuickShop.getInstance().text().of(sender, "compatibility.elitemobs.soulbound-disallowed").forLocale();
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onShopItemChanging(final ShopItemChangeEvent event) {

    if(isSoulBoundItem(event.getShop().getItem())) {
      event.setCancelled(true, getDisallowedMessage(event.getShop().getOwner()));
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onShopPurchase(final ShopPurchaseEvent event) {

    if(isSoulBoundItem(event.getShop().getItem())) {
      event.setCancelled(true, getDisallowedMessage(event.getShop().getOwner()));
    }
  }
}
