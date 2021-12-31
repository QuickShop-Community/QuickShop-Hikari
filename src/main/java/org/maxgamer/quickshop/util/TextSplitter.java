/*
 * This file is a part of project QuickShop, the name is TextSplitter.java
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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TextSplitter {
    private final static String HEADER = "!-!-!=-=-=-=-=-=";
    private final static String FOOTER = "=-=-=-=-=-=!-!-!";
    @NotNull
    public static String bakeComponent(BaseComponent[] components) {
        return HEADER +
                Base64.getEncoder().encodeToString(ComponentSerializer.toString(components).getBytes(StandardCharsets.UTF_8)) +
                FOOTER;
    }

    @SneakyThrows
    @Nullable
    public static SpilledString deBakeItem(String src) {
        if (!src.contains(HEADER)) {
           // Util.debugLog(src + " seems not a baked message");
            return null;
        }
        String base64 = StringUtils.substringBetween(src, HEADER, FOOTER);
        BaseComponent[] components = ComponentSerializer.parse(new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8));
        String left = StringUtils.substringBefore(src, HEADER);
        String right = StringUtils.substringAfter(src, FOOTER);
        return new SpilledString(left, right, components);
    }

    @AllArgsConstructor
    @Data
    public static class SpilledString {
        private String left;
        private String right;
        private BaseComponent[] components;
    }
}
