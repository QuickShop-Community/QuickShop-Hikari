package com.ghostchu.quickshop.menu;
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

import com.ghostchu.quickshop.QuickShop;
import net.tnemc.menu.core.Menu;

/**
 * ShopHistoryMenu
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class ShopHistoryMenu extends Menu {

  public ShopHistoryMenu() {

    this.rows = 4;
    this.name = "qs:history";

    setOpen((open)->open.getMenu().setTitle(QuickShop.getInstance().text().of(open.getPlayer().identifier(), "history.shop.gui-title").legacy()));
  }
}