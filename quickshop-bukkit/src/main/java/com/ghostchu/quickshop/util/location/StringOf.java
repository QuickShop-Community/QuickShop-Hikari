/*
 *  This file is a part of project QuickShop, the name is StringOf.java
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

package com.ghostchu.quickshop.util.location;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class StringOf {

    @NotNull
    private final Location location;

    @NotNull
    private final World world;

    public StringOf(@NotNull Location location) {
        if (location.getWorld() == null) {
            throw new IllegalStateException("World of the location cannot be null!");
        }

        this.location = location;
        this.world = location.getWorld();
    }

    @NotNull
    public String asKey() {
        return asString().replaceAll(":", "/").replaceAll("\\.", "_");
    }

    @NotNull
    public String asString() {
        String s = world.getName() + ":";

        s +=
                String.format(
                        Locale.ENGLISH, "%.2f,%.2f,%.2f", location.getX(), location.getY(), location.getZ());

        if (location.getYaw() != 0f || location.getPitch() != 0f) {
            s += String.format(Locale.ENGLISH, ":%.2f:%.2f", location.getYaw(), location.getPitch());
        }
        return s;
    }

}
