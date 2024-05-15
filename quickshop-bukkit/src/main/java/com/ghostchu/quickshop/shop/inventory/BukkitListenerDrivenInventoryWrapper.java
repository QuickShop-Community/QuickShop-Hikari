//package com.ghostchu.quickshop.shop.inventory;
//
//import com.ghostchu.quickshop.QuickShop;
//import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
//import com.ghostchu.quickshop.api.inventory.InventoryWrapperIterator;
//import com.ghostchu.quickshop.api.inventory.InventoryWrapperManager;
//import com.ghostchu.quickshop.api.inventory.InventoryWrapperType;
//import com.ghostchu.quickshop.common.util.JsonUtil;
//import org.bukkit.Location;
//import org.bukkit.inventory.Inventory;
//import org.bukkit.inventory.InventoryHolder;
//import org.bukkit.inventory.ItemStack;
//import org.jetbrains.annotations.ApiStatus;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@ApiStatus.Internal
//public class BukkitListenerDrivenInventoryWrapper implements InventoryWrapper, BukkitListenerDrivenInventoryListener {
//    private final Inventory inventory;
//    private final InventoryWrapperManager manager;
//    private final Location location;
//    private boolean needUpdate;
//
//    public BukkitListenerDrivenInventoryWrapper(@NotNull Inventory inventory, Location location) {
//        this.inventory = inventory;
//        this.manager = QuickShop.getInstance().getInventoryWrapperManager();
//        this.location = location.clone();
//        QuickShop.getInstance().getInvWrapperUpdateManager().registerListener(this);
//    }
//
//    @Override
//    public boolean notify(Location updated) {
//        if (location.equals(updated)) {
//            needUpdate = true;
//            return true;
//        }
//        return false;
//    }
//
//
//    @Override
//    public @NotNull InventoryWrapperIterator iterator() {
//        return InventoryWrapperIterator.ofBukkitInventory(inventory);
//    }
//
//    @Override
//    public void clear() {
//        inventory.clear();
//    }
//
//    @Override
//    public @NotNull ItemStack[] createSnapshot() {
//        ItemStack[] content = this.inventory.getContents();
//        ItemStack[] snapshot = new ItemStack[content.length];
//        for (int i = 0; i < content.length; i++) {
//            if (content[i] != null) {
//                snapshot[i] = content[i].clone();
//            } else {
//                snapshot[i] = null;
//            }
//
//        }
//        return snapshot;
//    }
//
//    @Override
//    public @NotNull InventoryWrapperManager getWrapperManager() {
//        return this.manager;
//    }
//
//    @Override
//    public InventoryHolder getHolder() {
//        return inventory.getHolder();
//    }
//
//    @Override
//    public @NotNull InventoryWrapperType getInventoryType() {
//        return InventoryWrapperType.BUKKIT;
//    }
//
//    @Override
//    public @Nullable Location getLocation() {
//        return inventory.getLocation();
//    }
//
//    @Override
//    public boolean isValid() {
//        if (this.inventory.getHolder() != null) {
//            return true;
//        } else {
//            return this.inventory instanceof InventoryHolder;
//        }
//    }
//
//    @Override
//    public boolean isNeedUpdate() {
//        return needUpdate;
//    }
//
//    @Override
//    public boolean restoreSnapshot(@NotNull ItemStack[] snapshot) {
//        this.inventory.setContents(snapshot);
//        return true;
//    }
//
//    @Override
//    public @NotNull Map<Integer, ItemStack> addItem(ItemStack... itemStacks) {
//        return inventory.addItem(itemStacks);
//    }
//
//    @Override
//    public void setContents(ItemStack[] itemStacks) {
//        inventory.setStorageContents(itemStacks);
//    }
//
//    @Override
//    public String toString() {
//        Map<String, Object> map = new HashMap<>();
//        map.put("inventory", inventory.toString());
//        map.put("location", location.toString());
//        map.put("inventoryType", inventory.getClass().getName());
//        return JsonUtil.getGson().toJson(map);
//    }
//}
