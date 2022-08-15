package com.ghostchu.quickshop.compatibility.nocheatplus;

import com.ghostchu.quickshop.api.event.ProtectionCheckStatus;
import com.ghostchu.quickshop.api.event.ShopProtectionCheckEvent;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class Main extends CompatibilityModule implements Listener {
    @Override
    public void init() {

    }

    @EventHandler(ignoreCancelled = true)
    public void onFakeEventBegin(ShopProtectionCheckEvent event) {
        if (event.getStatus() == ProtectionCheckStatus.BEGIN) {
            NCPExemptionManager.exemptPermanently(event.getPlayer().getUniqueId());
        } else if (event.getStatus() == ProtectionCheckStatus.END) {
            NCPExemptionManager.unexempt(event.getPlayer().getUniqueId());
        }
    }
}
