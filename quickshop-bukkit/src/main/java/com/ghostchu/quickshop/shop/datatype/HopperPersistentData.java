package com.ghostchu.quickshop.shop.datatype;

import com.google.gson.annotations.Expose;
import lombok.Getter;

import java.util.UUID;

@Getter
public class HopperPersistentData {
    @Expose
    private final UUID player;

    public HopperPersistentData(UUID player) {
        this.player = player;
    }
}
