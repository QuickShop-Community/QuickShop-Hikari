package com.ghostchu.quickshop.shop.interaction.interactions;

import com.ghostchu.quickshop.api.shop.interaction.InteractionClick;
import com.ghostchu.quickshop.api.shop.interaction.InteractionType;
import org.bukkit.entity.Interaction;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import static com.ghostchu.quickshop.shop.display.display.DisplayEntityItemManager.DISPLAY_ITEM_KEY_INSTANCE;

public class SneakingRightClickDisplay implements InteractionType {

  /**
   * Retrieves the identifier associated with this type of interaction.
   *
   * @return The identifier for this type of interaction.
   */
  @Override
  public String identifier() {

    return "SNEAKING_RIGHT_CLICK_DISPLAY";
  }

  /**
   * Checks if this type of interaction applies to the given PlayerInteractEvent.
   *
   * @param event The PlayerInteractEvent to check against.
   *
   * @return True if this interaction type applies to the event, false otherwise.
   */
  @Override
  public boolean applies(final @NotNull PlayerInteractEvent event, final @NotNull InteractionClick click) {

    return false;
  }

  @Override
  public boolean applies(final @NotNull PlayerInteractEntityEvent event, final @NotNull InteractionClick click) {

    if(!(event instanceof final PlayerInteractAtEntityEvent interactAt) || !(interactAt.getRightClicked() instanceof final Interaction interaction)) {
      return false;
    }

    if(!interaction.getPersistentDataContainer().has(DISPLAY_ITEM_KEY_INSTANCE, PersistentDataType.STRING)) {
      return false;
    }

    return click == InteractionClick.DISPLAY && event.getPlayer().isSneaking();
  }
}