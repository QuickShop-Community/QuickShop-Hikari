package com.ghostchu.quickshop.api.shop;

/**
 * ShopControlPanel showcase priority
 */
public enum ShopControlPanelPriority {
    LOWEST(16),
    LOW(32),
    NORMAL(64),
    HIGH(96),
    HIGHEST(128);

    private final int priority;

    ShopControlPanelPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Gets the priority number of the control panel
     *
     * @return priority
     */
    public int getPriority() {
        return priority;
    }
}
