/*
 * This file is a part of project QuickShop, the name is LocationOf.java
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

package org.maxgamer.quickshop.util.location;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocationOf {

    @NotNull
    private static final Pattern PATTERN =
            Pattern.compile(
                    "((?<world>[^:/]+)[:/])?(?<x>[\\-0-9.]+),(?<y>[\\-0-9.]+),(?<z>[\\-0-9.]+)(:(?<yaw>[\\-0-9.]+):(?<pitch>[\\-0-9.]+))?");

    @NotNull
    private final String text;

    public LocationOf(@NotNull String text) {
        this.text = text;
    }

    @NotNull
    public Location value() {
        final Matcher match = PATTERN.matcher(text.replaceAll("_", "\\."));

        if (match.matches()) {
            return new Location(
                    Bukkit.getWorld(match.group("world")),
                    Double.parseDouble(match.group("x")),
                    Double.parseDouble(match.group("y")),
                    Double.parseDouble(match.group("z")),
                    match.group("yaw") != null ? Float.parseFloat(match.group("yaw")) : 0F,
                    match.group("pitch") != null ? Float.parseFloat(match.group("pitch")) : 0F);
        }

        throw new IllegalStateException("Location string has wrong style!");
    }

}
