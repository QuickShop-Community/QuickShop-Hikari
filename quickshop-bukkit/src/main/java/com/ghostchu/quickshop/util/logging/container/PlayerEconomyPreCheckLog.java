package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.obj.QUserSimpleRecord;
import lombok.Data;

@Data
public class PlayerEconomyPreCheckLog {
    private static int v = 2;
    private boolean beforeTrading;
    private QUserSimpleRecord player;
    private double holding;

    public PlayerEconomyPreCheckLog(boolean beforeTrading, QUser player, double holding) {
        this.beforeTrading = beforeTrading;
        this.player = QUserSimpleRecord.wrap(player);
        this.holding = holding;
    }
}
