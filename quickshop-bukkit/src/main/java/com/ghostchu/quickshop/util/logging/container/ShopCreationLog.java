package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.api.serialize.BlockPos;
import com.ghostchu.quickshop.api.shop.ShopInfoStorage;
import com.ghostchu.quickshop.common.obj.QUser;
import lombok.Data;

@Data
public class ShopCreationLog {
    private static int v = 2;
    private QUser creator;
    private ShopInfoStorage shop;
    private BlockPos location;

    public ShopCreationLog(QUser creator, ShopInfoStorage shop, BlockPos location) {
        this.creator = creator;
        this.shop = shop;
        this.location = location;
    }
}
