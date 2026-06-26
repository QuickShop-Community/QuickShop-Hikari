package com.ghostchu.quickshop.shop;


/*
 * QuickShop-Hikari
 * Copyright (C) 2025 Daniel "creatorfromhell" Vidmar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.localization.text.ProxiedLocale;
import com.ghostchu.quickshop.api.shop.IShopLayoutProvider;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.SignRenderSnapshot;
import com.ghostchu.quickshop.api.shop.layout.RenderComponent;
import com.ghostchu.quickshop.shop.layout.line.HeaderLineRenderComponent;
import com.ghostchu.quickshop.shop.layout.line.ItemLineRenderComponent;
import com.ghostchu.quickshop.shop.layout.line.LevelLineRenderComponent;
import com.ghostchu.quickshop.shop.layout.line.PriceLineRenderComponent;
import com.ghostchu.quickshop.shop.layout.line.TradingLineRenderComponent;
import com.ghostchu.quickshop.shop.layout.partial.AmountRenderComponent;
import com.ghostchu.quickshop.shop.layout.partial.ItemNameRenderComponent;
import com.ghostchu.quickshop.shop.layout.partial.LevelRenderComponent;
import com.ghostchu.quickshop.shop.layout.partial.OwnerRenderComponent;
import com.ghostchu.quickshop.shop.layout.partial.PriceAloneRenderComponent;
import com.ghostchu.quickshop.shop.layout.partial.StatusRenderComponent;
import com.ghostchu.quickshop.shop.layout.partial.StockRenderComponent;
import com.ghostchu.quickshop.shop.layout.partial.TypeRenderComponent;
import com.ghostchu.quickshop.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * SimpleShopLayoutProvider
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class SimpleShopLayoutProvider implements IShopLayoutProvider {

  protected final List<RenderComponent> renderComponents = new ArrayList<>();

  private final String[] defaultLayout = new String[] {
          "header", "trading", "item", "price"
  };

  private final QuickShop plugin;

  public SimpleShopLayoutProvider(final QuickShop plugin) {

    this.plugin = plugin;

    init();
  }

  private void init() {

    renderComponents.add(new HeaderLineRenderComponent());
    renderComponents.add(new ItemLineRenderComponent());
    renderComponents.add(new LevelLineRenderComponent());
    renderComponents.add(new PriceLineRenderComponent());
    renderComponents.add(new TradingLineRenderComponent());
    renderComponents.add(new AmountRenderComponent());
    renderComponents.add(new ItemNameRenderComponent());
    renderComponents.add(new LevelRenderComponent());
    renderComponents.add(new OwnerRenderComponent());
    renderComponents.add(new PriceAloneRenderComponent());
    renderComponents.add(new StatusRenderComponent());
    renderComponents.add(new StockRenderComponent());
    renderComponents.add(new TypeRenderComponent());
  }

  @Override
  public List<RenderComponent> renderComponents() {

    return renderComponents;
  }

  /**
   * Generates a layout template for the specified shop.
   *
   * @param shop the shop instance for which the layout template is to be generated
   *
   * @return a LinkedList of Strings representing the layout template of the shop
   */
  @Override
  public LinkedList<String> layoutTemplate(final Shop shop) {

    final LinkedList<String> template = new LinkedList<>();

    final String baseNode = "shop.layout." + shop.shopType().identifier() + ".line";
    for(int i = 0; i < 4; i++) {

      template.add(QuickShop.getInstance().getConfig().getString(baseNode + (i + 1), defaultLayout[i]));
    }
    return template;
  }

  /**
   * Creates a snapshot of a shop sign that encapsulates the visual and structural data required to
   * render the sign, based on the specified shop and locale.
   *
   * @param shop   the shop instance for which the sign snapshot is to be created
   * @param locale the locale to be used for formatting and rendering the snapshot
   *
   * @return a CompletableFuture that completes with a SignRenderSnapshot, representing the state of
   * the shop sign
   */
  @Override
  public CompletableFuture<SignRenderSnapshot> createSignSnapshot(@NotNull final Shop shop, final @NotNull ProxiedLocale locale) {

    final ItemStack item = shop.getItem().clone();

    final Component levels = renderLevels(shop, locale);
    final CompletableFuture<Boolean> inventoryAvailableFuture = shop.inventoryAvailableAsync();
    final CompletableFuture<Integer> remainingStockFuture = shop.shopType().remainingStockAsync(shop);

    return inventoryAvailableFuture.thenCombine(remainingStockFuture,(inventoryAvailable, remainingStock) -> new SignRenderSnapshot(
                                                  inventoryAvailable,
                                                  shop.ownerName(false, locale),
                                                  shop.isStackingShop(),
                                                  shop.shopType().translationKey(),
                                                  shop.shopType().tradingTranslationKey(),
                                                  shop.shopType().stackTradingTranslationKey(),
                                                  shop.shopType().outOfStockTranslationKey(),
                                                  remainingStock,
                                                  shop.shopState().overrideShopTypeText(),
                                                  shop.shopState().translationKey(),
                                                  Util.getItemStackName(item, locale.getLocale()),
                                                  item.getAmount(),
                                                  plugin.getShopManager().format(shop.getPrice(), shop),
                                                  levels,
                                                  layoutTemplate(shop))
                                         );
  }

  /**
   * Renders the header section of a shop sign snapshot into a component based on the provided
   * locale.
   *
   * @param snapshot the snapshot representing the current state of the shop sign's header to be
   *                 rendered
   * @param locale   the locale to be used for rendering the header component
   *
   * @return a component representing the rendered header section of the shop sign snapshot
   *
   * @deprecated Individual render methods replaced my the render component system.
   */
  @Deprecated(forRemoval = true, since = "6.3.0.0")
  @Override
  public Component renderHeaderSnapshot(final SignRenderSnapshot snapshot, final ProxiedLocale locale) {

    final String key = snapshot.inventoryAvailable()
                       ? "signs.header-available"
                       : "signs.header-unavailable";

    return plugin.text().of(key, snapshot.ownerName()).forLocale(locale.getLocale());
  }

  /**
   * Renders the trading section of a shop sign snapshot into a component based on the provided
   * locale.
   *
   * @param snapshot the snapshot representing the current state of the shop sign's trading section
   *                 to be rendered
   * @param locale   the locale to be used for rendering the trading component
   *
   * @return a component representing the rendered trading section of the shop sign snapshot
   *
   * @deprecated Individual render methods replaced my the render component system.
   */
  @Deprecated(forRemoval = true, since = "6.3.0.0")
  @Override
  public Component renderTradingSnapshot(final SignRenderSnapshot snapshot, final ProxiedLocale locale) {

    final String baseKey = snapshot.stackingShop() ? snapshot.stackTradingKey() : snapshot.tradingKey();
    final String finalKey = snapshot.overrideShopTypeText() ? snapshot.shopStateKey() : baseKey;

    return switch (snapshot.remainingStock()) {
      case -1 -> plugin.text()
              .of(finalKey, plugin.text().of("signs.unlimited").forLocale(locale.getLocale()))
              .forLocale(locale.getLocale());

      case 0 -> snapshot.overrideShopTypeText()
                ? plugin.text().of(snapshot.shopStateKey()).forLocale(locale.getLocale())
                : plugin.text().of(snapshot.outOfStockKey()).forLocale(locale.getLocale());

      default -> plugin.text()
              .of(finalKey, Component.text(snapshot.remainingStock()))
              .forLocale(locale.getLocale());
    };
  }

  /**
   * Renders the price section of a shop sign snapshot into a component based on the provided
   * locale.
   *
   * @param snapshot the snapshot representing the current state of the shop sign to be rendered
   * @param locale   the locale to be used for rendering the price component
   *
   * @return a component representing the rendered price section of the shop sign snapshot
   *
   * @deprecated Individual render methods replaced my the render component system.
   */
  @Deprecated(forRemoval = true, since = "6.3.0.0")
  @Override
  public Component renderPriceSnapshot(final SignRenderSnapshot snapshot, final ProxiedLocale locale) {
    
    if (snapshot.stackingShop()) {
      return plugin.text()
              .of("signs.stack-price", snapshot.formattedPrice(), snapshot.itemAmount(), snapshot.itemName())
              .forLocale(locale.getLocale());
    }

    return plugin.text().of("signs.price", snapshot.formattedPrice()).forLocale(locale.getLocale());
  }

  /**
   * Renders the layout of the specified shop into a list of components based on the provided locale.
   *
   * @param shop the shop instance for which the components are to be rendered
   * @param locale the locale to be used for rendering the components
   * @return a LinkedList of components representing the visual layout of the shop
   */
  @Override
  public LinkedList<Component> render(@NotNull final Shop shop, @NotNull final ProxiedLocale locale) {

    final LinkedList<String> template = layoutTemplate(shop);

    final LinkedList<Component> renderedLines = new LinkedList<>();

    final ItemStack shopItem = shop.getItem();
    for (int i = 0; i < 4; i++) {

      if(template.size() <= i || template.get(i).isBlank()) {
        renderedLines.add(Component.empty());
        continue;
      }

      final String line = template.get(i);

      boolean isFullLine = false;
      for (RenderComponent component : fullLineRenderComponents()) {

        if (!component.supportsSnapshot()) {
          continue;
        }

        if (!component.appliesTo(line)) {
          continue;
        }

        renderedLines.add(component.render(shop, shopItem, locale));
        isFullLine = true;
        break;

      }

      if (isFullLine) {
        continue;
      }

      Component lineComponent = MiniMessage.miniMessage().deserialize(line);
      for (RenderComponent component : nonFullLineRenderComponents()) {

        if (!component.supportsSnapshot()) {
          continue;
        }

        if (!component.appliesTo(line)) {
          continue;
        }

        lineComponent = lineComponent.replaceText(builder -> builder
                .matchLiteral(component.placeholder())
                .replacement(component.render(shop, shopItem, locale)));
      }
      renderedLines.add(lineComponent);
    }
    return renderedLines;
  }

  /**
   * Renders the header section of the specified shop into a component.
   *
   * @param shop   the shop instance for which the header component is to be rendered
   * @param locale the locale to be used for rendering the header component
   *
   * @return a component representing the visual header of the shop
   *
   * @deprecated Individual render methods replaced my the render component system.
   */
  @Deprecated(forRemoval = true, since = "6.3.0.0")
  @Override
  public Component renderHeader(final @NotNull Shop shop, final @NotNull ProxiedLocale locale) {

    final String headerKey = shop.inventoryAvailable()? "signs.header-available" : "signs.header-unavailable";

    return plugin.text().of(headerKey, shop.ownerName(false, locale)).forLocale(locale.getLocale());
  }

  /**
   * Renders the trading section of the specified shop into a component.
   *
   * @param shop   the shop instance for which the trading component is to be rendered
   * @param locale the locale to be used for rendering the trading component
   *
   * @return a component representing the visual trading section of the shop
   *
   * @deprecated Individual render methods replaced my the render component system.
   */
  @Deprecated(forRemoval = true, since = "6.3.0.0")
  @Override
  public Component renderTrading(final @NotNull Shop shop, final @NotNull ProxiedLocale locale) {

    final String tradingStringKey = (shop.isStackingShop()? shop.shopType().stackTradingTranslationKey() : shop.shopType().tradingTranslationKey());
    final String noRemainingStringKey = shop.shopType().outOfStockTranslationKey();
    final int shopRemaining = shop.shopType().remainingStock(shop);

    final String finalTradingStringKey = (shop.shopState().overrideShopTypeText())? shop.shopState().translationKey() : tradingStringKey;

    final Component trading = switch(shopRemaining) {
      //Unlimited
      case -1 -> plugin.text().of(finalTradingStringKey, plugin.text().of("signs.unlimited").forLocale(locale.getLocale())).forLocale(locale.getLocale());
      //No remaining
      case 0 -> {
        if(shop.shopState().overrideShopTypeText()) {
          yield plugin.text().of(shop.shopState().translationKey()).forLocale(locale.getLocale());
        }
        yield plugin.text().of(noRemainingStringKey).forLocale(locale.getLocale());
      }
      //Has remaining
      default -> plugin.text().of(finalTradingStringKey, Component.text(shopRemaining)).forLocale(locale.getLocale());
    };
    return trading;
  }

  /**
   * Renders an item section of the specified shop into a component based on the provided locale.
   *
   * @param shop   the shop instance for which the item component is to be rendered
   * @param locale the locale to be used for rendering the item component
   *
   * @return a component representing the visual item section of the shop
   *
   * @deprecated Individual render methods replaced my the render component system.
   */
  @Deprecated(forRemoval = true, since = "6.3.0.0")
  @Override
  public Component renderItem(final @NotNull Shop shop, final @NotNull ProxiedLocale locale) {

    if(plugin.getConfig().getBoolean("shop.force-use-item-original-name")
       || !shop.getItem().hasItemMeta() || !shop.getItem().getItemMeta().hasDisplayName()) {

      final Component left = plugin.text().of("signs.item-left").forLocale(locale.getLocale());
      final Component right = plugin.text().of("signs.item-right").forLocale(locale.getLocale());
      final Component itemName = Util.getItemStackName(shop.getItem(), locale.getLocale());

      return left.append(itemName).append(right);
    }

    return plugin.text().of("signs.item-left").forLocale(locale.getLocale())
            .append(Util.getItemStackName(shop.getItem(), locale.getLocale())
                            .append(plugin.text().of("signs.item-right").forLocale(locale.getLocale())));
  }

  /**
   * Renders the price section of the specified shop into a component based on the provided locale.
   *
   * @param shop   the shop instance for which the price component is to be rendered
   * @param locale the locale to be used for rendering the price component
   *
   * @return a component representing the visual price section of the shop
   *
   * @deprecated Individual render methods replaced my the render component system.
   */
  @Deprecated(forRemoval = true, since = "6.3.0.0")
  @Override
  public Component renderPrice(final @NotNull Shop shop, final @NotNull ProxiedLocale locale) {

    if(shop.isStackingShop()) {

      return plugin.text().of("signs.stack-price",
                               plugin.getShopManager().format(shop.getPrice(), shop),
                               shop.getItem().getAmount(),
                               Util.getItemStackName(shop.getItem())).forLocale(locale.getLocale());
    }

    return plugin.text().of("signs.price", plugin.getShopManager().format(shop.getPrice(), shop)).forLocale(locale.getLocale());
  }

  /**
   * Renders the enchantment level for books, or the duration for potions into a component based on
   * the provided locale.
   *
   * @param shop   the shop instance for which the level component is to be rendered
   * @param locale the locale to be used for rendering the level component
   *
   * @return a component representing the visual level section of the shop
   *
   * @deprecated Individual render methods replaced my the render component system.
   */
  @Deprecated(forRemoval = true, since = "6.3.0.0")
  @Override
  public Component renderLevels(final @NotNull Shop shop, final @NotNull ProxiedLocale locale) {

    final ItemStack clone = shop.getItem().clone();
    final PotionEffect effect = Util.getFirstPotionEffect(clone);
    if(effect != null) {

      return plugin.text().of("signs.item-duration", Util.getPotionDuration(effect)).forLocale(locale.getLocale());
    }

    final Map.Entry<Enchantment, Integer> enchantment = Util.getFirstEnchantment(clone);
    if(enchantment != null) {

      return plugin.text().of("signs.item-level", enchantment.getValue()).forLocale(locale.getLocale());
    }

    //TODO: Parser API? Tying rendering to specific parts, or adding custom rendering logic
    if(clone.getItemMeta() instanceof final FireworkMeta fireworkMeta) {

      return plugin.text().of("signs.item-duration", fireworkMeta.getPower()).forLocale(locale.getLocale());
    }

    return Component.empty();
  }
}