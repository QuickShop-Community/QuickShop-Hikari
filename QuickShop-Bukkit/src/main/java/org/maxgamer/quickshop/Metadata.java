package org.maxgamer.quickshop;

import lombok.Getter;
import org.bukkit.NamespacedKey;
@Getter
public class Metadata {
    private final NamespacedKey hopperPersistentDataKey = new NamespacedKey(QuickShop.getInstance(), "hopper-persistent-data");
}
