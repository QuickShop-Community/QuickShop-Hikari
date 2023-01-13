package com.ghostchu.quickshop.util.paste;

import com.ghostchu.quickshop.util.paste.item.SubPasteItem;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.*;

public class PasteManager {
    private final Map<Plugin, List<WeakReference<SubPasteItem>>> registry = new LinkedHashMap<>();

    public void register(@NotNull Plugin plugin, @NotNull List<SubPasteItem> collectors) {
        List<WeakReference<SubPasteItem>> collectorsWeakCopy = collectors.stream().map(WeakReference::new).toList();
        List<WeakReference<SubPasteItem>> registered = registry.getOrDefault(plugin, new ArrayList<>());
        registered.addAll(collectorsWeakCopy);
        registry.put(plugin, registered);
    }

    public void register(@NotNull Plugin plugin, @NotNull SubPasteItem collector) {
        List<WeakReference<SubPasteItem>> registered = registry.getOrDefault(plugin, new ArrayList<>());
        registered.add(new WeakReference<>(collector));
        registry.put(plugin, registered);
    }

    public void unregister(@NotNull Plugin plugin, @NotNull SubPasteItem collector) {
        List<WeakReference<SubPasteItem>> registered = registry.getOrDefault(plugin, Collections.emptyList());
        registered.removeIf(cWeak -> {
            SubPasteItem c = cWeak.get();
            return c == null || c.equals(collector);
        });
    }

    public void unregister(@NotNull Plugin plugin) {
        registry.remove(plugin);
    }

    @NotNull
    public List<SubPasteItem> getAllRegistered() {
        List<SubPasteItem> collectors = new ArrayList<>();
        for (List<WeakReference<SubPasteItem>> collectorList : registry.values()) {
            for (WeakReference<SubPasteItem> weakReference : collectorList) {
                SubPasteItem collector = weakReference.get();
                if (collector != null) {
                    collectors.add(collector);
                }
            }
        }
        return collectors;
    }
}
