/*
 *  This file is a part of project QuickShop, the name is QSHandleChatEvent.java
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

package com.ghostchu.quickshop.api.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.entity.Player;

/**
 * QuickShop chat handling event
 *
 * @author Ghost_chu
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
public class QSHandleChatEvent extends AbstractQSEvent {
    private final Player sender;
    private String message;

    /**
     * Getting the chat sender
     *
     * @return The chat sender
     */
    public Player getSender() {
        return sender;
    }

    /**
     * Getting the player chat content
     *
     * @return The chat content
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the new player chat content that pass to the QuickShop
     *
     * @param message The new chat content
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
