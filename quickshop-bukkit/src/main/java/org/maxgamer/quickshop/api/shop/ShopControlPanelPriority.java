package org.maxgamer.quickshop.api.shop;

public enum ShopControlPanelPriority {
    LOWEST(16),
    LOW(32),
    NORMAL(64),
    HIGH(96),
    HIGHEST(128);

    private int priority;

    ShopControlPanelPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
