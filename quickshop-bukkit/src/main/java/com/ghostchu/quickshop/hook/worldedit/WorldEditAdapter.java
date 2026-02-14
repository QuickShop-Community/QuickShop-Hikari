package com.ghostchu.quickshop.hook.worldedit;

import com.ghostchu.quickshop.QuickShop;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.eventbus.EventHandler;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class WorldEditAdapter implements Listener {

  private final WorldEditPlugin worldEditPlugin;

  public WorldEditAdapter() {

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
    event.setExtent(new WorldEditBlockListener(actor, world, event.getExtent()));
  }

  public void register() {

    worldEditPlugin.getWorldEdit().getEventBus().register(this);
  }

  public void unregister() {

    worldEditPlugin.getWorldEdit().getEventBus().unregister(this);
  }

}
