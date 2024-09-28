package com.ghostchu.quickshop.listener;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.performance.PerfMonitor;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * @author Netherfoam
 */
public class ChatListener extends AbstractQSListener {

  public ChatListener(QuickShop plugin) {

    super(plugin);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onChat(AsyncPlayerChatEvent e) {

    if(e.isCancelled() && plugin.getConfig().getBoolean("shop.ignore-cancel-chat-event")) {
      Log.debug("Ignored a chat event (Cancelled by another plugin, you can force process by turn on ignore-cancel-chat-event)");
      return;
    }

    if(!plugin.getShopManager().getInteractiveManager().containsKey(e.getPlayer().getUniqueId())) {
      return;
    }
    try(PerfMonitor ignored = new PerfMonitor("HandleChat", Duration.of(3, ChronoUnit.SECONDS))) {
      // Fix stupid chat plugin will add a weird space before or after the number we want.
      plugin.getShopManager().handleChat(e.getPlayer(), e.getMessage().trim());
    }
    e.setCancelled(true);
  }

  /**
   * Callback for reloading
   *
   * @return Reloading success
   */
  @Override
  public ReloadResult reloadModule() {

    return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
  }
}
