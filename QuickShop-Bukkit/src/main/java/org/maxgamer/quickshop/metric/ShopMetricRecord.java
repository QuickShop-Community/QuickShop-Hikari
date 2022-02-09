package org.maxgamer.quickshop.metric;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ShopMetricRecord {
    private long time;
    private int x;
    private int y;
    private int z;
    private String world;
    private ShopOperationEnum type;

}
