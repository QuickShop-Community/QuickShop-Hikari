/*
 *  This file is a part of project QuickShop, the name is ReflServerStateProvider.java
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

package com.ghostchu.quickshop.platform.spigot;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
/**
 * @author EssentialsX
 * <a href="https://github.com/EssentialsX/Essentials/blob/2.x/providers/NMSReflectionProvider/src/main/java/net/ess3/nms/refl/providers/ReflServerStateProvider.java">https://github.com/EssentialsX/Essentials/blob/2.x/providers/NMSReflectionProvider/src/main/java/net/ess3/nms/refl/providers/ReflServerStateProvider.java</a>
 */
public class ReflServerStateProvider {
    private final Object nmsServer;
    private final MethodHandle nmsIsRunning;

    public ReflServerStateProvider() {
        Object serverObject = null;
        MethodHandle isRunning = null;
        final Class<?> nmsClass = ReflUtil.getNMSClass("MinecraftServer");
        try {
            serverObject = nmsClass.getMethod("getServer").invoke(null);
            isRunning = MethodHandles.lookup().findVirtual(nmsClass,
                    ReflUtil.getNmsVersionObject().isHigherThanOrEqualTo(ReflUtil.V1_18_R1) ? "v" : "isRunning", //TODO jmp said he may make this better
                    MethodType.methodType(boolean.class));
        } catch (final Exception e) {
            e.printStackTrace();
        }
        nmsServer = serverObject;
        nmsIsRunning = isRunning;
    }

    public boolean isStopping() {
        if (nmsServer != null && nmsIsRunning != null) {
            try {
                return !(boolean) nmsIsRunning.invoke(nmsServer);
            } catch (final Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        return false;
    }

}
