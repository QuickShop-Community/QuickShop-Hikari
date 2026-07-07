package com.ghostchu.quickshop.api.shop;

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

import com.ghostchu.quickshop.api.localization.text.ProxiedLocale;
import com.ghostchu.quickshop.api.shop.layout.ConditionalRenderComponent;
import com.ghostchu.quickshop.api.shop.layout.ItemComponent;
import com.ghostchu.quickshop.api.shop.layout.RenderComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * IShopLayoutProvider
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public interface IShopLayoutProvider {

  /**
   * Provides a list of {@link ItemComponent} objects, each representing a component associated
   * with an item in the context of a shop layout.
   *
   * @return a list of {@link ItemComponent} objects representing the item components.
   */
  List<ItemComponent> itemComponents();

  /**
   * A list of components that represent various aspects of a text-display layout or sign.
   *
   * @return a list of {@link RenderComponent} objects representing the render components
   * @since 6.3.0.0
   */
  List<RenderComponent> renderComponents();

  /**
   * Filters the list of render components to include only those that are full line replacements.
   *
   * @return a list of {@link RenderComponent} objects representing components that replace entire lines
   * @since 6.3.0.0
   */
  default List<RenderComponent> fullLineRenderComponents() {

    return renderComponents().stream().filter(RenderComponent::fullLine).toList();
  }

  /**
   * Filters the list of render components to include only those that are not full line replacements.
   *
   * @return a list of {@link RenderComponent} objects representing components that do not replace entire lines
   * @since 6.3.0.0
   */
  default List<RenderComponent> inlineRenderComponents() {
    return renderComponents().stream().filter(component -> !component.fullLine()).toList();
  }

  /**
   * Generates a layout template for the specified shop.
   *
   * @param shop the shop instance for which the layout template is to be generated
   * @return a LinkedList of Strings representing the layout template of the shop
   */
  LinkedList<String> layoutTemplate(final Shop shop);

  /**
   * Creates a snapshot of a shop sign that encapsulates the visual and structural data required
   * to render the sign, based on the specified shop and locale.
   *
   * @param shop the shop instance for which the sign snapshot is to be created
   * @param locale the locale to be used for formatting and rendering the snapshot
   * @return a CompletableFuture that completes with a SignRenderSnapshot, representing
   *         the state of the shop sign
   */
  CompletableFuture<SignRenderSnapshot> createSignSnapshot(@NotNull Shop shop, @NotNull final ProxiedLocale locale);

  /**
   * Renders a snapshot of a shop sign into a list of components based on the provided locale.
   *
   * @param snapshot the snapshot representing the current state of the shop sign to be rendered
   * @param locale the locale to be used for rendering the components
   * @return a list of components representing the rendered shop sign snapshot
   */
  default List<Component> renderSnapshot(final SignRenderSnapshot snapshot, final ProxiedLocale locale) {

    final List<Component> rendered = new ArrayList<>(4);

    for (int i = 0; i < 4; i++) {
      if (snapshot.template().size() <= i || snapshot.template().get(i).isBlank()) {
        rendered.add(Component.empty());
        continue;
      }

      final String line = snapshot.template().get(i);

      boolean isFullLine = false;
      for (final RenderComponent component : fullLineRenderComponents()) {

        if (!component.supportsSnapshot()) {
          continue;
        }

        if (!component.appliesTo(line)) {
          continue;
        }

        rendered.add(component.renderSnapshot(snapshot, locale));
        isFullLine = true;
        break;

      }

      if (isFullLine) {
        continue;
      }

      Component lineComponent = MiniMessage.miniMessage().deserialize(line);
      for (final RenderComponent component : inlineRenderComponents()) {

        if (!component.supportsSnapshot()) {
          continue;
        }

        if (!component.appliesTo(line)) {
          continue;
        }

        if (component instanceof final ConditionalRenderComponent conditionalComponent && conditionalComponent.isFullLine(snapshot)) {
          lineComponent = conditionalComponent.renderSnapshot(snapshot, locale);
          break;
        }

        lineComponent = lineComponent.replaceText(builder -> builder
                .matchLiteral(component.placeholder())
                .replacement(component.renderSnapshot(snapshot, locale)));
      }
      rendered.add(lineComponent);
    }

    return rendered;
  }

  /**
   * Renders the header section of a shop sign snapshot into a component based on the provided locale.
   *
   * @param snapshot the snapshot representing the current state of the shop sign's header to be rendered
   * @param locale the locale to be used for rendering the header component
   * @return a component representing the rendered header section of the shop sign snapshot
   *
   * @deprecated Individual render methods replaced my the render component system.
   */
  @Deprecated(forRemoval = true, since = "6.3.0.0")
  Component renderHeaderSnapshot(final SignRenderSnapshot snapshot, final ProxiedLocale locale);

  /**
   * Renders the trading section of a shop sign snapshot into a component based on the provided locale.
   *
   * @param snapshot the snapshot representing the current state of the shop sign's trading section to be rendered
   * @param locale the locale to be used for rendering the trading component
   * @return a component representing the rendered trading section of the shop sign snapshot
   *
   * @deprecated Individual render methods replaced my the render component system.
   */
  @Deprecated(forRemoval = true, since = "6.3.0.0")
  Component renderTradingSnapshot(final SignRenderSnapshot snapshot, final ProxiedLocale locale);

  /**
   * Renders the price section of a shop sign snapshot into a component based on the provided locale.
   *
   * @param snapshot the snapshot representing the current state of the shop sign to be rendered
   * @param locale the locale to be used for rendering the price component
   * @return a component representing the rendered price section of the shop sign snapshot
   *
   * @deprecated Individual render methods replaced my the render component system.
   */
  @Deprecated(forRemoval = true, since = "6.3.0.0")
  Component renderPriceSnapshot(final SignRenderSnapshot snapshot, final ProxiedLocale locale);

  /**
   * Renders the layout of the specified shop into a list of components based on the provided locale.
   *
   * @param shop the shop instance for which the components are to be rendered
   * @param locale the locale to be used for rendering the components
   * @return a LinkedList of components representing the visual layout of the shop
   */
  LinkedList<Component> render(@NotNull final Shop shop, @NotNull final ProxiedLocale locale);

  /**
   * Renders the header section of the specified shop into a component.
   *
   * @param shop the shop instance for which the header component is to be rendered
   * @param locale the locale to be used for rendering the header component
   * @return a component representing the visual header of the shop
   *
   * @deprecated Individual render methods replaced my the render component system.
   */
  @Deprecated(forRemoval = true, since = "6.3.0.0")
  Component renderHeader(@NotNull final Shop shop, @NotNull final ProxiedLocale locale);

  /**
   * Renders the trading section of the specified shop into a component.
   *
   * @param shop the shop instance for which the trading component is to be rendered
   * @param locale the locale to be used for rendering the trading component
   * @return a component representing the visual trading section of the shop
   *
   * @deprecated Individual render methods replaced my the render component system.
   */
  @Deprecated(forRemoval = true, since = "6.3.0.0")
  Component renderTrading(@NotNull final Shop shop, @NotNull final ProxiedLocale locale);

  /**
   * Renders an item section of the specified shop into a component based on the provided locale.
   *
   * @param shop the shop instance for which the item component is to be rendered
   * @param locale the locale to be used for rendering the item component
   * @return a component representing the visual item section of the shop
   *
   * @deprecated Individual render methods replaced my the render component system.
   */
  @Deprecated(forRemoval = true, since = "6.3.0.0")
  Component renderItem(@NotNull final Shop shop, @NotNull final ProxiedLocale locale);

  /**
   * Renders the price section of the specified shop into a component based on the provided locale.
   *
   * @param shop the shop instance for which the price component is to be rendered
   * @param locale the locale to be used for rendering the price component
   * @return a component representing the visual price section of the shop
   *
   * @deprecated Individual render methods replaced my the render component system.
   */
  @Deprecated(forRemoval = true, since = "6.3.0.0")
  Component renderPrice(@NotNull final Shop shop, @NotNull final ProxiedLocale locale);

  /**
   * Renders the enchantment level for books, or the duration for potions into a component based on the provided locale.
   *
   * @param shop the shop instance for which the level component is to be rendered
   * @param locale the locale to be used for rendering the level component
   * @return a component representing the visual level section of the shop
   *
   * @deprecated Individual render methods replaced my the render component system.
   */
  @Deprecated(forRemoval = true, since = "6.3.0.0")
  Component renderLevels(final @NotNull Shop shop, final @NotNull ProxiedLocale locale);
}