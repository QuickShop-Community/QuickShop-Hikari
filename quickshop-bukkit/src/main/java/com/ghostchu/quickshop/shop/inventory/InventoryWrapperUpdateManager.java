//package com.ghostchu.quickshop.shop.inventory;
//
//import com.ghostchu.quickshop.QuickShop;
//import com.ghostchu.quickshop.listener.AbstractQSListener;
//import com.ghostchu.quickshop.util.Util;
//import com.ghostchu.quickshop.util.logger.Log;
//import com.google.common.collect.Lists;
//import org.bukkit.Bukkit;
//import org.bukkit.Location;
//import org.bukkit.Material;
//import org.bukkit.block.Block;
//import org.bukkit.block.BlockState;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.EventPriority;
//import org.bukkit.event.block.BlockPlaceEvent;
//import org.bukkit.inventory.DoubleChestInventory;
//import org.bukkit.inventory.InventoryHolder;
//
//import java.lang.ref.WeakReference;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
//public class InventoryWrapperUpdateManager extends AbstractQSListener {
//    private final List<Material> checkList = Lists.newArrayList(Material.CHEST, Material.TRAPPED_CHEST);
//    private final List<WeakReference<BukkitListenerDrivenInventoryListener>> registeredListeners = new ArrayList<>();
//
//    public InventoryWrapperUpdateManager(QuickShop plugin) {
//        super(plugin);
//    }
//
//    private synchronized void notifyAllListeners(Location... locs) {
//        synchronized (registeredListeners) {
//            Iterator<WeakReference<BukkitListenerDrivenInventoryListener>> it = registeredListeners.iterator();
//            while (it.hasNext()) {
//                WeakReference<BukkitListenerDrivenInventoryListener> ref = it.next();
//                BukkitListenerDrivenInventoryListener listener = ref.get();
//                if (listener == null) {
//                    it.remove();
//                    continue;
//                }
//                for (Location loc : locs) {
//                    try {
//                        if (listener.notify(loc)) {
//                            Log.debug("Successfully notified listener " + listener + " which need to be update");
//                        }
//                    } catch (Throwable e) {
//                        plugin.logger().warn("Failed to notify inventory possible update to listener: {}", listener, e);
//                    }
//                }
//            }
//        }
//    }
//
//    public void registerListener(BukkitListenerDrivenInventoryListener listener) {
//        this.registeredListeners.add(new WeakReference<>(listener));
//    }
//
//    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
//    public void onInventoryRelatedBlockPlaced(BlockPlaceEvent event) {
//        Block placed = event.getBlockPlaced();
//        if (!checkList.contains(placed.getType())) {
//            return;
//        }
//        // Delay 1 tick to allow chest merge
//        Bukkit.getScheduler().runTaskLater(plugin.getJavaPlugin(), () -> {
//            BlockState state = placed.getState();
//            if (state instanceof InventoryHolder holder) {
//                if (holder.getInventory() instanceof DoubleChestInventory doubleChestInventory) {
//                    Util.asyncThreadRun(() -> notifyAllListeners(doubleChestInventory.getLeftSide().getLocation(), doubleChestInventory.getRightSide().getLocation()));
//                }
//            }
//        }, 1L);
//    }
//}
