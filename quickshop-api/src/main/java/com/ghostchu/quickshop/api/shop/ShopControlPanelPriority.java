/*
 *  This file is a part of project QuickShop, the name is ShopControlPanelPriority.java
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

package com.ghostchu.quickshop.api.shop;

/**
 * ShopControlPanel showcase priority
 */
public enum ShopControlPanelPriority {
    LOWEST(16),
    LOW(32),
    NORMAL(64),
    HIGH(96),
    HIGHEST(128);

    private final int priority;

    ShopControlPanelPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Gets the priority number of the control panel
     *
     * @return priority
     */
    public int getPriority() {
        return priority;
    }
}
