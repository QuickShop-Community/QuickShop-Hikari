/*
 * This file is a part of project QuickShop, the name is InteractUtilTest.java
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

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InteractUtilTest {


    private ConfigurationSection genConfig(int mode, boolean allowSneaking) {
        ConfigurationSection configurationSection = new MemoryConfiguration();
        configurationSection.set("shop.interact.interact-mode", mode);
        configurationSection.set("shop.interact.sneak-to-create", allowSneaking);
        return configurationSection.getConfigurationSection("shop.interact");
    }

    @Test
    public void testInteractBoolean() {
        //ONLY
        InteractUtil.init(genConfig(0, true));
        Assertions.assertTrue(InteractUtil.check(InteractUtil.Action.CREATE, true));
        Assertions.assertFalse(InteractUtil.check(InteractUtil.Action.CREATE, false));
        InteractUtil.init(genConfig(0, false));
        Assertions.assertFalse(InteractUtil.check(InteractUtil.Action.CREATE, true));
        Assertions.assertTrue(InteractUtil.check(InteractUtil.Action.CREATE, false));
        //BOTH
        InteractUtil.init(genConfig(1, false));
        Assertions.assertFalse(InteractUtil.check(InteractUtil.Action.CREATE, true));
        Assertions.assertTrue(InteractUtil.check(InteractUtil.Action.CREATE, false));
        InteractUtil.init(genConfig(1, true));
        Assertions.assertTrue(InteractUtil.check(InteractUtil.Action.CREATE, true));
        Assertions.assertTrue(InteractUtil.check(InteractUtil.Action.CREATE, false));
        //REVERSED
        InteractUtil.init(genConfig(2, false));
        Assertions.assertTrue(InteractUtil.check(InteractUtil.Action.CREATE, true));
        Assertions.assertTrue(InteractUtil.check(InteractUtil.Action.CREATE, false));
        InteractUtil.init(genConfig(2, true));
        Assertions.assertFalse(InteractUtil.check(InteractUtil.Action.CREATE, true));
        Assertions.assertTrue(InteractUtil.check(InteractUtil.Action.CREATE, false));

    }
}
