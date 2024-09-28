package com.ghostchu.quickshop.api.registry;

import java.util.Map;

public interface RegistryManager {

  Registry getRegistry(BuiltInRegistry registry);

  Registry getRegistry(String registryName);

  Map<String, Registry> getRegistryList();

  void registerRegistry(String namespacedName, Registry registry);

  void unregisterRegistry(String namespacedName);

  Map<String, Registry> getRegistries();
}
