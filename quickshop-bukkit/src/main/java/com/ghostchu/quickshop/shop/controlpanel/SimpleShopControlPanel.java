package com.ghostchu.quickshop.shop.controlpanel;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.localization.text.ProxiedLocale;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopControlPanel;
import com.ghostchu.quickshop.api.shop.ShopControlPanelPriority;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.util.MsgUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
  public @NotNull List<Component> generate(@NotNull final Player sender, @NotNull final Shop shop) {
    final QuickShop plugin = QuickShop.getInstance();
    final List<Component> components = new ArrayList<>();
    final ProxiedLocale locale = plugin.text().findRelativeLanguages(sender.getLocale());

    // Buying/Selling Mode
    if (plugin.perm().hasPermission(sender, "quickshop.create.buy")
        && plugin.perm().hasPermission(sender, "quickshop.create.sell")
        && (shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_SHOPTYPE) ||
            plugin.perm().hasPermission(sender, "quickshop.create.admin"))) {
      if (shop.isSelling()) {
        final Component text = plugin.text().of(sender, "controlpanel.mode-selling").forLocale();
        final Component hoverText = plugin.text().of(sender, "controlpanel.mode-selling-hover").forLocale();
        final String clickCommand = MsgUtil.fillArgs("/{0} {1} {2}", plugin.getMainCommand(), plugin.getCommandPrefix("silentbuy"), shop.getRuntimeRandomUniqueId().toString());
        components.add(text
                               .hoverEvent(HoverEvent.showText(hoverText))
                               .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, clickCommand)));
      } else if (shop.isBuying()) {
        final Component text = plugin.text().of(sender, "controlpanel.mode-buying").forLocale();
        final Component hoverText = plugin.text().of(sender, "controlpanel.mode-buying-hover").forLocale();
        final String clickCommand = MsgUtil.fillArgs("/{0} {1} {2}", plugin.getMainCommand(), plugin.getCommandPrefix("silentsell"), shop.getRuntimeRandomUniqueId().toString());
        components.add(text
                               .hoverEvent(HoverEvent.showText(hoverText))
                               .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, clickCommand)));
      }
    }
    // Set Price
    if (plugin.perm().hasPermission(sender, "quickshop.other.price")
        || (plugin.perm().hasPermission(sender, "quickshop.create.changeprice") && shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_PRICE))) {
      final Component text = MsgUtil.fillArgs(
              plugin.text().of(sender, "controlpanel.price").forLocale(),
              LegacyComponentSerializer.legacySection().deserialize(
                      (plugin.getConfig().getBoolean("use-decimal-format"))
                      ? MsgUtil.decimalFormat(shop.getPrice())
                      : Double.toString(shop.getPrice()))
                                             );
      final Component hoverText = plugin.text().of(sender, "controlpanel.price-hover").forLocale();
      final String clickCommand = MsgUtil.fillArgs("/{0} {1} ", plugin.getMainCommand(), plugin.getCommandPrefix("price"));

      components.add(text
                             .hoverEvent(HoverEvent.showText(hoverText))
                             .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickCommand)));
    }
    //Set amount per bulk
    if (plugin.isAllowStack()) {
      if (plugin.perm().hasPermission(sender, "quickshop.other.amount") ||
          shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_STACK_AMOUNT) &&
          plugin.perm().hasPermission(sender, "quickshop.create.changeamount")) {
        final Component text = plugin.text().of(sender, "controlpanel.stack", shop.getItem().getAmount()).forLocale();
        final Component hoverText = plugin.text().of(sender, "controlpanel.stack-hover").forLocale();
        final String clickCommand = MsgUtil.fillArgs("/{0} {1} ", plugin.getMainCommand(), plugin.getCommandPrefix("size"));
        components.add(text
                               .hoverEvent(HoverEvent.showText(hoverText))
                               .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickCommand)));
      }
    }
    if (!shop.isUnlimited()) {
      // Refill
      if (plugin.perm().hasPermission(sender, "quickshop.refill")) {
        final Component text = plugin.text().of(sender, "controlpanel.refill", shop.getPrice()).forLocale();
        final Component hoverText = plugin.text().of(sender, "controlpanel.refill-hover").forLocale();
        final String clickCommand = MsgUtil.fillArgs("/{0} {1} ", plugin.getMainCommand(), plugin.getCommandPrefix("refill"));
        components.add(text
                               .hoverEvent(HoverEvent.showText(hoverText))
                               .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickCommand)));
      }
      // Empty
      if (plugin.perm().hasPermission(sender, "quickshop.empty")) {
        final Component text = plugin.text().of(sender, "controlpanel.empty", shop.getPrice()).forLocale();
        final Component hoverText = plugin.text().of(sender, "controlpanel.empty-hover").forLocale();
        final String clickCommand = MsgUtil.fillArgs("/{0} {1} {2}", plugin.getMainCommand(), plugin.getCommandPrefix("silentempty"), shop.getRuntimeRandomUniqueId().toString());
        components.add(text
                               .hoverEvent(HoverEvent.showText(hoverText))
                               .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, clickCommand)));
      }
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
