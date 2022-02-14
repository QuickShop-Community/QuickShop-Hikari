/*
 *  This file is a part of project QuickShop, the name is MatcherWorkMode.java
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

package com.ghostchu.quickshop.shop;

import org.jetbrains.annotations.NotNull;

public enum MatcherWorkMode {
    QSMATCHER(0),
    BUKKITMATCHER(1),
    JAVAMATCHER(2);

    private final int id;

    MatcherWorkMode(int id) {
        this.id = id;
    }

    public static @NotNull MatcherWorkMode fromID(int id) {
        for (MatcherWorkMode workMode : MatcherWorkMode.values()) {
            if (workMode.id == id) {
                return workMode;
            }
        }
        return QSMATCHER;
    }

    public static int toID(@NotNull MatcherWorkMode workMode) {
        return workMode.id;
    }

    public int toID() {
        return id;
    }

    public int getId() {
        return id;
    }
}
