package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.api.serialize.BlockPos;
import com.ghostchu.quickshop.api.shop.ShopInfoStorage;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class ShopCreationLog {
    private static int v = 1;
    private UUID creator;
    private ShopInfoStorage shop;
    private BlockPos location;
}
