package com.ghostchu.quickshop.api.event;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class ItemPreviewComponentPopulateEvent extends AbstractQSEvent {
    private Component component;

    public ItemPreviewComponentPopulateEvent(@NotNull Component component) {
        this.component = component;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }
}
