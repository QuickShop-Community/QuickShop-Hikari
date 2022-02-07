/*
 * This file is a part of project QuickShop, the name is ShopRemoveLog.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.util.logging.container;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.maxgamer.quickshop.api.shop.ShopInfoStorage;

import java.util.UUID;

@AllArgsConstructor
@Data
public class ShopRemoveLog {
    private static int v = 1;
    private UUID player;
    private String reason;
    private ShopInfoStorage shop;
}
