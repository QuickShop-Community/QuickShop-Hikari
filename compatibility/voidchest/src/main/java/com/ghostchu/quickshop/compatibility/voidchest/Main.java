package com.ghostchu.quickshop.compatibility.voidchest;

import com.georgev22.voidchest.api.VoidChestAPI;
import com.georgev22.voidchest.api.event.annotations.EventHandler;
import com.georgev22.voidchest.api.event.events.item.ItemSpawnEvent;
import com.georgev22.voidchest.api.event.events.sell.VoidSellChunkItemEvent;
import com.georgev22.voidchest.api.event.interfaces.EventListener;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class Main extends CompatibilityModule implements EventListener {

  @Override
  public void init() {

    VoidChestAPI.getInstance().eventManager().register(VoidSellChunkItemEvent.class, this);
    VoidChestAPI.getInstance().eventManager().register(ItemSpawnEvent.class, this);
  }

  @EventHandler(ignoreCancelled = true)
  public void onVoidSellChunkItem(final VoidSellChunkItemEvent event) {

    event.setCancelled(cancelEvent(event.getDroppedItem(), null));
  }

  @EventHandler(ignoreCancelled = true)
  public void onItemSpawnEvent(final ItemSpawnEvent event) {

    event.setCancelled(cancelEvent(event.getItem(), event.getItemStack().getItemStack()));
  }

  private boolean cancelEvent(@Nullable final Item item, @Nullable final ItemStack itemStack) {

    if(item != null) {
      return AbstractDisplayItem.checkIsGuardItemStack(item.getItemStack());
    }

    if(itemStack != null) {
      return AbstractDisplayItem.checkIsGuardItemStack(itemStack);
    }
    return false;
  }

}
