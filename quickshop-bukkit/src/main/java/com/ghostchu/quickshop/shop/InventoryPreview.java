package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.ShopInventoryPreviewEvent;
import com.ghostchu.quickshop.shop.datatype.PreviewGuiPersistentDataType;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.holder.QuickShopPreviewGUIHolder;
import com.ghostchu.quickshop.util.logger.Log;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

/**
 * A class to create a GUI item preview quickly
 */
@EqualsAndHashCode
@ToString
public class InventoryPreview implements Listener {

    private static final NamespacedKey NAMESPACED_KEY = new NamespacedKey(QuickShop.getInstance(), "preview-item");
    private final ItemStack itemStack;
    private final QuickShop plugin = QuickShop.getInstance();
    private String previewStr;
    @Nullable
    private Inventory inventory;

    /**
     * Create a preview item GUI.
     *
     * @param itemStack The item you want create.
     * @param plugin    The plugin instance.
     */
    public InventoryPreview(@NotNull QuickShop plugin, @NotNull ItemStack itemStack, @NotNull String locale) {
        Util.ensureThread(false);
        this.itemStack = itemStack.clone();
        ItemMeta itemMeta;
        if (itemStack.hasItemMeta()) {
            itemMeta = this.itemStack.getItemMeta();
        } else {
            itemMeta = plugin.getServer().getItemFactory().getItemMeta(itemStack.getType());
        }
        previewStr = LegacyComponentSerializer.legacySection().serialize(plugin.text().of("quickshop-gui-preview").forLocale(locale));
        if (StringUtils.isEmpty(previewStr)) {
            previewStr = ChatColor.RED + "FIXME: Do not set quickshop-gui-preview to null or empty string.";
        }
        if (itemMeta != null) {
            if (itemMeta.hasLore()) {
                itemMeta.getLore().add(previewStr);
            } else {
                itemMeta.setLore(Collections.singletonList(previewStr));
            }

            itemMeta.getPersistentDataContainer().set(NAMESPACED_KEY, PreviewGuiPersistentDataType.INSTANCE, UUID.randomUUID());
            this.itemStack.setItemMeta(itemMeta);
        }
    }

    public void close() {
        Util.ensureThread(false);
        if (inventory == null) {
            return;
        }

        for (HumanEntity player : new ArrayList<>(inventory.getViewers())) {
            player.closeInventory();
        }
        inventory = null; // Destroy
    }

    /**
     * Open the preview GUI for player.
     */
    public void show(Player player) {
        Util.ensureThread(false);
        if (itemStack == null || player == null || player.isSleeping()) // Null pointer exception
        {
            return;
        }
        ShopInventoryPreviewEvent shopInventoryPreview = new ShopInventoryPreviewEvent(player, itemStack);
        if (Util.fireCancellableEvent(shopInventoryPreview)) {
            Log.debug("Inventory preview was canceled by a plugin.");
            return;
        }
        if (inventory == null) {
            final int size = 9;
            inventory = plugin.getServer().createInventory(new QuickShopPreviewGUIHolder(), size, LegacyComponentSerializer.legacySection().serialize(plugin.text().of(player, "menu.preview").forLocale()));
            for (int i = 0; i < size; i++) {
                inventory.setItem(i, itemStack);
            }
        }
        player.openInventory(inventory);
    }

}
