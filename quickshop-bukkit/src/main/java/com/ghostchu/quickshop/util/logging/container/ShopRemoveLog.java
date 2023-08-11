package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.ShopInfoStorage;
import com.ghostchu.quickshop.common.obj.QUser;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.obj.QUserImpl;
import lombok.Data;

import java.util.UUID;

@Data
public class ShopRemoveLog {
    private static int v = 2;
    private QUser player;
    private String reason;
    private ShopInfoStorage shop;

    public ShopRemoveLog(QUser player, String reason, ShopInfoStorage shop) {
        this.player = player;
        this.reason = reason;
        this.shop = shop;
    }

    @Deprecated
    public ShopRemoveLog(UUID player, String reason, ShopInfoStorage shop) {
        QUser qUser;
        if (player == null || CommonUtil.getNilUniqueId().equals(player)) {
            qUser = QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "SYSTEM", false);
        } else {
            qUser = QUserImpl.createSync(QuickShop.getInstance().getPlayerFinder(), player);
        }
        this.player = qUser;
        this.reason = reason;
        this.shop = shop;
    }
}
