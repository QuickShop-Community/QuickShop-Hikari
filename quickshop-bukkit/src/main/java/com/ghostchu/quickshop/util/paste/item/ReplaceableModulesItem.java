package com.ghostchu.quickshop.util.paste.item;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.ServiceInjector;
import com.ghostchu.quickshop.shop.DisplayProvider;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import com.ghostchu.quickshop.util.paste.util.HTMLTable;
import org.jetbrains.annotations.NotNull;

public class ReplaceableModulesItem implements SubPasteItem {

  private final String economyCore;
  private final String itemMatcher;
  private final String displayItem;

  public ReplaceableModulesItem() {

    final QuickShop plugin = QuickShop.getInstance();
    itemMatcher = plugin.getItemMatcher().getName() + "@" + plugin.getItemMatcher().getPlugin().getName();
    if(plugin.getEconomy() == null) {
      economyCore = "undefined@unknown";
    } else {
      economyCore = plugin.getEconomy().getName() + "@" + plugin.getEconomy().getPlugin().getName();
    }
    final DisplayProvider provider = ServiceInjector.getInjectedService(DisplayProvider.class, null);
    if(provider == null) {
      displayItem = AbstractDisplayItem.getNowUsing().name() + "@QuickShop-Hikari";
    } else {
      displayItem = provider.getClass().getSimpleName() + "@" + provider.getProvider().getName();
    }
  }

  @Override
  public @NotNull String genBody() {

    return buildContent();
  }

  @Override
  public @NotNull String getTitle() {

    return "Replaceable Modules";
  }

  @NotNull
  private String buildContent() {

    final HTMLTable table = new HTMLTable(2, true);
    table.insert("Economy Core", economyCore);
    table.insert("Item Matcher", itemMatcher);
    table.insert("DisplayItem", displayItem);
    return table.render();
  }
}
