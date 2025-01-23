package com.ghostchu.quickshop.shop.controlpanel;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopControlPanel;
import com.ghostchu.quickshop.api.shop.ShopControlPanelManager;
import com.ghostchu.quickshop.util.ChatSheetPrinter;
import com.ghostchu.quickshop.util.paste.item.SubPasteItem;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleShopControlPanelManager implements ShopControlPanelManager, SubPasteItem {

  private final QuickShop plugin;
  private final Lock LOCK = new ReentrantLock();
  private final Map<ShopControlPanel, Integer> registry = new LinkedHashMap<>();

  public SimpleShopControlPanelManager(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void openControlPanel(@NotNull final Player player, @NotNull final Shop shop) {

    final ChatSheetPrinter chatSheetPrinter = new ChatSheetPrinter(player);
    chatSheetPrinter.printHeader();
    chatSheetPrinter.printLine(plugin.text().of(player, "controlpanel.infomation").forLocale());
    final List<Component> total = new ArrayList<>();
    for(final ShopControlPanel entry : registry.keySet()) {
      try {
        total.addAll(entry.generate(player, shop));
      } catch(Exception e) {
        try {
          plugin.logger().warn("Failed to generate control panel for {}. Contact the developer of the plugin {}.", entry.getPlugin().getName(), entry.getClass().getName());
        } catch(Exception e2) {
          plugin.logger().warn("Failed to generate control panel for {}. Contact the developer of that plugin", entry.getClass().getName());
        }
      }
    }
    total.forEach(chatSheetPrinter::printLine);
    chatSheetPrinter.printFooter();
  }

  @Override
  public void register(@NotNull final ShopControlPanel panel) {

    LOCK.lock();
    try {
      registry.put(panel, panel.getInternalPriority());
    } finally {
      LOCK.unlock();
    }
    resort();
  }

  private void resort() {

    if(!LOCK.tryLock()) {
      throw new IllegalStateException("Cannot resort while another thread is sorting");
    }
    final List<Map.Entry<ShopControlPanel, Integer>> list = new ArrayList<>(registry.entrySet());
    list.sort((o1, o2)->o2.getValue().compareTo(o1.getValue()));
    registry.clear();
    list.forEach(k->registry.put(k.getKey(), k.getValue())); // Re-sort
    LOCK.unlock();
  }

  @Override
  public void unregister(@NotNull final Plugin plugin) {

    LOCK.lock();
    try {
      final List<ShopControlPanel> pending = new ArrayList<>();
      for(final Map.Entry<ShopControlPanel, Integer> entry : registry.entrySet()) {
        if(entry.getKey().getPlugin().equals(plugin)) {
          pending.add(entry.getKey());
        }
      }
      pending.forEach(this::unregister);
    } finally {
      LOCK.unlock();
    }

  }

  @Override
  public void unregister(@NotNull final ShopControlPanel panel) {

    LOCK.lock();
    try {
      registry.remove(panel);
    } finally {
      LOCK.unlock();
    }
    // Doesn't need resort
  }

  @Override
  public @NotNull String genBody() {

    return "<p>Registered Control Panels: " + registry.size() + "</p>";
  }

  @Override
  public @NotNull String getTitle() {

    return "Shop ControlPanel Manager";
  }
}
