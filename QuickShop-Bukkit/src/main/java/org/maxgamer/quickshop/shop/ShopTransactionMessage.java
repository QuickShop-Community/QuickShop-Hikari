/*
 * This file is a part of project QuickShop, the name is ShopTransactionMessage.java
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

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.localization.LocalizedMessagePair;


public interface ShopTransactionMessage {
    /**
     * Get the version of ShopTransactionMessage
     *
     * @return the version
     */
    int getVersion();

    @AllArgsConstructor
    @Getter
    class V1 implements ShopTransactionMessage {
        @NotNull
        private final String message;

        @Override
        public int getVersion() {
            return 1;
        }
    }

    @AllArgsConstructor
    @Getter
    class V2 implements ShopTransactionMessage {
        @NotNull
        private final String message;
        @Nullable
        private final String hoverItem;
        @Nullable
        private final String hoverText;

        @Override
        public int getVersion() {
            return 2;
        }
    }

    @AllArgsConstructor
    @Getter
    class V3 implements ShopTransactionMessage {
        @NotNull
        private final LocalizedMessagePair message;
        @Nullable
        private final String hoverItem;
        @Nullable
        private final LocalizedMessagePair hoverText;

        @Override
        public int getVersion() {
            return 3;
        }
    }
}
