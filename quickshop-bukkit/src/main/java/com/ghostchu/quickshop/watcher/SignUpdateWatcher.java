package com.ghostchu.quickshop.watcher;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.logger.Log;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;

public class SignUpdateWatcher implements Runnable {

  private final Queue<Shop> signUpdateQueue = new LinkedList<>();

  private WrappedTask task = null;

  @Override
  public void run() {

    Instant startTime = Instant.now();
    Instant endTime = startTime.plusMillis(50);
    Shop shop = signUpdateQueue.poll();
    while(shop != null && !Instant.now().isAfter(endTime)) {
      shop.setSignText(QuickShop.getInstance().text().findRelativeLanguages(shop.getOwner(), false));
      shop = signUpdateQueue.poll();
    }
  }

  public void scheduleSignUpdate(@NotNull Shop shop) {

    if(signUpdateQueue.contains(shop)) {
      return; // Ignore if schedule too frequently
    }
    signUpdateQueue.add(shop);
  }


  public void start(int i, int i2) {

    task = QuickShop.folia().getImpl().runTimerAsync(this, i, i2);
  }

  public void stop() {

    try {
      if(task != null && !task.isCancelled()) {
        task.cancel();
      }
    } catch(IllegalStateException ex) {
      Log.debug("Task already cancelled " + ex.getMessage());
    }
  }
}
