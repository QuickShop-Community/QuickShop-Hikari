package com.ghostchu.quickshop.api.shop.layout;

/*
 * QuickShop-Hikari
 * Copyright (C) 2026 Daniel "creatorfromhell" Vidmar
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

import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.SignRenderSnapshot;

/**
 * ConditionalRenderComponent
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public interface ConditionalRenderComponent extends RenderComponent {

  /**
   * Determines if the specified shop represents a full-line replacement.
   * A full-line replacement indicates that no other rendering components
   * will be processed for the same line, and rendering will proceed to the next line.
   *
   * @param shop the shop instance to evaluate, must not be null
   * @return {@code true} if the shop is a full-line replacement, {@code false} otherwise
   */
  boolean isFullLine(final Shop shop);

  /**
   * Determines if a specific render component for a shop sign is designated as a full-line replacement.
   * If it is a full-line replacement, this indicates that no other renderers
   * will be processed on the same line, and rendering proceeds to the next line.
   *
   * @param snapshot the {@link SignRenderSnapshot} containing data about the shop's current state,
   *             must not be null
   * @return {@code true} if the render component is a full-line replacement, {@code false} otherwise
   */
  boolean isFullLine(final SignRenderSnapshot snapshot);
}
