package com.ghostchu.quickshop.registry;

import com.ghostchu.quickshop.api.registry.BuiltInRegistry;
import com.ghostchu.quickshop.api.registry.Registry;
import com.ghostchu.quickshop.api.registry.RegistryManager;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class SimpleRegistryManager implements RegistryManager {
    private Map<String, Registry> registryList = new ConcurrentSkipListMap<>();

    @Override
    public Registry getRegistry(BuiltInRegistry registry) {
        return getRegistry(registry.getName());
    }

    @Override
    public Registry getRegistry(String registryName) {
        return registryList.get(registryName);
    }

    @Override
    public Map<String, Registry> getRegistryList() {
        return ImmutableMap.copyOf(registryList);
    }

    @Override
    public void registerRegistry(String namespacedName, Registry registry) {
        if (registryList.get(namespacedName) != null) {
            throw new IllegalArgumentException("Registry " + namespacedName + " already registered in RegistryManager!");
        }
        this.registryList.put(namespacedName, registry);
    }

    @Override
    public void unregisterRegistry(String namespacedName) {
        this.registryList.remove(namespacedName);
    }

    @Override
    @NotNull
    public Map<String, Registry> getRegistries() {
        return ImmutableMap.copyOf(registryList);
    }
}
