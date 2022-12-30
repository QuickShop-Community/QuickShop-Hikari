package com.ghostchu.quickshop.compatibility.elitemobs;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.ShopItemChangeEvent;
import com.ghostchu.quickshop.api.event.ShopPurchaseEvent;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public final class Main extends CompatibilityModule implements Listener {
    @Override
    public void init() {
        // There no init stuffs need to do
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShopCreation(ShopCreateEvent event) {
        if (isSoulBoundItem(event.getShop().getItem())) {
            event.setCancelled(true, getDisallowedMessage(event.getCreator()));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShopItemChanging(ShopItemChangeEvent event) {
        if (isSoulBoundItem(event.getShop().getItem())) {
            event.setCancelled(true, getDisallowedMessage(event.getShop().getOwner()));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShopPurchase(ShopPurchaseEvent event) {
        if (isSoulBoundItem(event.getShop().getItem())) {
            event.setCancelled(true, getDisallowedMessage(event.getShop().getOwner()));
        }
    }

    private boolean isSoulBoundItem(ItemStack stack) {
        if (stack == null) return false;
        if (stack.getItemMeta() == null) return false;
        return EliteItemManager.getSoulboundPlayer(stack.getItemMeta()) != null;
    }

    private Component getDisallowedMessage(UUID sender) {
        return QuickShop.getInstance().text().of(sender, "compatibility.elitemobs.soulbound-disallowed").forLocale();
    }
}
