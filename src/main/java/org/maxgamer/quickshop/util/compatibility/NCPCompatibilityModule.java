/*
 * This file is a part of project QuickShop, the name is NCPCompatibilityModule.java
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

package org.maxgamer.quickshop.util.compatibility;

import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.compatibility.AbstractQSCompatibilityModule;
import org.maxgamer.quickshop.util.Util;


public class NCPCompatibilityModule extends AbstractQSCompatibilityModule {

    public NCPCompatibilityModule(QuickShop plugin) {
        super(plugin);
    }

    /**
     * Gets the CompatibilityModule provider name
     *
     * @return Provider name
     */
    @Override
    public @NotNull String getName() {
        return "NoCheatPlus";
    }


    /**
     * Calls CompatibilityModule to toggle the detection status for playerb between on and off
     *
     * @param player The player
     * @param status On or Off
     */
    @Override
    public void toggle(@NotNull Player player, boolean status) {
        if (status) {
            Util.debugLog(
                    "Calling NoCheatPlus continue follow " + player.getName() + " cheats detection.");
            NCPExemptionManager.unexempt(player);
        } else {
            Util.debugLog(
                    "Calling NoCheatPlus ignore "
                            + player.getName()
                            + " cheats detection until we finished permission checks.");

            NCPExemptionManager.exemptPermanently(player);
        }
    }
}
