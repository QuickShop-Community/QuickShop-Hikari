package org.maxgamer.quickshop.nonquickshopstuff.net.ess3.essentialsx;

import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;

public class PaperServerStateProvider implements ServerStateProvider {
    @Override
    public boolean isStopping() {
        try {
            return (Boolean) Class.forName("org.bukkit.bukkit.Bukkit").getMethod("isStopping").invoke(Bukkit.getServer());
        } catch (IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }
}
