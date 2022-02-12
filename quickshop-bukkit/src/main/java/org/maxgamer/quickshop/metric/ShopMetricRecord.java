package org.maxgamer.quickshop.metric;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
@Builder
public class ShopMetricRecord {
    private long time;
    private int x;
    private int y;
    private int z;
    private String world;
    private ShopOperationEnum type;
    private double total;
    private double tax;
    private int amount;
    private UUID player;

}
