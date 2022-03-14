/*
 *  This file is a part of project QuickShop, the name is EnvironmentChecker.java
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

package com.ghostchu.quickshop.util.envcheck;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.AbstractDisplayItem;
import com.ghostchu.quickshop.api.shop.DisplayType;
import com.ghostchu.quickshop.shop.display.VirtualDisplayItem;
import com.ghostchu.quickshop.util.GameVersion;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.ReflectFactory;
import com.ghostchu.quickshop.util.Util;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;

public final class EnvironmentChecker {
    private final QuickShop plugin;
    private final List<Method> tests = new ArrayList<>();

    public EnvironmentChecker(QuickShop plugin) {
        this.plugin = plugin;
        this.registerTests(this.getClass()); //register self
    }

    /**
     * Register tests to QuickShop EnvChecker
     *
     * @param clazz The class contains test
     */
    public void registerTests(@NotNull Class<?> clazz) {
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            registerTest(declaredMethod);
        }
    }

    /**
     * Register test to QuickShop EnvChecker
     *
     * @param method The test method
     */
    public void registerTest(@NotNull Method method) {
        EnvCheckEntry envCheckEntry = method.getAnnotation(EnvCheckEntry.class);
        if (envCheckEntry == null) {
            return;
        }
        if (method.getReturnType() != ResultContainer.class) {
            plugin.getLogger().warning("Failed loading EncCheckEntry [" + method.getName() + "]: Illegal test returns");
            return;
        }
        tests.add(method);
    }

    private void sortTests() {
        tests.sort((o1, o2) -> {
            EnvCheckEntry e1 = o1.getAnnotation(EnvCheckEntry.class);
            EnvCheckEntry e2 = o2.getAnnotation(EnvCheckEntry.class);
            return Integer.compare(e1.priority(), e2.priority());
        });
    }

    public ResultReport run(EnvCheckEntry.Stage stage) {
        sortTests();

        Map<EnvCheckEntry, ResultContainer> results = new LinkedHashMap<>();
        boolean skipAllTest = false;
        ResultContainer executeResult = null;

        Properties properties = System.getProperties();
        CheckResult gResult = CheckResult.PASSED;
        for (Method declaredMethod : this.tests) {
            if (skipAllTest) {
                break;
            }
            CheckResult result = CheckResult.PASSED;
            try {
                EnvCheckEntry envCheckEntry = declaredMethod.getAnnotation(EnvCheckEntry.class);
                if (Arrays.stream(envCheckEntry.stage()).noneMatch(entry -> entry == stage)) {
                    Util.debugLog("Skip test: " + envCheckEntry.name() + ": Except stage: " + Arrays.toString(envCheckEntry.stage()) + " Current stage: " + stage);
                    continue;
                }
                if (!properties.containsKey("com.ghostchu.quickshop.util.envcheck.skip." + envCheckEntry.name().toUpperCase(Locale.ROOT).replace(" ", "_"))) {
                    executeResult = (ResultContainer) declaredMethod.invoke(this);
                    if (executeResult.getResult().ordinal() > result.ordinal()) { //set bad result if its worse than the latest one.
                        result = executeResult.getResult();
                    }
                } else {
                    result = CheckResult.SKIPPED;
                }
                switch (result) {
                    case SKIPPED:
                        plugin.getLogger().info("[SKIP] " + envCheckEntry.name());
                        Util.debugLog("Runtime check [" + envCheckEntry.name() + "] has been skipped (Startup Flag).");
                        break;
                    case PASSED:
                        if (Util.isDevEdition() || Util.isDevMode()) {
                            plugin.getLogger().info("[OK] " + envCheckEntry.name());
                            Util.debugLog("[Pass] " + envCheckEntry.name() + ": " + executeResult.getResultMessage());
                        }
                        break;
                    case WARNING:
                        plugin.getLogger().warning("[WARN] " + envCheckEntry.name() + ": " + executeResult.getResultMessage());
                        Util.debugLog("[Warning] " + envCheckEntry.name() + ": " + executeResult.getResultMessage());
                        break;
                    case STOP_WORKING:
                        plugin.getLogger().warning("[STOP] " + envCheckEntry.name() + ": " + executeResult.getResultMessage());
                        Util.debugLog("[Stop-Freeze] " + envCheckEntry.name() + ": " + executeResult.getResultMessage());
                        //It's okay, QuickShop should continue executing checks to collect more data.
                        //And show user all errors at once.
                        break;
                    case DISABLE_PLUGIN:
                        plugin.getLogger().warning("[FATAL] " + envCheckEntry.name() + ": " + executeResult.getResultMessage());
                        Util.debugLog("[Fatal-Disable] " + envCheckEntry.name() + ": " + executeResult.getResultMessage());
                        skipAllTest = true; //We need to disable the plugin NOW! Some HUGE exception is happening here, hurry up!
                        break;
                    default:
                        plugin.getLogger().warning("[UNDEFINED] " + envCheckEntry.name() + ": " + executeResult.getResultMessage());
                }
                results.put(envCheckEntry, Objects.requireNonNullElseGet(executeResult, () -> new ResultContainer(CheckResult.SKIPPED, "Startup flag mark this check should be skipped.")));
                if (result.ordinal() > gResult.ordinal()) { //set bad result if its worse than the latest one.
                    gResult = result;
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to execute EnvCheckEntry [" + declaredMethod.getName() + "]: Exception thrown out without getting caught. Something went wrong!", e);
                plugin.getLogger().warning("[FAIL] " + declaredMethod.getName());
            }
        }
        return new ResultReport(gResult, results);
    }

    public boolean isOutdatedJvm() {
        String jvmVersion = System.getProperty("java.version"); //Use java version not jvm version.
        String[] splitVersion = jvmVersion.split("\\.");
        if (splitVersion.length < 1) {
            Util.debugLog("Failed to parse jvm version to check: " + jvmVersion);
            return false;
        }
        try {
            int majorVersion = Integer.parseInt(splitVersion[0]);
            return majorVersion < 17; //Target JDK/JRE version
        } catch (NumberFormatException ignored) {
            Util.debugLog("Failed to parse jvm major version to check: " + splitVersion[0]);
            return false;
        }
    }

    @EnvCheckEntry(name = "Spigot Based Server Test", priority = 2)
    public ResultContainer spigotBasedServer() {
        ResultContainer success = new ResultContainer(CheckResult.PASSED, "Server");
        ResultContainer failed = new ResultContainer(CheckResult.STOP_WORKING, "Server must be Spigot based, Don't use CraftBukkit!");
        if(!PaperLib.isSpigot()) {
            return failed;
        }
        return success;
    }

    @EnvCheckEntry(name = "Old QuickShop Test", priority = 3)
    public ResultContainer oldQuickShopTest() {
        if (Util.isClassAvailable("com.ghostchu.quickshop.Util.NMS")) {
            return new ResultContainer(CheckResult.STOP_WORKING, "FATAL: Old QuickShop build is installed! You must remove old QuickShop jar from the plugins folder!");
        }
        return new ResultContainer(CheckResult.PASSED, "No old QuickShop jar installled on this server");
    }

    public boolean isForgeBasedServer() {
        //Forge server detect - Arclight
        if (Util.isClassAvailable("net.minecraftforge.server.ServerMain")) {
            return true;
        }
        return Util.isClassAvailable("net.minecraftforge.fml.loading.ModInfo");
    }

    public boolean isFabricBasedServer() {
        //Nobody really make it right!?
        return Util.isClassAvailable("net.fabricmc.loader.launch.knot.KnotClient"); //OMG
    }

    @EnvCheckEntry(name = "ModdedServer Based Test", priority = 4)
    public ResultContainer moddedBasedTest() {
        boolean trigged = false;
        if (isForgeBasedServer()) {
            plugin.getLogger().warning("WARN: QuickShop is not designed and tested for Forge!");
            plugin.getLogger().warning("WARN: Use at you own risk!.");
            plugin.getLogger().warning("WARN: No support will be given!");
            trigged = true;
        }
        if (isFabricBasedServer()) {
            plugin.getLogger().warning("WARN: QuickShop is not designed and tested for Fabric!");
            plugin.getLogger().warning("WARN: Use at you own risk!.");
            plugin.getLogger().warning("WARN: No support will be given!");
            trigged = true;
        }
        if (trigged) {
            return new ResultContainer(CheckResult.WARNING, "No support will be given to modded servers.");
        }
        return new ResultContainer(CheckResult.PASSED, "Server is unmodified.");
    }

    @EnvCheckEntry(name = "CoreSupport Test", priority = 6)
    public ResultContainer coreSupportTest() {
        String nmsVersion = ReflectFactory.getNMSVersion();
        GameVersion gameVersion = GameVersion.get(nmsVersion);
        if (!gameVersion.isCoreSupports()) {
            return new ResultContainer(CheckResult.STOP_WORKING, "Your Minecraft version is no longer supported: " + plugin.getPlatform().getMinecraftVersion() + " (" + nmsVersion + ")");
        }
        if (gameVersion == GameVersion.UNKNOWN) {
            return new ResultContainer(CheckResult.WARNING, "QuickShop may not fully support version " + nmsVersion + "/" + plugin.getPlatform().getMinecraftVersion() + ", Some features may not work.");
        }
        return new ResultContainer(CheckResult.PASSED, "Passed checks");
    }

    @EnvCheckEntry(name = "Virtual DisplayItem Support Test", priority = 7)
    public ResultContainer virtualDisplaySupportTest() {
        String nmsVersion = ReflectFactory.getNMSVersion();
        GameVersion gameVersion = GameVersion.get(nmsVersion);
        Throwable throwable;
        if (!gameVersion.isVirtualDisplaySupports()) {
            throwable = new IllegalStateException("Version not supports Virtual DisplayItem.");
        } else {
            if (plugin.getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
                throwable = VirtualDisplayItem.PacketFactory.testFakeItem();
            } else {
                AbstractDisplayItem.setNotSupportVirtualItem(true);
                return new ResultContainer(CheckResult.WARNING, "ProtocolLib is not installed, virtual DisplayItem seems will not work on your server.");
            }
        }
        if (throwable != null) {
            Util.debugLog(throwable.getMessage());
            MsgUtil.debugStackTrace(throwable.getStackTrace());
            AbstractDisplayItem.setNotSupportVirtualItem(true);
            //do not throw
            plugin.getLogger().log(Level.SEVERE, "Virtual DisplayItem Support Test: Failed to initialize VirtualDisplayItem", throwable);
            return new ResultContainer(CheckResult.WARNING, "Virtual DisplayItem seems to not work on this Minecraft server, Make sure QuickShop, ProtocolLib and server builds are up to date.");
        } else {
            return new ResultContainer(CheckResult.PASSED, "Passed checks");
        }
    }


    @EnvCheckEntry(name = "GameVersion supporting Test", priority = 9)
    public ResultContainer gamerVersionSupportTest() {
        String nmsVersion = ReflectFactory.getNMSVersion();
        GameVersion gameVersion = GameVersion.get(nmsVersion);
        if (gameVersion == GameVersion.UNKNOWN) {
            return new ResultContainer(CheckResult.WARNING, "Your Minecraft server version not tested by developers, QuickShop may ran into issues on this version.");
        }
        return new ResultContainer(CheckResult.PASSED, "Passed checks");
    }

    @EnvCheckEntry(name = "PacketListenerAPI Conflict Test", priority = 10)
    public ResultContainer plapiConflictTest() {
        if (plugin.isDisplayEnabled() && AbstractDisplayItem.getNowUsing() == DisplayType.VIRTUALITEM && Bukkit.getPluginManager().isPluginEnabled("ProtocolLib") && Bukkit.getPluginManager().isPluginEnabled("PacketListenerAPI")) {
            return new ResultContainer(CheckResult.WARNING, "Virtual DisplayItem may stop working on your server. We are already aware that [PacketListenerAPI] and [ProtocolLib] are conflicting. (QuickShops requirement to send fake items). If your display is not showing, please uninstall [PacketListenerAPI].");
        }
        return new ResultContainer(CheckResult.PASSED, "Passed checks");
    }

    @EnvCheckEntry(name = "Permission Manager Test", priority = 10, stage = EnvCheckEntry.Stage.ON_ENABLE)
    public ResultContainer permManagerConflictTest() {
        if (plugin.getServer().getPluginManager().isPluginEnabled("GroupManager")) {
            return new ResultContainer(CheckResult.WARNING, "WARNING: Unsupported plugin management plugin [GroupManager] installed, the permissions may not working.");
        }
        return new ResultContainer(CheckResult.PASSED, "Passed checks");
    }

    @EnvCheckEntry(name = "Reremake Test", priority = 11, stage = EnvCheckEntry.Stage.ON_ENABLE)
    public ResultContainer rereMakeTest() {
        if (plugin.getServer().getPluginManager().isPluginEnabled("QuickShop")) {
            return new ResultContainer(CheckResult.WARNING, "WARNING: Multiple QuickShop installed, uninstall one of them.");
        }
        return new ResultContainer(CheckResult.PASSED, "Passed checks");
    }

    @EnvCheckEntry(name = "End of life Test", priority = Integer.MAX_VALUE, stage = EnvCheckEntry.Stage.ON_ENABLE)
    public ResultContainer eolTest() {
        if (plugin.getGameVersion().isEndOfLife()) {
            return new ResultContainer(CheckResult.WARNING, "End Of Life! This Minecraft version no-longer receive QuickShop future updates! You won't receive any updates from QuickShop, think about upgrading!");
        }
        return new ResultContainer(CheckResult.PASSED, "Passed checks");
    }
}
