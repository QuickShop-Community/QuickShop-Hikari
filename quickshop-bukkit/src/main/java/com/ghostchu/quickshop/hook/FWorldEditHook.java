package com.ghostchu.quickshop.hook;
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

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.hook.Hook;
import com.ghostchu.quickshop.hook.fworldedit.FWorldEditAdapter;
import org.bukkit.Bukkit;

/**
 * WorldEditHook
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class FWorldEditHook implements Hook {

  private FWorldEditAdapter adapter;

  /**
   * Retrieves the identifier for the hook.
   *
   * @return a non-null, unique string representing the identifier of the hook
   */
  @Override
  public String identifier() {

    return "FastAsyncWorldEdit";
  }

  /**
   * Checks whether the hook is currently enabled.
   *
   * @return true if the hook is enabled, false otherwise
   */
  @Override
  public boolean canEnable() {

    return Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit")
           && QuickShop.getInstance().getConfig().getBoolean("plugin.WorldEdit", true);
  }

  /**
   * Enables the hook and updates its state to active.
   *
   * @return true if the hook was successfully enabled, false if the operation failed
   */
  @Override
  public boolean enable() {

    if(adapter == null) {
      adapter = new FWorldEditAdapter();
      adapter.register();
      return true;
    }

    return false;
  }

  /**
   * Disables the hook and updates its state to inactive.
   *
   * @return true if the hook was successfully disabled, false if the operation failed
   */
  @Override
  public boolean disable() {

    if(adapter != null) {

      adapter.unregister();
      adapter = null;
      return true;
    }

    return false;
  }
}