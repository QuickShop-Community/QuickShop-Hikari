/*
 * This file is a part of project QuickShop, the name is ShopTransactionMessageTest.java
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

package org.maxgamer.quickshop.shop;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.maxgamer.quickshop.TestBukkitBase;
import org.maxgamer.quickshop.localization.LocalizedMessagePair;

public class ShopTransactionMessageTest extends TestBukkitBase {
    @Test
    public void testV1PlainMessage() {
        ShopTransactionMessageContainer container = ShopTransactionMessageContainer.fromJson("233333");
        container = ShopTransactionMessageContainer.fromJson(container.toJson());
        Assertions.assertEquals("233333", container.getMessage(null));
        Assertions.assertNull(container.getHoverItemStr());
        Assertions.assertNull(container.getHoverText(null));
    }

    @Test
    public void testV2PlainMessage() {
        ShopTransactionMessageContainer container = ShopTransactionMessageContainer.fromJson(ShopTransactionMessageContainer.fromJson("{\"message\":\"233333\",\"hoverItem\":\"item:\\n  \\u003d\\u003d: org.bukkit.inventory.ItemStack\\n  v: 2730\\n  type: JUNGLE_LOG\\n\",\"hoverText\":\"233333\"}").toJson());
        Assertions.assertEquals("233333", container.getMessage(null));
        Assertions.assertEquals("item:\n  \u003d\u003d: org.bukkit.inventory.ItemStack\n  v: 2730\n  type: JUNGLE_LOG\n", container.getHoverItemStr());
        Assertions.assertEquals("233333", container.getHoverText(null));
        container = ShopTransactionMessageContainer.fromJson(ShopTransactionMessageContainer.fromJson("{\"message\":\"233333\",\"hoverItem\":\"item:\\n  \\u003d\\u003d: org.bukkit.inventory.ItemStack\\n  v: 2730\\n  type: JUNGLE_LOG\\n\"}").toJson());
        Assertions.assertEquals("233333", container.getMessage(null));
        Assertions.assertEquals("item:\n  \u003d\u003d: org.bukkit.inventory.ItemStack\n  v: 2730\n  type: JUNGLE_LOG\n", container.getHoverItemStr());
        Assertions.assertNull(container.getHoverText(null));
        container = ShopTransactionMessageContainer.fromJson(ShopTransactionMessageContainer.fromJson("{\"message\":\"233333\"}").toJson());
        Assertions.assertEquals("233333", container.getMessage(null));
        Assertions.assertNull(container.getHoverItemStr());
        Assertions.assertNull(container.getHoverText(null));
    }

    @Test
    public void testModernMessage() {
        //V1
        ShopTransactionMessageContainer container = ShopTransactionMessageContainer.fromJson(ShopTransactionMessageContainer.ofPlainStr("ghost_chu is an lucky guy").toJson());
        Assertions.assertEquals("ghost_chu is an lucky guy", container.getMessage(null));
        Assertions.assertNull(container.getHoverItemStr());
        Assertions.assertNull(container.getHoverText(null));
        //V2
        container = ShopTransactionMessageContainer.fromJson(ShopTransactionMessageContainer.ofStrWithItem("ghost_chu", null, null).toJson());
        Assertions.assertEquals("ghost_chu", container.getMessage(null));
        Assertions.assertNull(container.getHoverItemStr());
        Assertions.assertNull(container.getHoverText(null));
        container = ShopTransactionMessageContainer.fromJson(ShopTransactionMessageContainer.ofStrWithItem("ghost_chu", "is", null).toJson());
        Assertions.assertEquals("ghost_chu", container.getMessage(null));
        Assertions.assertEquals("is", container.getHoverItemStr());
        Assertions.assertNull(container.getHoverText(null));
        container = ShopTransactionMessageContainer.fromJson(ShopTransactionMessageContainer.ofStrWithItem("ghost_chu", "is", "an lucky guy").toJson());
        Assertions.assertEquals("ghost_chu", container.getMessage(null));
        Assertions.assertEquals("is", container.getHoverItemStr());
        Assertions.assertEquals("an lucky guy", container.getHoverText(null));
        //V3
        container = ShopTransactionMessageContainer.fromJson(ShopTransactionMessageContainer.ofLocalizedMessageWithItem(LocalizedMessagePair.of("nearby-shop-this-way", "ghost_chu"), "is", LocalizedMessagePair.of("nearby-shop-this-way", "ghost_chu")).toJson());
        Assertions.assertTrue(container.getMessage(null).contains("ghost_chu"));
        Assertions.assertEquals("is", container.getHoverItemStr());
        Assertions.assertTrue(container.getHoverText(null).contains("ghost_chu"));
        container = ShopTransactionMessageContainer.fromJson(ShopTransactionMessageContainer.ofLocalizedMessageWithItem(LocalizedMessagePair.of("nearby-shop-this-way", "ghost_chu"), "is", null).toJson());
        Assertions.assertTrue(container.getMessage(null).contains("ghost_chu"));
        Assertions.assertEquals("is", container.getHoverItemStr());
        Assertions.assertNull(container.getHoverText(null));
        container = ShopTransactionMessageContainer.fromJson(ShopTransactionMessageContainer.ofLocalizedMessageWithItem(LocalizedMessagePair.of("nearby-shop-this-way", "ghost_chu"), null, null).toJson());
        Assertions.assertTrue(container.getMessage(null).contains("ghost_chu"));
        Assertions.assertNull(container.getHoverItemStr());
        Assertions.assertNull(container.getHoverText(null));
    }
}
