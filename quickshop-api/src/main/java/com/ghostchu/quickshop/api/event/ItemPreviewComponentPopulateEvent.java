package com.ghostchu.quickshop.api.event;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ItemPreviewComponentPopulateEvent extends AbstractQSEvent {
    private final Player player;
    private Component component;

    public ItemPreviewComponentPopulateEvent(@NotNull Component component, @NotNull Player p) {
        this.component = component;
        this.player = p;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public Player getPlayer() {
        return player;
    }
}
