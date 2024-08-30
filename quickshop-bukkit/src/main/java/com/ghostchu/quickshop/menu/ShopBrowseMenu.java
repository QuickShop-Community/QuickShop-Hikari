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
import com.ghostchu.quickshop.menu.browse.MainPage;
import net.tnemc.menu.core.Menu;
import net.tnemc.menu.core.Page;

/**
 * ShopBrowseMenu
 *
 * @author creatorfromhell
 * @since 6.2.0.8
 */
public class ShopBrowseMenu extends Menu {

  public static final String SHOPS_DATA = "BROWSE_SHOPS";
  public static final String SHOPS_PAGE = "BROWSE_PAGE";

  public ShopBrowseMenu() {

    this.rows = 6;
    this.name = "qs:browse";

    setOpen((open)->open.getMenu().setTitle(QuickShop.getInstance().text().of(open.getPlayer().identifier(), "gui.browse.title").legacy()));

    final Page main = new Page(1);
    final MainPage mainPageOpen = new MainPage(this.name, this.name, 1, 1, SHOPS_PAGE, this.rows, "gui.browse.info");
    main.setOpen(mainPageOpen::handle);
    addPage(main);
  }
}