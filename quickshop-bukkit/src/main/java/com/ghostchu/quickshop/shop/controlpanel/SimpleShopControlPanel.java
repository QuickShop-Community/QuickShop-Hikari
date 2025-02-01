package com.ghostchu.quickshop.shop.controlpanel;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.Phase;
import com.ghostchu.quickshop.api.event.panel.ControlComponentGenerateEvent;
import com.ghostchu.quickshop.api.shop.ControlComponent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopControlPanel;
import com.ghostchu.quickshop.api.shop.ShopControlPanelPriority;
import com.ghostchu.quickshop.obj.QUserImpl;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public class SimpleShopControlPanel implements ShopControlPanel {

  /**
   * Generate components for the shop control panel.
   *
   * @param sender The player
   * @param shop   The shop
   *
   * @return The components, or empty list if nothing to show. Every component will be shown in a
   * new line.
   */
  @Override
  public @NotNull LinkedList<Component> generate(@NotNull final Player sender, @NotNull final Shop shop) {
    final LinkedList<Component> components = new LinkedList<>();

    for(final ControlComponent controlComponent : QuickShop.getInstance().controlPanelManager().controlComponents().values()) {

      ControlComponentGenerateEvent event = new ControlComponentGenerateEvent(Phase.PRE, shop, QUserImpl.createFullFilled(sender), controlComponent);
      event.callEvent();

      event = (ControlComponentGenerateEvent)event.clone(Phase.MAIN);
      if(event.callCancellableEvent()) {

        continue;
      }

      if(!event.updated().applies(QuickShop.getInstance(), sender, shop)) {

        continue;
      }

      components.add(event.updated().generate(QuickShop.getInstance(), sender, shop));

      event = (ControlComponentGenerateEvent)event.clone(Phase.POST);
      event.callEvent();
    }

    return components;
  }

  /**
   * The shop control panel's priority. HIGH = Earlier shown LOW = Later shown
   *
   * @return The priority.
   */
  @Override
  public @NotNull ShopControlPanelPriority getPriority() {

    return ShopControlPanelPriority.HIGH;
  }

  /**
   * The shop control panel impl's plugin instance.
   *
   * @return Your plugin instance;
   */
  @Override
  public @NotNull Plugin getPlugin() {

    return QuickShop.getInstance().getJavaPlugin();
  }
}
