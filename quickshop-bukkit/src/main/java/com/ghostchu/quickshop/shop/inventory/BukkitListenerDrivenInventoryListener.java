package com.ghostchu.quickshop.shop.inventory;

import org.bukkit.Location;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface BukkitListenerDrivenInventoryListener {
    // This listener to fix for:
    //https://discord.com/channels/942378696415797298/942378809632649256/1156394450570969089
    //https://discord.com/channels/942378696415797298/942378809632649256/1207787041992216586
    //https://discord.com/channels/942378696415797298/942378809632649256/1207783977352437881
    //https://discord.com/channels/942378696415797298/942378809632649256/1208807999003562044
    //https://discord.com/channels/942378696415797298/942378809632649256/1221584126432514110
    boolean notify(Location updated); // May call from async thread
}
