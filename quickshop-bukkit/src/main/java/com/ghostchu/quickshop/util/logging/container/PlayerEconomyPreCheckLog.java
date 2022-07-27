package com.ghostchu.quickshop.util.logging.container;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class PlayerEconomyPreCheckLog {
    private static int v = 1;
    private boolean beforeTrading;
    private UUID player;
    private double holding;
}
