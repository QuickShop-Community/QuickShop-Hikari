/*
 * This file is a part of project QuickShop, the name is EnvironmentChecker.java
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

package org.maxgamer.quickshop.util.envcheck;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.shop.AbstractDisplayItem;
import org.maxgamer.quickshop.api.shop.DisplayType;
import org.maxgamer.quickshop.shop.VirtualDisplayItem;
import org.maxgamer.quickshop.util.*;
import org.maxgamer.quickshop.util.security.JarVerifyTool;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class EnvironmentChecker {
    private final QuickShop plugin;
    private final List<Method> tests = new ArrayList<>();
    @Getter
    private final SecurityReport reportMaker = new SecurityReport();

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
                if (!properties.containsKey("org.maxgamer.quickshop.util.envcheck.skip." + envCheckEntry.name().toUpperCase(Locale.ROOT).replace(" ", "_"))) {
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
                    case KILL_SERVER:
                        plugin.getLogger().warning("[FATAL] " + envCheckEntry.name() + ": " + executeResult.getResultMessage());
                        Util.debugLog("[Fatal-Kill] " + envCheckEntry.name() + ": " + executeResult.getResultMessage());
                        skipAllTest = true; //We need to disable the plugin NOW! Some HUGE exception is happening here, hurry up!
                        break;
                    default:
                        plugin.getLogger().warning("[UNDEFINED] " + envCheckEntry.name() + ": " + executeResult.getResultMessage());
                }
                if (executeResult != null) {
                    results.put(envCheckEntry, executeResult);
                } else {
                    results.put(envCheckEntry, new ResultContainer(CheckResult.SKIPPED, "Startup flag mark this check should be skipped."));
                }
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
            return majorVersion < 16; //Target JDK/JRE version
        } catch (NumberFormatException ignored) {
            Util.debugLog("Failed to parse jvm major version to check: " + splitVersion[0]);
            return false;
        }
    }

    @SneakyThrows
    @EnvCheckEntry(name = "Signature Verify", priority = 0, stage = {EnvCheckEntry.Stage.ON_LOAD, EnvCheckEntry.Stage.ON_ENABLE})
    public ResultContainer securityVerify() {
        JarVerifyTool tool = new JarVerifyTool();
        JarFile jarFile = null;
        try {
            ClassLoader loader = this.getClass().getClassLoader();
            try (InputStream stream1 = loader.getResourceAsStream("META-INF/MANIFEST.MF");
                 InputStream stream2 = loader.getResourceAsStream("META-INF/SELFSIGN.DSA");
                 InputStream stream3 = loader.getResourceAsStream("META-INF/SELFSIGN.SF")) {
                if (stream1 == null || stream2 == null || stream3 == null) {
                    if(stream1 == null){
                        this.reportMaker.signatureFileMissing("META-INF/MANIFEST.MF");
                    }
                    if(stream2 == null){
                        this.reportMaker.signatureFileMissing("META-INF/SELFSIGN.DSA");
                    }
                    if(stream3 == null){
                        this.reportMaker.signatureFileMissing("META-INF/SELFSIGN.SF");
                    }
                    plugin.getLogger().warning("The signature could not be found! The QuickShop jar has been modified or you're running a custom build.");
                    return new ResultContainer(CheckResult.KILL_SERVER, "Security risk detected, QuickShop jar has been modified.");
                }
            }
            String jarPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
            jarPath = URLDecoder.decode(jarPath, "UTF-8");
            Util.debugLog("JarPath selected: " + jarPath);
            jarFile = new JarFile(jarPath);
            List<JarEntry> modifiedEntry = tool.verify(jarFile);
            if (modifiedEntry.isEmpty()) {
                return new ResultContainer(CheckResult.PASSED, "The jar is valid. No issues detected.");
            } else {
                modifiedEntry.forEach(jarEntry -> {
                    this.reportMaker.signatureVerifyFail(jarEntry);
                    plugin.getLogger().warning(">> Modified Class Detected <<");
                    plugin.getLogger().warning("Name: " + jarEntry.getName());
                    plugin.getLogger().warning("CRC: " + jarEntry.getCrc());
                    plugin.getLogger().warning(JsonUtil.getGson().toJson(jarEntry));
                });
                plugin.getLogger().severe("QuickShop detected that the jar has been modified! This is usually caused by the file being corrupted or virus infected.");
                plugin.getLogger().severe("To prevent severe server failure, QuickShop has been disabled.");
                plugin.getLogger().severe("For further information, Please join our support Discord server: https://discord.com/invite/bfefw2E.");
                return new ResultContainer(CheckResult.KILL_SERVER, "Security risk detected, QuickShop jar has been modified.");
            }
        } catch (Exception ioException) {
            plugin.getLogger().log(Level.WARNING, "ALERT: QuickShop cannot validate itself. This may be caused by you having deleted QuickShop's jar while the server is running.", ioException);
            return new ResultContainer(CheckResult.KILL_SERVER, "Failed to validate digital signature! Security may be compromised!");
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException ignored) {
                }
            }
        }
    }


    @EnvCheckEntry(name = "Plugin Manifest Check", priority = 1, stage = {EnvCheckEntry.Stage.ON_LOAD, EnvCheckEntry.Stage.ON_ENABLE})
    public ResultContainer manifestCheck() {
        String mainClass = plugin.getDescription().getMain();
        if(!mainClass.equals("org.maxgamer.quickshop.QuickShop")){
            this.reportMaker.manifestModified(plugin.getDescription());
            plugin.getLogger().warning("ALERT: Detected main class has been modified!");
            plugin.getLogger().warning("Should be: org.maxgamer.quickshop.QuickShop");
            plugin.getLogger().warning("Actually: "+mainClass);
            return new ResultContainer(CheckResult.KILL_SERVER, "Failed to validate main class! Security may be compromised!");

        }
        return new ResultContainer(CheckResult.PASSED, "Check passed.");
    }

    @EnvCheckEntry(name = "Potential Infection Characteristics Check", priority = 1, stage = {EnvCheckEntry.Stage.ON_LOAD, EnvCheckEntry.Stage.ON_ENABLE})
    public ResultContainer potentialCheck() {
        String jarPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        try {
            jarPath = URLDecoder.decode(jarPath, "UTF-8");
            Util.debugLog("JarPath selected: " + jarPath);
            ZipFile zipFile = new ZipFile(jarPath);
            Enumeration<? extends ZipEntry> zipEntryEnumeration = zipFile.entries();
            boolean found = false;
            while(zipEntryEnumeration.hasMoreElements()){
                ZipEntry entry = zipEntryEnumeration.nextElement();
                if(entry.getName().startsWith("javassist") || entry.getName().startsWith(".")){
                    found = true;
                    this.reportMaker.potentialInfected(entry);
                    plugin.getLogger().log(Level.WARNING, "Potential Infection Detected:");
                    plugin.getLogger().log(Level.WARNING, "File: "+entry.getName());
                    plugin.getLogger().log(Level.WARNING, "CRC: "+entry.getCrc());
                    plugin.getLogger().log(Level.WARNING, "Time: "+entry.getTime());
                }
            }
            if(found){
                plugin.getLogger().log(Level.WARNING, "ALERT: QuickShop detected Potential Infection, this jar may already infected by malware, stop the server and create full server backup immediately, run virus scan and contact the QuickShop support if you need!");
                return new ResultContainer(CheckResult.KILL_SERVER, "Potential Infection detected, Killing server process...");
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "ALERT: QuickShop cannot validate itself. This may be caused by you having deleted QuickShop's jar while the server is running.", e);
            return new ResultContainer(CheckResult.KILL_SERVER, "Failed to validate files in jar! Security may be compromised!");
        }
        return new ResultContainer(CheckResult.PASSED, "Check passed.");
    }


    @SneakyThrows
    @EnvCheckEntry(name = "Java Runtime Environment Version Test", priority = 1, stage = EnvCheckEntry.Stage.ON_ENABLE)
    public ResultContainer jrevTest() {
        if (isOutdatedJvm()) {
            String jvmWarning = "\n" +
                    "============================================================\n" +
                    "    Warning! You're running an outdated version of Java!\n" +
                    "============================================================\n" +
                    "* QuickShop will stop being compatible with your current Java version in the future!\n" +
                    "*\n" +
                    "* You should schedule a Java upgrade on your server,\n" +
                    "* because we will drop support for any Java versions lower than Java 16.\n" +
                    "*\n" +
                    "* That means:\n" +
                    "* 1) Future QuickShop builds will no longer work on your server.\n" +
                    "* 2) No support for QuickShop builds that run on outdated Java versions will be given.\n" +
                    "* \n" +
                    "* Why:\n" +
                    "* 1) We don't want to keep compatibility with outdated software\n" +
                    "*    and systems. As Paper upgraded, we did too.\n" +
                    "* 2) Java is downward compatible, so most legacy (Java 8+) Plugins should still work.\n" +
                    "* 3) Newer Java APIs allow accessing resources and processing them\n" +
                    "*    faster than before, meaning a performance improvement.\n" +
                    "* \n" +
                    "* What should I do?\n" +
                    "* You should update your server's Java version\n" +
                    "* as soon as you can to Java 16 or higher.\n" +
                    "*\n" +
                    "* Most plugins can run on Java 16+ without problems\n" +
                    "* unless their code is really outdated.\n" +
                    "* If a Plugin is not compatible with Java 16 and the developer\n" +
                    "* doesn't update it, then you should replace them.\n" +
                    "*\n" +
                    "* You can get Java 16 here:\n" +
                    "* https://www.oracle.com/java/technologies/javase-downloads.html\n" +
                    "*\n" +
                    String.format("* Current Java version: %s", System.getProperty("java.version"));
            plugin.getLogger().warning(jvmWarning);
            Thread.sleep(5000);
            return new ResultContainer(CheckResult.WARNING, "Unsupported outdated JRE version detected.");
        }
        return new ResultContainer(CheckResult.PASSED, "Java is up-to-date: " + System.getProperty("java.version"));
    }

    @EnvCheckEntry(name = "Spigot Based Server Test", priority = 2)
    public ResultContainer spigotBasedServer() {
        ResultContainer success = new ResultContainer(CheckResult.PASSED, "Server");
        ResultContainer failed = new ResultContainer(CheckResult.STOP_WORKING, "Server must be Spigot based, Don't use CraftBukkit!");
        try {
            //API test
            try {
                plugin.getServer().spigot();
                String nmsVersion = ReflectFactory.getNMSVersion();
                plugin.getLogger().info("Running QuickShop-" + QuickShop.getFork() + " on NMS version " + nmsVersion + " For Minecraft version " + ReflectFactory.getServerVersion());
            } catch (Throwable e) {
                return failed;
            }
            return success;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to check Spigot API!" + plugin.getServer().getBukkitVersion() + " may not be Spigot based.");
            return failed;
        }

    }

    @EnvCheckEntry(name = "Old QuickShop Test", priority = 3)
    public ResultContainer oldQuickShopTest() {
        if (Util.isClassAvailable("org.maxgamer.quickshop.Util.NMS")) {
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
            return new ResultContainer(CheckResult.STOP_WORKING, "Your Minecraft version is no longer supported: " + ReflectFactory.getServerVersion() + " (" + nmsVersion + ")");
        }
        if (gameVersion == GameVersion.UNKNOWN) {
            return new ResultContainer(CheckResult.WARNING, "QuickShop may not fully support version " + nmsVersion + "/" + ReflectFactory.getServerVersion() + ", Some features may not work.");
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

    @EnvCheckEntry(name = "End of life Test", priority = Integer.MAX_VALUE, stage = EnvCheckEntry.Stage.ON_ENABLE)
    public ResultContainer eolTest() {
        if (plugin.getGameVersion().isEndOfLife()) {
            return new ResultContainer(CheckResult.WARNING, "End Of Life! This Minecraft version no-longer receive QuickShop future updates! You won't receive any updates from QuickShop, think about upgrading!");
        }
        return new ResultContainer(CheckResult.PASSED, "Passed checks");
    }
}
