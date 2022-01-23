/*
 * This file is a part of project QuickShop, the name is ReflectFactory.java
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

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandMap;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * ReflectFactory is library builtin QuickShop to get/execute stuff that cannot be access with BukkitAPI with reflect way.
 *
 * @author Ghost_chu
 */
public class ReflectFactory {
    private static String cachedVersion = null;
    private static Method craftItemStack_asNMSCopyMethod;
    private static Method itemStack_saveMethod;
    private static Class<?> nbtTagCompoundClass;
    private static Class<?> craftServerClass;
    private static Class<?> cachedNMSClass;
    private static String nmsVersion;
    private static Method getMinecraftKeyNameMethod;
    private static final boolean isMinecraftKeyNameMethodUnavailable = false;
//    private static Object serverInstance;
//    private static Field tpsField;

    static {
        String nmsVersion = getNMSVersion();

        try {
            craftItemStack_asNMSCopyMethod =
                    Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".inventory.CraftItemStack")
                            .getDeclaredMethod("asNMSCopy", ItemStack.class);

            GameVersion gameVersion = GameVersion.get(nmsVersion);
            if (gameVersion.isNewNmsName()) {
                // 1.17+
                nbtTagCompoundClass = Class.forName("net.minecraft.nbt.NBTTagCompound");
                List<Method> methodList = Arrays.stream(Class.forName("net.minecraft.world.item.ItemStack").getDeclaredMethods()).filter(method ->
                        {
                            Class<?> returnType = method.getReturnType();
                            Parameter[] parameters = method.getParameters();
                            //Save method sign is foo(net/minecraft/nbt/NBTTagCompound)L(net/minecraft/nbt/NBTTagCompound)
                            return !method.isSynthetic() && !method.isBridge() && parameters.length == 1 && returnType.equals(nbtTagCompoundClass) && parameters[0].getType().equals(nbtTagCompoundClass);
                        }
                ).collect(Collectors.toList());
                if (methodList.size() == 1) {
                    itemStack_saveMethod = methodList.get(0);
                } else {
                    throw new RuntimeException("Unable to find correct itemStack save method, got " + methodList + ", please report!");
                }
            } else {
                // Before 1.17
                nbtTagCompoundClass = Class.forName("net.minecraft.server." + nmsVersion + ".NBTTagCompound");
                itemStack_saveMethod = Class.forName("net.minecraft.server." + nmsVersion + ".ItemStack").getDeclaredMethod("save", nbtTagCompoundClass);
            }
            craftServerClass = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".CraftServer");

        } catch (Exception t) {
            QuickShop.getInstance().getLogger().log(Level.WARNING, "Failed to loading up net.minecraft.server support module, usually this caused by NMS changes but QuickShop not support yet, Did you have up-to-date?", t);
        }
    }

    @NotNull
    public static String getNMSVersion() {
        if (nmsVersion == null) {
            String name = Bukkit.getServer().getClass().getPackage().getName();
            nmsVersion = name.substring(name.lastIndexOf('.') + 1);
        }
        return nmsVersion;
    }

    /**
     * Get MinecraftServer's TPS
     *
     * @return TPS (e.g 19.92)
     */
    @NotNull
    public static Double getTPS() {
        return QuickShop.getInstance().getTpsWatcher().getAverageTPS();
//        if (serverInstance == null || tpsField == null) {
//            try {
//                serverInstance = getNMSClass("MinecraftServer").getMethod("getServer").invoke(null);
//                tpsField = serverInstance.getClass().getField("recentTps");
//            } catch (NoSuchFieldException
//                    | SecurityException
//                    | IllegalAccessException
//                    | IllegalArgumentException
//                    | InvocationTargetException
//                    | NoSuchMethodException e) {
//                serverInstance = null;
//                tpsField = null;
//                Util.debugLog("Failed to get TPS " + e.getMessage());
//                try {
//
//                }catch (Exception exception){
//                    return 20.0;
//                }
//            }
//        }
//        try {
//            double[] tps = ((double[]) tpsField.get(serverInstance));
//            return tps[0];
//        } catch (IllegalAccessException ignored) {
//            return 20.0;
//        }
    }

    @NotNull
    public static Class<?> getNMSClass(@Nullable String className) {
        if (cachedNMSClass != null) {
            return cachedNMSClass;
        }
        if (className == null) {
            className = "MinecraftServer";
        }
        String name = Bukkit.getServer().getClass().getPackage().getName();
        String version = name.substring(name.lastIndexOf('.') + 1);
        try {
            cachedNMSClass = Class.forName("net.minecraft.server." + version + "." + className);
            return cachedNMSClass;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static String getServerVersion() {
        if (cachedVersion != null) {
            return cachedVersion;
        }
        try {
            Field consoleField = Bukkit.getServer().getClass().getDeclaredField("console");
            // protected
            consoleField.setAccessible(true);
            // dedicated server
            Object console = consoleField.get(Bukkit.getServer());
            cachedVersion = String.valueOf(
                    console.getClass().getSuperclass().getMethod("getVersion").invoke(console));
            return cachedVersion;
        } catch (Exception e) {
            //Fallback to common substring
            String[] strings = StringUtils.substringsBetween(Bukkit.getServer().getVersion(), "(MC: ", ")");
            if (strings != null && strings.length == 1) {
                cachedVersion = strings[0];
            } else {
                cachedVersion = "Unknown";
            }
            return cachedVersion;
        }
    }

    /**
     * Save ItemStack to Json through the NMS.
     *
     * @param bStack ItemStack
     * @return The json for ItemStack.
     * @throws InvocationTargetException throws
     * @throws IllegalAccessException    throws
     * @throws NoSuchMethodException     throws
     * @throws InstantiationException    throws
     */
    @Nullable
    public static String convertBukkitItemStackToJson(@NotNull ItemStack bStack) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        if (bStack.getType() == Material.AIR || craftItemStack_asNMSCopyMethod == null || nbtTagCompoundClass == null) {
            return null;
        }
        Object mcStack = craftItemStack_asNMSCopyMethod.invoke(null, bStack);
        Object nbtTagCompound = nbtTagCompoundClass.getDeclaredConstructor().newInstance();

        itemStack_saveMethod.invoke(mcStack, nbtTagCompound);
        return nbtTagCompound.toString();
    }

    /**
     * Save ItemStack to Json through the NMS.
     *
     * @param bStack ItemStack
     * @return The json for ItemStack.
     * @throws InvocationTargetException throws
     * @throws IllegalAccessException    throws
     * @throws NoSuchMethodException     throws
     * @throws InstantiationException    throws
     */
    @Nullable
    public static Object convertBukkitItemStackToMojangItemStack(@NotNull ItemStack bStack) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        if (bStack.getType() == Material.AIR) {
            return null;
        }
        Object mcStack = craftItemStack_asNMSCopyMethod.invoke(null, bStack);
        Object nbtTagCompound = nbtTagCompoundClass.getDeclaredConstructor().newInstance();

        itemStack_saveMethod.invoke(mcStack, nbtTagCompound);
        return nbtTagCompound.toString();
    }

    public static CommandMap getCommandMap() throws NoSuchFieldException, IllegalAccessException {
        Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        commandMapField.setAccessible(true);
        return (CommandMap) commandMapField.get(Bukkit.getServer());
    }

    public static void syncCommands() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = craftServerClass.getDeclaredMethod("syncCommands");
        try {
            method.setAccessible(true);
        } catch (Exception ignored) {
        }
        method.invoke(Bukkit.getServer(), (Object[]) null);
    }

    private static Method findMethod(Class<?> targetClass, Class<?> returnType, Predicate<Method> filter, Class<?>... args) {
        FindProcess:
        for (Method method : targetClass.getMethods()) {
            if (method.isBridge() && method.isSynthetic()) {
                continue;
            }
            if (method.getReturnType() == returnType) {
                if (method.getParameterCount() == args.length) {
                    Parameter[] parameters = method.getParameters();
                    for (int i = 0; i < args.length; i++) {
                        if (parameters[i].getType() != args[i]) {
                            continue FindProcess;
                        }
                    }
                    if (filter.test(method)) {
                        return method;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    public static String getMaterialMinecraftNamespacedKey(Material material) {
        Object nmsItem;
        try {
            nmsItem = Class.forName("org.bukkit.craftbukkit." + getNMSVersion() + ".util.CraftMagicNumbers").getMethod("getItem", Material.class).invoke(null, material);

            if (nmsItem == null) {
                Util.debugLog("nmsItem null");
                return null;
            }
            try {
                getMinecraftKeyNameMethod = nmsItem.getClass().getMethod("getName");
            } catch (NoSuchMethodException exception) {
                Util.debugLog("Mapping changed during minecraft update, dynamic searching...");
                getMinecraftKeyNameMethod = findMethod(nmsItem.getClass(), String.class, method -> {
                    try {
                        return !"toString".equals(method.getName()) && ((String) method.invoke(nmsItem)).toLowerCase().contains(material.getKey().getKey().toLowerCase(Locale.ROOT));
                    } catch (Throwable throwable) {
                        return false;
                    }
                });
            }

            if (getMinecraftKeyNameMethod == null) {
                Util.debugLog("getMinecraftKeyNameMethod is null");
                return null;
            } else {
                return (String) getMinecraftKeyNameMethod.invoke(nmsItem);
            }
        } catch (IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

}
