package org.maxgamer.quickshop.api.chat;

import net.md_5.bungee.api.chat.BaseComponent;

public class ComponentPackage {
    private final BaseComponent[] components;

    public ComponentPackage(BaseComponent... components) {
        this.components = components;
    }

    public BaseComponent[] getComponents() {
        return components;
    }
}
