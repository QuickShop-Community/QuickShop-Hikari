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
import net.tnemc.menu.core.viewer.CoreStatus;

/**
 * PageSwitchWithCloseAction
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class PageSwitchWithCloseAction extends IconAction {

  private final String menu;
  private final int page;

  public PageSwitchWithCloseAction(String menu, int page) {

    super(ActionType.ANY);
    this.menu = menu;
    this.page = page;
  }

  public PageSwitchWithCloseAction(String menu, int page, ActionType type) {

    super(type);
    this.menu = menu;
    this.page = page;
  }

  public boolean onClick(MenuClickHandler handler) {

    if(this.page == -1) {
      handler.player().inventory().close();
      return true;
    } else {
      handler.player().status(CoreStatus.SWITCHING);
      handler.player().inventory().openMenu(handler.player(), this.menu, this.page);
      return true;
    }
  }
}