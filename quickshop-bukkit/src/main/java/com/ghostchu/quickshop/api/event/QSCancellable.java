/*
 *  This file is a part of project QuickShop, the name is QSCancellable.java
 *  Copyright (C) Ghost_chu and contributors
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

package com.ghostchu.quickshop.api.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.Nullable;

/**
 * Parent class for all cancellable events.
 */
public interface QSCancellable extends Cancellable {
    @Override
    @Deprecated
    default void setCancelled(boolean cancel){
        setCancelled(cancel, (Component) null);
    }

    default void setCancelled(boolean cancel, @Nullable String reason){
        if(reason == null){
            setCancelled(cancel,(Component) null);
            return;
        }
        setCancelled(cancel, LegacyComponentSerializer.legacySection().deserialize(reason));
    }

    void setCancelled(boolean cancel, @Nullable Component reason);

    @Nullable
    Component getCancelReason();
}
