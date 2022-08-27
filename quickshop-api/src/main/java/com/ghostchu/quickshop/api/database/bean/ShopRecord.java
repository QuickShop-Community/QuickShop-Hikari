package com.ghostchu.quickshop.api.database.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ShopRecord {
    private DataRecord dataRecord;
    private InfoRecord infoRecord;
}
