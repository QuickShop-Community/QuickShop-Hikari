/*
 * This file is a part of project QuickShop, the name is QSCompatibilityModule.java
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

package org.maxgamer.quickshop.api.compatibility;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.holder.QuickShopInstanceHolder;

public abstract class AbstractQSCompatibilityModule extends QuickShopInstanceHolder implements CompatibilityModule {
    public AbstractQSCompatibilityModule(QuickShop plugin) {
        super(plugin);
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return plugin;
    }
}
