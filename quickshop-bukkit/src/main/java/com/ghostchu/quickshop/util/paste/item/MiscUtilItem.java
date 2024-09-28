package com.ghostchu.quickshop.util.paste.item;

import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.paste.util.HTMLTable;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.StringJoiner;

public class MiscUtilItem implements SubPasteItem {

  @SuppressWarnings("deprecation")
  @Override
  public @NotNull String genBody() {

    final StringJoiner joiner = new StringJoiner("<br/>");
    joiner.add("<h5>General</h5>");
    final HTMLTable general = new HTMLTable(2, true);
    general.insert("Dye Color", String.valueOf(Util.getDyeColor()));
    general.insert("BungeeCord", Util.checkIfBungee());
    general.insert("Cache Folder", Util.getCacheFolder());
    general.insert("Sign Material", Util.getSignMaterial().getKey().toString());
    general.insert("Vertical Facing", CommonUtil.list2String(Util.getVerticalFacing()));
    general.insert("Dev Edition", Util.isDevEdition());
    general.insert("Dev Mode (Debug Mode)", Util.isDevMode());
    joiner.add(general.render());
    joiner.add("<h5>Shopable Types</h5>");
    final HTMLTable shopable = new HTMLTable(2);
    shopable.setTableTitle("Bukkit Material", "Minecraft NamespacedKey");
    for(final Material material : Util.getShopables()) {
      shopable.insert(material.name(), material.getKey().toString());
    }
    joiner.add(shopable.render());
    joiner.add("<h5>Custom StackSize</h5>");
    final HTMLTable customStackSize = new HTMLTable(2);
    customStackSize.setTableTitle("Bukkit Material", "Minecraft NamespacedKey", "Override StackSize");
    for(final Map.Entry<Material, Integer> entry : Util.getCustomStacksize().entrySet()) {
      customStackSize.insert(entry.getKey().name(), entry.getKey().getKey().toString(), entry.getValue());
    }
    joiner.add(customStackSize.render());
    return joiner.toString();
  }

  @Override
  public @NotNull String getTitle() {

    return "Misc Util";
  }
}
