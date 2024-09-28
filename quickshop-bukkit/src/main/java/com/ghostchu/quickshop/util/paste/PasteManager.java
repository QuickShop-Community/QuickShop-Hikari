package com.ghostchu.quickshop.util.paste;

import com.ghostchu.quickshop.util.paste.item.SubPasteItem;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PasteManager {

  private final Map<Plugin, List<WeakReference<SubPasteItem>>> registry = new LinkedHashMap<>();

  public void register(@NotNull final Plugin plugin, @NotNull final List<SubPasteItem> collectors) {

    final List<WeakReference<SubPasteItem>> collectorsWeakCopy = collectors.stream().map(WeakReference::new).toList();
    final List<WeakReference<SubPasteItem>> registered = registry.getOrDefault(plugin, new ArrayList<>());
    registered.addAll(collectorsWeakCopy);
    registry.put(plugin, registered);
  }

  public void register(@NotNull final Plugin plugin, @NotNull final SubPasteItem collector) {

    final List<WeakReference<SubPasteItem>> registered = registry.getOrDefault(plugin, new ArrayList<>());
    registered.add(new WeakReference<>(collector));
    registry.put(plugin, registered);
  }

  public void unregister(@NotNull final Plugin plugin, @NotNull final SubPasteItem collector) {

    final List<WeakReference<SubPasteItem>> registered = registry.getOrDefault(plugin, Collections.emptyList());
    registered.removeIf(cWeak->{
      final SubPasteItem c = cWeak.get();
      return c == null || c.equals(collector);
    });
  }

  public void unregister(@NotNull final Plugin plugin) {

    registry.remove(plugin);
  }

  @NotNull
  public List<SubPasteItem> getAllRegistered() {

    final List<SubPasteItem> collectors = new ArrayList<>();
    for(final List<WeakReference<SubPasteItem>> collectorList : registry.values()) {
      for(final WeakReference<SubPasteItem> weakReference : collectorList) {
        final SubPasteItem collector = weakReference.get();
        if(collector != null) {
          collectors.add(collector);
        }
      }
    }
    return collectors;
  }
}
