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
    // Owner
    if (!plugin.perm().hasPermission(sender, "quickshop.setowner")) {
      components.add(plugin.text().of(sender, "menu.owner", shop.ownerName(locale)).forLocale());
    } else {
      final Component text;
      if (plugin.getConfig().getBoolean("shop.show-owner-uuid-in-controlpanel-if-op") && shop.isUnlimited()) {
        text = plugin.text().of(sender, "controlpanel.setowner-uuid", shop.ownerName(locale), shop.getOwner().toString()).forLocale();
      } else {
        text = plugin.text().of(sender, "controlpanel.setowner", shop.ownerName(locale)).forLocale();
      }
      components.add(text
                             .hoverEvent(HoverEvent.showText(plugin.text().of(sender, "controlpanel.setowner-hover").forLocale()))
                             .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, MsgUtil.fillArgs("/{0} {1} ", plugin.getMainCommand(), plugin.getCommandPrefix("transferownership")))));
    }
    // Unlimited
    if (plugin.perm().hasPermission(sender, "quickshop.unlimited")) {
      final Component text = plugin.text().of(sender, "controlpanel.unlimited", MsgUtil.bool2String(shop.isUnlimited())).forLocale();
      final Component hoverText = plugin.text().of(sender, "controlpanel.unlimited-hover").forLocale();
      final String clickCommand = MsgUtil.fillArgs("/{0} {1} {2}", plugin.getMainCommand(), plugin.getCommandPrefix("silentunlimited"), shop.getRuntimeRandomUniqueId().toString());
      components.add(text
                             .hoverEvent(HoverEvent.showText(hoverText))
                             .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, clickCommand)));
    }
    if (plugin.perm().hasPermission(sender, "quickshop.other.freeze")
        || (plugin.perm().hasPermission(sender, "quickshop.togglefreeze")
            && shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_SHOPTYPE))) {

      final Component text = ((QuickShop)plugin).text().of(sender, "controlpanel.freeze", MsgUtil.bool2String(shop.isFrozen())).forLocale();

      final Component hoverText = plugin.text().of(sender, "controlpanel.freeze-hover").forLocale();
      final String clickCommand = MsgUtil.fillArgs("/{0} {1} {2}", plugin.getMainCommand(), plugin.getCommandPrefix("silentfreeze"), shop.getRuntimeRandomUniqueId().toString());
      components.add(text
                             .hoverEvent(HoverEvent.showText(hoverText))
                             .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, clickCommand)));
    }

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

    // ToggleDisplay
    if ((plugin.perm().hasPermission(sender, "quickshop.other.toggledisplay")
         || shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.TOGGLE_DISPLAY))
        && plugin.isDisplayEnabled()) {
      final Component text = plugin.text().of(sender, "controlpanel.toggledisplay", MsgUtil.bool2String(!shop.isDisableDisplay())).forLocale();
      final Component hoverText = plugin.text().of(sender, "controlpanel.toggledisplay-hover").forLocale();
      final String clickCommand = MsgUtil.fillArgs("/{0} {1} {2}", plugin.getMainCommand(), plugin.getCommandPrefix("silenttoggledisplay"), shop.getRuntimeRandomUniqueId().toString());
      components.add(text
                             .hoverEvent(HoverEvent.showText(hoverText))
                             .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, clickCommand)));
    }

    // View purchase logs
    if (plugin.perm().hasPermission(sender, "quickshop.other.history")
        || (plugin.perm().hasPermission(sender, "quickshop.history") && shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.VIEW_PURCHASE_LOGS))) {
      final Component text = plugin.text().of(sender, "controlpanel.history", MsgUtil.bool2String(!shop.isDisableDisplay())).forLocale();
      final Component hoverText = plugin.text().of(sender, "controlpanel.history-hover").forLocale();
      final String clickCommand = MsgUtil.fillArgs("/{0} {1} {2}", plugin.getMainCommand(), plugin.getCommandPrefix("silenthistory"), shop.getRuntimeRandomUniqueId().toString());
      components.add(text
                             .hoverEvent(HoverEvent.showText(hoverText))
                             .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, clickCommand)));
    }

    // --------------------- FUNCTION BUTTON ---------------------

    // Remove
    if (plugin.perm().hasPermission(sender, "quickshop.other.destroy") || shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.DELETE)) {
      final Component text = plugin.text().of(sender, "controlpanel.remove", shop.getPrice()).forLocale();
      final Component hoverText = plugin.text().of(sender, "controlpanel.remove-hover").forLocale();
      final String clickCommand = MsgUtil.fillArgs("/{0} {1} {2}", plugin.getMainCommand(), plugin.getCommandPrefix("silentremove"), shop.getRuntimeRandomUniqueId().toString());
      components.add(text
                             .hoverEvent(HoverEvent.showText(hoverText))
                             .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, clickCommand)));
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
