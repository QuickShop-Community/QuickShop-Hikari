package com.ghostchu.quickshop.util.logging.container;

import lombok.Data;

import java.util.UUID;

@Data
public class PlayerEconomyPreCheckLog {
    private static int v = 1;
    private boolean beforeTrading;
    private UUID player;
    private double holding;

    public PlayerEconomyPreCheckLog(boolean beforeTrading, UUID player, double holding) {
        this.beforeTrading = beforeTrading;
        this.player = player;
        this.holding = holding;
    }
}
