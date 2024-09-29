package com.ghostchu.quickshop.util.paste.item;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import com.ghostchu.quickshop.util.paste.util.HTMLTable;
import org.jetbrains.annotations.NotNull;

public class DisplaySystemItem implements SubPasteItem {

  private final QuickShop plugin = QuickShop.getInstance();

  @Override
  public @NotNull String genBody() {

    return buildContent();
  }

  @NotNull
  private String buildContent() {

    final HTMLTable table = new HTMLTable(2, true);
    table.insert("Display Enabled", plugin.isDisplayEnabled());
    table.insert("Display Provider", AbstractDisplayItem.getNowUsing().name());
    table.insert("VirtualDisplayItem Status", !AbstractDisplayItem.isVirtualDisplayDoesntWork());
    return table.render();
  }

  @Override
  public @NotNull String getTitle() {

    return "Display Item Manager";
  }

}
