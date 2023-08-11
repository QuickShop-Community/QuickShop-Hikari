package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.common.obj.QUser;
import lombok.Data;

@Data
public class PlayerEconomyPreCheckLog {
    private static int v = 2;
    private boolean beforeTrading;
    private QUser player;
    private double holding;

    public PlayerEconomyPreCheckLog(boolean beforeTrading, QUser player, double holding) {
        this.beforeTrading = beforeTrading;
        this.player = player;
        this.holding = holding;
    }
}
