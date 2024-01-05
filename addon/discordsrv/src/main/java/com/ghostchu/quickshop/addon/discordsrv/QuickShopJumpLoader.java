package com.ghostchu.quickshop.addon.discordsrv;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.QuickShopBukkit;
import com.ghostchu.quickshop.api.QuickShopAPI;

public class QuickShopJumpLoader {
    public QuickShop getQuickShopInstance() {
        return ((QuickShopBukkit) QuickShopAPI.getPluginInstance()).getQuickShop();
    }
}
