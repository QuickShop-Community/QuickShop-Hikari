package com.ghostchu.quickshop.compatibility.worldedit;

import com.ghostchu.quickshop.QuickShop;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.eventbus.EventHandler;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.World;
import org.bukkit.event.Listener;

public class WorldEditAdapter implements Listener {
    private final WorldEditPlugin weBukkit;

    public WorldEditAdapter(WorldEditPlugin weBukkit) {
        this.weBukkit = weBukkit;
    }

    public void register() {
        weBukkit.getWorldEdit().getEventBus().register(this);
    }

    public void unregister() {
        weBukkit.getWorldEdit().getEventBus().unregister(this);
    }

    @Subscribe(priority = EventHandler.Priority.NORMAL)
    public void proxyEditSession(EditSessionEvent event) {
        Actor actor = event.getActor();
        World world = event.getWorld();
        if (actor != null && event.getStage() == EditSession.Stage.BEFORE_CHANGE) {
            event.setExtent(new WorldEditBlockListener(actor, world, event.getExtent(), QuickShop.getInstance()));
        }
    }


}
