package com.ghostchu.quickshop.menu.shared;
/*
 * QuickShop-Hikari
 * Copyright (C) 2024 Daniel "creatorfromhell" Vidmar
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

import net.tnemc.menu.core.handlers.MenuClickHandler;
import net.tnemc.menu.core.icon.action.ActionType;
import net.tnemc.menu.core.icon.action.IconAction;
import net.tnemc.menu.core.viewer.MenuViewer;

/**
 * ClearSearchAction - Clears search filter on right-click and reopens menu
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class ClearSearchAction extends IconAction {

  private final String searchDataKey;
  private final String pageDataKey;
  private final String menuName;
  private final int menuPage;

  /**
   * Creates a ClearSearchAction for right-click
   *
   * @param searchDataKey The data key for the search query
   * @param pageDataKey   The data key for the page number
   * @param menuName      The menu name to reopen
   * @param menuPage      The menu page to reopen
   */
  public ClearSearchAction(final String searchDataKey, final String pageDataKey,
                           final String menuName, final int menuPage) {
    super(ActionType.RIGHT_CLICK);
    this.searchDataKey = searchDataKey;
    this.pageDataKey = pageDataKey;
    this.menuName = menuName;
    this.menuPage = menuPage;
  }

  @Override
  public boolean onClick(final MenuClickHandler handler) {
    // Get existing viewer and modify data directly
    final java.util.Optional<MenuViewer> viewerOpt = handler.player().viewer();
    if(viewerOpt.isPresent()) {
      final MenuViewer viewer = viewerOpt.get();
      
      // Clear search and reset page on existing viewer
      viewer.addData(searchDataKey, "");
      viewer.addData(pageDataKey, 1);
      
      // Switch to same page to refresh (this triggers page open callback)
      handler.player().inventory().openMenu(handler.player(), menuName, menuPage);
    }
    
    return true;
  }
}
