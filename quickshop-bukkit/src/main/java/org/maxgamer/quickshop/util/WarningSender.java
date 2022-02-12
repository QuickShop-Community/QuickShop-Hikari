/*
 * This file is a part of project QuickShop, the name is WarningSender.java
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

package org.maxgamer.quickshop.util;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;

/**
 * WarningSender to prevent send too many warnings to CommandSender in short time.
 *
 * @author Ghost_chu
 */
@EqualsAndHashCode
@ToString
public class WarningSender {
    private final long cooldown;
    @ToString.Exclude
    private final QuickShop plugin;
    private long lastSend = 0;

    /**
     * Create a warning sender
     *
     * @param plugin   Main class
     * @param cooldown Time unit: ms
     */
    public WarningSender(@NotNull QuickShop plugin, long cooldown) {
        this.plugin = plugin;
        this.cooldown = cooldown;
    }

    /**
     * Send warning a warning
     *
     * @param text The text you want send/
     * @return Success sent, if it is in a cool-down, it will return false
     */
    public boolean sendWarn(String text) {
        if (System.currentTimeMillis() - lastSend > cooldown) {
            plugin.getLogger().warning(text);
            this.lastSend = System.currentTimeMillis();
            return true;
        }
        return false;
    }

}
