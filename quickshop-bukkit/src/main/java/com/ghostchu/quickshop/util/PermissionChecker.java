package com.ghostchu.quickshop.util;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.general.ProtectionCheckStatus;
import com.ghostchu.quickshop.api.event.general.ShopProtectionCheckEvent;
import com.ghostchu.quickshop.api.eventmanager.QuickEventManager;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.eventmanager.BukkitEventManager;
import com.ghostchu.quickshop.eventmanager.QSEventManager;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.holder.Result;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.performance.PerfMonitor;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import com.ghostchu.simplereloadlib.Reloadable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A helper to resolve issue around other plugins with BlockBreakEvent
 *
 * @author Ghost_chu and sandtechnology
 */
public class PermissionChecker implements Reloadable {

  private final QuickShop plugin;

  private boolean usePermissionChecker;

  private QuickEventManager eventManager;


  public PermissionChecker(@NotNull final QuickShop plugin) {

    this.plugin = plugin;
    plugin.getReloadManager().register(this);
    init();
  }

  private void init() {

    usePermissionChecker = this.plugin.getConfig().getBoolean("shop.protection-checking");
    final List<String> listenerBlacklist = plugin.getConfig().getStringList("shop.protection-checking-blacklist");
    listenerBlacklist.removeIf("ignored_listener"::equalsIgnoreCase); // Remove default demo rule
    if(listenerBlacklist.isEmpty()) {
      this.eventManager = new BukkitEventManager();
    } else {
      this.eventManager = new QSEventManager(plugin);
      plugin.logger().info("Loaded {} rules for listener blacklist.", listenerBlacklist.size());
    }
    plugin.logger().info("EventManager selected: {}", this.eventManager.getClass().getSimpleName());
  }

  /**
   * Check player can build in target location
   *
   * @param player   Target player
   * @param location Target location
   *
   * @return Result represent if you can build there
   */
  @NotNull
  public Result canBuild(@NotNull final Player player, @NotNull final Location location) {

    return canBuild(player, location.getBlock());
  }

  /**
   * Check player can build in target block
   *
   * @param player Target player
   * @param block  Target block
   *
   * @return Result represent if you can build there
   */
  @NotNull
  public Result canBuild(@NotNull final Player player, @NotNull final Block block) {

    try(PerfMonitor ignored = new PerfMonitor("Build Permission Check", Duration.of(1, ChronoUnit.SECONDS))) {
      final QUser qUser = QUserImpl.createFullFilled(player);
      if(plugin.getConfig().getStringList("shop.protection-checking-blacklist").contains(block.getWorld().getName())) {
        Log.debug("Skipping protection checking in world " + block.getWorld().getName() + " causing it in blacklist.");
        return Result.SUCCESS;
      }

      if(!usePermissionChecker) {
        return Result.SUCCESS;
      }

      final AtomicBoolean qsCancelling = new AtomicBoolean(false);

      final Result isCanBuild = new Result();

      final BlockBreakEvent beMainHand;

      beMainHand = new BlockBreakEvent(block, player) {

        @Override
        public void setCancelled(final boolean cancel) {
          //tracking cancel plugin
          if(cancel && !isCancelled()) {
            if(qsCancelling.get()) {
              return;
            }
            Log.debug("An plugin blocked the protection checking event! See this stacktrace:");
            for(final StackTraceElement element : Thread.currentThread().getStackTrace()) {
              Log.debug(element.getClassName() + "." + element.getMethodName() + "(" + element.getLineNumber() + ")");
            }
            isCanBuild.setMessage(Thread.currentThread().getStackTrace()[2].getClassName());
            out:
            for(final StackTraceElement element : Thread.currentThread().getStackTrace()) {

              for(final RegisteredListener listener : getHandlerList().getRegisteredListeners()) {
                if(listener.getListener().getClass().getName().equals(element.getClassName())) {
                  isCanBuild.setResult(false);
                  isCanBuild.setMessage(listener.getPlugin().getName());
                  isCanBuild.setListener(listener.getListener().getClass().getName());
                  break out;
                }
              }
            }
          }
          super.setCancelled(cancel);
        }
      };
      // Call for event for protection check start
      this.eventManager.callEvent(new ShopProtectionCheckEvent(block.getLocation(), qUser, ProtectionCheckStatus.BEGIN, beMainHand), null);
      beMainHand.setDropItems(false);
      beMainHand.setExpToDrop(0);

      //register a listener to cancel test event
      Bukkit.getPluginManager().registerEvents(new Listener() {
        @EventHandler(priority = EventPriority.HIGHEST)
        public void onTestEvent(final BlockBreakEvent event) {

          if(event.equals(beMainHand)) {
            // Call for event for protection check end
            eventManager.callEvent(
                    new ShopProtectionCheckEvent(
                            block.getLocation(), qUser, ProtectionCheckStatus.END, beMainHand), null);
            if(!event.isCancelled()) {
              //Ensure this test will no be logged by some plugin
              beMainHand.setCancelled(true);
              isCanBuild.setResult(true);
            }
            HandlerList.unregisterAll(this);
          }
        }
      }, plugin.getJavaPlugin());
      this.eventManager.callEvent(beMainHand, (event)->{
        if(plugin.getConfig().getBoolean("shop.cancel-protection-fake-event-before-reach-monitor-listeners")) {
          if(event instanceof BlockBreakEvent blockBreakEvent) {
            qsCancelling.set(true);
            blockBreakEvent.setCancelled(true);
            blockBreakEvent.setDropItems(false);
            qsCancelling.set(false);
          }
        }
      });
      return isCanBuild;
    }
  }

  /**
   * Callback for reloading
   *
   * @return Reloading success
   */
  @Override
  public ReloadResult reloadModule() {

    init();
    return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
  }
}
