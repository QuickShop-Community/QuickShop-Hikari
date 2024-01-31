package com.ghostchu.quickshop.api.event;

import com.ghostchu.quickshop.api.shop.Shop;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
@Getter
public class ShopHistoryGuiOpenEvent extends AbstractQSEvent{
    private final Player player;
    private final Shop shop;
    private final Inventory inventory;

    /**
     * Called when user opened a Shop History GUI
     * @param shop The shop that related to GUI
     * @param player The player that open GUI
     * @param inventory The GUI itself
     */
    public ShopHistoryGuiOpenEvent(Shop shop, Player player, Inventory inventory){
        this.player = player;
        this.shop = shop;
        this.inventory = inventory;
    }
}
