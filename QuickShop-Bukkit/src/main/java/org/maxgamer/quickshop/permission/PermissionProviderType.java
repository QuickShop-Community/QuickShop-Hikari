/*
 * This file is a part of project QuickShop, the name is PermissionProviderType.java
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

package org.maxgamer.quickshop.permission;

public enum PermissionProviderType {
    // BUKKIT(0), VAULT(1), LUCKPERMS(2), PERMISSIONEX(3), GROUPMANAGER(4);
    BUKKIT(0);

    final int id;

    PermissionProviderType(int id) {
        this.id = id;
    }

    public static PermissionProviderType fromID(int id) throws IllegalArgumentException {
        for (PermissionProviderType child : PermissionProviderType.values()) {
            if (child.toID() == id) {
                return child;
            }
        }
        throw new IllegalArgumentException("Type not exists");
    }

    public int toID() {
        return this.id;
    }
}
