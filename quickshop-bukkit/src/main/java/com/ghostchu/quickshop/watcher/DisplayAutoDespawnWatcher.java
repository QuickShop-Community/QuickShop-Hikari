package com.ghostchu.quickshop.watcher;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.shop.ContainerShop;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import com.ghostchu.quickshop.util.paste.item.SubPasteItem;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import com.ghostchu.simplereloadlib.Reloadable;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.StringJoiner;

public class DisplayAutoDespawnWatcher implements Runnable, Reloadable, SubPasteItem {

  private final QuickShop plugin;
  private int range;
  private WrappedTask task;
  private int taskPeriod;

  public DisplayAutoDespawnWatcher(@NotNull final QuickShop plugin) {

    this.plugin = plugin;
    plugin.getReloadManager().register(this);
    plugin.getPasteManager().register(plugin.getJavaPlugin(), this);
    init();
  }

  public DisplayAutoDespawnWatcher(final QuickShop plugin, final int range) {

    this.plugin = plugin;
    this.range = range;
  }

  private void init() {

    this.range = plugin.getConfig().getInt("shop.display-despawn-range");
  }

  @Override
  public ReloadResult reloadModule() {

    init();
    return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
  }

  public void start(final int delay, final int period) {

    taskPeriod = period;
    stop();

    task = QuickShop.folia().getScheduler().runTimer(this, delay, period);
  }

  @Override
  public void run() {

    for(final Shop shop : plugin.getShopManager().getLoadedShops()) {
      //Shop may be deleted or unloaded when iterating
      if(!shop.isLoaded()) {
        continue;
      }
      if(shop.isDisableDisplay()) {
        continue;
      }
      final Location location = shop.bukkitLocation();
      final World world = shop.bukkitLocation().getWorld(); //Cache this, because it will took some time.
      final AbstractDisplayItem displayItem = ((ContainerShop)shop).getDisplayItem();
      if(displayItem != null) {
        // Check the range has player?
        boolean anyPlayerInRegion = false;
        for(final Player player : Bukkit.getOnlinePlayers()) {
          if((player.getWorld() == world) && (player.getLocation().distance(location) <= range)) {
            anyPlayerInRegion = true;
            break;
          }
        }
        if(anyPlayerInRegion) {
          if(!displayItem.isSpawned()) {
            displayItem.spawn();
          }
        } else if(displayItem.isSpawned()) {
          displayItem.remove(false);
        }
      }
    }
  }

  public void stop() {

    try {
      if(task != null && !task.isCancelled()) {

        task.cancel();
      }
    } catch(final IllegalStateException ignore) {
    }
  }

  public void unregister() {

    stop();
    plugin.getReloadManager().unregister(this);
    plugin.getPasteManager().unregister(plugin.getJavaPlugin(), this);
  }

  @Override
  @NotNull
  public String genBody() {

    final StringJoiner joiner = new StringJoiner("<br/>");
    joiner.add("<b>Warning: DisplayAutoDespawnWatcher has been enabled, this may cause lag. This feature is not recommended</b>");
    joiner.add("<p>Range: " + range + "</p>");
    return joiner.toString();
  }

  @Override
  @NotNull
  public String getTitle() {

    return "Display Auto Despawn Watcher";
  }

  public int getTaskPeriod() {

    return this.taskPeriod;
  }
}
