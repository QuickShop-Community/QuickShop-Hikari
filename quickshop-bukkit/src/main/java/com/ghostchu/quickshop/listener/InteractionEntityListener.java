package com.ghostchu.quickshop.listener;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.interaction.InteractionBehavior;
import com.ghostchu.quickshop.api.shop.interaction.InteractionClick;
import com.ghostchu.quickshop.api.shop.interaction.InteractionType;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.Location;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;

import static com.ghostchu.quickshop.shop.display.display.DisplayEntityItemManager.DISPLAY_ITEM_KEY_INSTANCE;

public class InteractionEntityListener implements Listener {

  private final QuickShop plugin;

  public InteractionEntityListener(final QuickShop plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onInteract(final PlayerInteractAtEntityEvent event) {

    final Optional<InteractionType> interactionType = plugin.getInteractionManager().interaction(event, InteractionClick.DISPLAY);
    if(interactionType.isEmpty()) {
      Log.debug("Interaction: InteractionType is empty");
      return;
    }

    final Optional<InteractionBehavior> behavior = plugin.getInteractionManager().behavior(interactionType.get());
    if(behavior.isEmpty()) {
      Log.debug("Interaction: InteractionBehavior is empty");
      return;
    }

    final Location location = Util.locationFromPDCString(event.getRightClicked().getWorld(), event.getRightClicked().getPersistentDataContainer().get(DISPLAY_ITEM_KEY_INSTANCE, PersistentDataType.STRING));
    if(location == null) {
      Log.debug("Interaction: Location is empty");
      return;
    }

    final Shop shop = plugin.getShopManager().getShopIncludeAttached(location);
    if(shop == null) {
      Log.debug("Interaction: Shop is empty");
      return;
    }

    behavior.get().handle(plugin, shop, event.getPlayer(), event, InteractionClick.DISPLAY, interactionType.get());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onDamage(final EntityDamageByEntityEvent event) {

    final Optional<InteractionType> interactionType = plugin.getInteractionManager().interaction(event, InteractionClick.DISPLAY);
    if(interactionType.isEmpty()) {
      Log.debug("Interaction: InteractionType is empty");
      return;
    }

    final Optional<InteractionBehavior> behavior = plugin.getInteractionManager().behavior(interactionType.get());
    if(behavior.isEmpty()) {
      Log.debug("Interaction: InteractionBehavior is empty");
      return;
    }

    final Location location = Util.locationFromPDCString(event.getEntity().getWorld(), event.getEntity().getPersistentDataContainer().get(DISPLAY_ITEM_KEY_INSTANCE, PersistentDataType.STRING));
    if(location == null) {
      Log.debug("Interaction: Location is empty");
      return;
    }

    final Shop shop = plugin.getShopManager().getShopIncludeAttached(location);
    if(shop == null) {
      Log.debug("Interaction: Shop is empty");
      return;
    }

    behavior.get().handle(plugin, shop, (Player)event.getDamager(), event, InteractionClick.DISPLAY, interactionType.get());
  }
}