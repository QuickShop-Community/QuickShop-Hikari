package com.ghostchu.quickshop.hook.fworldedit;

import com.ghostchu.quickshop.QuickShop;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.eventbus.EventHandler;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.World;
import com.vdurmont.semver4j.Semver;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class FWorldEditAdapter implements Listener {

  private final WorldEditPlugin worldEditPlugin;

  public FWorldEditAdapter() {

    this.worldEditPlugin = (WorldEditPlugin)Bukkit.getPluginManager().getPlugin("WorldEdit");
    Bukkit.getPluginManager().registerEvents(this, QuickShop.getInstance().getJavaPlugin());
  }

  @Subscribe(priority = EventHandler.Priority.NORMAL)
  public void proxyEditSession(final EditSessionEvent event) {

    final Actor actor = event.getActor();
    final World world = event.getWorld();

    if(actor == null) {
      return;
    }

    final String version = worldEditPlugin.getPluginMeta().getVersion();
    //check if version is greater than 2.11.0
    final boolean isLegacy = new Semver(version).isLowerThanOrEqualTo("2.11.0");


    if(event.getStage() == EditSession.Stage.BEFORE_HISTORY) {

      if(isLegacy) {
        event.getExtent().addProcessor(new ShopProcessorLegacy(world));
      } else {
        event.getExtent().addProcessor(new ShopProcessor(world));
      }
    }
  }

  public void register() {

    worldEditPlugin.getWorldEdit().getEventBus().register(this);
  }

  public void unregister() {

    worldEditPlugin.getWorldEdit().getEventBus().unregister(this);
  }

}
