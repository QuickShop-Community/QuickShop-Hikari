package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.inventory.ShopInventoryPreviewEvent;
import com.ghostchu.quickshop.shop.datatype.PreviewGuiPersistentDataType;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.holder.QuickShopPreviewGUIHolder;
import com.ghostchu.quickshop.util.logger.Log;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A class to create a GUI item preview quickly
 */
public class InventoryPreview {

  private static final NamespacedKey NAMESPACED_KEY = new NamespacedKey(QuickShop.getInstance().getJavaPlugin(), "preview-item");

  private InventoryPreview() {}

  /**
   * Open the preview GUI for player.
   */
  public static void show(final Player player, ItemStack itemStack) {

    Util.ensureThread(false);
    if(itemStack == null || player == null || player.isSleeping()) // Null pointer exception
    {
      return;
    }

    itemStack = itemStack.clone();
    final ShopInventoryPreviewEvent shopInventoryPreview = new ShopInventoryPreviewEvent(player, itemStack);
    if(Util.fireCancellableEvent(shopInventoryPreview)) {
      Log.debug("Inventory preview was canceled by a plugin.");
      return;
    }

    final QuickShop plugin = QuickShop.getInstance();

    Component previewStr = plugin.text().of(player, "quickshop-gui-preview").forLocale();
    if(PlainTextComponentSerializer.plainText().serialize(previewStr).isEmpty()) {
      previewStr = Component.text("FIXME: Do not set quickshop-gui-preview to an empty string.", NamedTextColor.RED);
    }

    final Component finalPreviewStr = previewStr;
    itemStack.editMeta(meta -> {
      List<Component> lore = meta.lore();
      if(lore == null) {
        lore = new ArrayList<>();
      }

      lore.add(finalPreviewStr);
      meta.lore(lore);

      meta.getPersistentDataContainer().set(NAMESPACED_KEY, PreviewGuiPersistentDataType.INSTANCE, UUID.randomUUID());
    });

    final int size = 9;
    final QuickShopPreviewGUIHolder holder = new QuickShopPreviewGUIHolder();
    final Inventory inventory = Bukkit.createInventory(holder, size, plugin.text().of(player, "menu.preview").forLocale());

    holder.setInventory(inventory);

    for(int i = 0; i < size; i++) {
      inventory.setItem(i, itemStack);
    }
    player.openInventory(inventory);
  }

}
