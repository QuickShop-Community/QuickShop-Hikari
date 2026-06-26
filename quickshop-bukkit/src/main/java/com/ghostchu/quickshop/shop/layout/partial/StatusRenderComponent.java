package com.ghostchu.quickshop.shop.layout.partial;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.localization.text.ProxiedLocale;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.SignRenderSnapshot;
import com.ghostchu.quickshop.api.shop.layout.RenderComponent;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class StatusRenderComponent implements RenderComponent {

  @Override
  public String placeholder() {

    return "status";
  }

  @Override
  public boolean fullLine() {

    return false;
  }

  @Override
  public boolean supportsSnapshot() {

    return true;
  }

  @Override
  public Component renderSnapshot(final @NotNull SignRenderSnapshot snapshot, final ProxiedLocale locale) {

    return QuickShop.getInstance().text().of(snapshot.shopStateKey()).forLocale();
  }

  @Override
  public Component render(final @NotNull Shop shop, final @NotNull ItemStack item, final ProxiedLocale locale) {

    return QuickShop.getInstance().text().of(shop.shopState().translationKey()).forLocale();
  }
}
