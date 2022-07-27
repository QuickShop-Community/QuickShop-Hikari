package com.ghostchu.quickshop.api.shop.display;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum DisplayType {
    /*
     * REALITEM = USE REAL DROPPED ITEM
     * ARMORSTAND = USE ARMORSTAND DISPLAY
     * VIRTUALITEM = USE VIRTUAL DROPPED ITEM (CLIENT SIDE)
     * */
    REALITEM(0),
    //  ARMORSTAND(1),
    VIRTUALITEM(2),
    CUSTOM(3);

    private static final Map<Integer, DisplayType> TYPE_MAP;

    static {
        Map<Integer, DisplayType> map = new HashMap<>(values().length);
        for (DisplayType type : values()) {
            map.put(type.id, type);
        }
        TYPE_MAP = Collections.unmodifiableMap(map);
    }

    private final int id;

    DisplayType(int id) {
        this.id = id;
    }

    public static @NotNull DisplayType fromID(int id) {
        return TYPE_MAP.getOrDefault(id, CUSTOM);
    }

    public static int toID(@NotNull DisplayType displayType) {
        return displayType.id;
    }

    public int toID() {
        return id;
    }
}
