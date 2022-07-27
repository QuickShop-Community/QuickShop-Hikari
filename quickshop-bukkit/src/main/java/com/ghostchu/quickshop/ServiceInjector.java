package com.ghostchu.quickshop;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ServiceInjector used for "Replaceable Modules" features that allow 3rd party QuickShop addon
 * replace some modules used in QuickShop internal by register as service.
 *
 * @author Ghost_chu
 */
public class ServiceInjector {
    public static @Nullable <T> T getInjectedService(@NotNull Class<T> clazz, T def) {
        @Nullable RegisteredServiceProvider<? extends T> registeredServiceProvider =
                Bukkit.getServicesManager().getRegistration(clazz);
        if (registeredServiceProvider == null) {
            return def;
        } else {
            return registeredServiceProvider.getProvider();
        }
    }
}
