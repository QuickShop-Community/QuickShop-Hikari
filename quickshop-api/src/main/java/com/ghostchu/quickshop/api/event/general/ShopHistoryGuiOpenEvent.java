package com.ghostchu.quickshop.api.event.general;

import com.ghostchu.quickshop.api.event.AbstractQSEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

@Getter
public class ShopHistoryGuiOpenEvent extends AbstractQSEvent {

  private final Player player;
  private final List<Shop> shops;
  private final Inventory inventory;

  /**
   * Called when user opened a Shop History GUI
   *
   * @param shops     The shops that related to GUI
   * @param player    The player that open GUI
   * @param inventory The GUI itself
   */
  public ShopHistoryGuiOpenEvent(final List<Shop> shops, final Player player, final Inventory inventory) {

    this.player = player;
    this.shops = shops;
    this.inventory = inventory;
  }
}
