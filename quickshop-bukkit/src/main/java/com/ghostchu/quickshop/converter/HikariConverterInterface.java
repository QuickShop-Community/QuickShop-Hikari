/*
 *  This file is a part of project QuickShop, the name is HikariConverterInterface.java
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

package com.ghostchu.quickshop.converter;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.UUID;

public interface HikariConverterInterface {

    /**
     * Returns empty for ready, any elements inside will mark as not ready and will be post to users.
     *
     * @return The element about not ready.
     * @throws Exception Any exception throws will mark as unready and will show to users.
     */
    @NotNull
    List<Component> checkReady() throws Exception;

    /**
     * Start for backing up
     *
     * @param actionId Action Identifier for this upgrade operation.
     * @param folder   The target folder for backup.
     * @throws Exception Backup fails.
     */
    void backup(@NotNull UUID actionId, @NotNull File folder) throws Exception;

    /**
     * Start the migrating
     *
     * @param actionId Action Identifier for this upgrade operation.
     * @throws IllegalStateException Not ready.
     * @throws Exception             Migrate operation fails.
     */
    void migrate(@NotNull UUID actionId) throws Exception;
}
