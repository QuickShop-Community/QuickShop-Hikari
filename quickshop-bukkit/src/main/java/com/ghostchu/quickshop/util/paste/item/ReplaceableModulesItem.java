package com.ghostchu.quickshop.util.paste.item;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.paste.util.HTMLTable;
import org.jetbrains.annotations.NotNull;

public class ReplaceableModulesItem implements SubPasteItem {
    private final String economyCore;
    private final String itemMatcher;

    public ReplaceableModulesItem() {
        QuickShop plugin = QuickShop.getInstance();
        itemMatcher = plugin.getItemMatcher().getName() + "@" + plugin.getItemMatcher().getPlugin().getName();


        if (plugin.getEconomy() == null) {
            economyCore = "undefined@unknown";
        } else {
            economyCore = plugin.getEconomy().getName() + "@" + plugin.getEconomy().getPlugin().getName();
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
        HTMLTable table = new HTMLTable(2, true);
        table.insert("Economy Core", economyCore);
        table.insert("Item Matcher", itemMatcher);
        return table.render();
    }
}
