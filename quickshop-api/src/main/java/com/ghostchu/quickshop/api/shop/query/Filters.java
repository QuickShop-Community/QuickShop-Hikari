package com.ghostchu.quickshop.api.shop.query;

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

import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.IShopType;
import com.ghostchu.quickshop.api.shop.query.filters.MaterialFilter;
import com.ghostchu.quickshop.api.shop.query.filters.OwnerUUIDFilter;
import com.ghostchu.quickshop.api.shop.query.filters.QUserOwnerFilter;
import com.ghostchu.quickshop.api.shop.query.filters.StateFilter;
import com.ghostchu.quickshop.api.shop.query.filters.TypeFilter;
import com.ghostchu.quickshop.api.shop.query.filters.WorldUUIDFilter;
import com.ghostchu.quickshop.api.shop.state.ShopState;
import org.bukkit.Material;

import java.util.UUID;

/**
 * Filters
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class Filters {

  public static final Filter<Material> ITEM_TYPE = new MaterialFilter();
  public static final Filter<UUID> OWNER_UUID = new OwnerUUIDFilter();
  public static final Filter<QUser> OWNER_QUSER = new QUserOwnerFilter();
  public static final Filter<ShopState> STATE = new StateFilter();
  public static final Filter<IShopType> TYPE = new TypeFilter();
  public static final Filter<UUID> WORLD_UUID = new WorldUUIDFilter();
}