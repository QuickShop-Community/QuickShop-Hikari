package com.ghostchu.quickshop.addon.displaycontrol.bean;

public enum DisplayOption {
    AUTO(0),
    ENABLED(1),
    DISABLED(2);
    private final int id;

    DisplayOption(int id) {
        this.id = id;
    }

    public boolean isDisplayAvailable(boolean bedrock) {
        if (this == AUTO) {
            return !bedrock;
        }
        return this == ENABLED;
    }

    public static DisplayOption fromId(int id) {
        for (DisplayOption value : values()) {
            if (value.id == id) {
                return value;
            }
        }
        return AUTO;
    }

    public int getId() {
        return id;
    }
}
