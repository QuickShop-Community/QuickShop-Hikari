package com.ghostchu.quickshop.util.envcheck;

import com.comphenix.protocol.ProtocolLibrary;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.GameVersion;
import com.ghostchu.quickshop.api.shop.display.DisplayType;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import com.ghostchu.quickshop.util.PackageUtil;
import com.ghostchu.quickshop.util.ReflectFactory;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.vdurmont.semver4j.Semver;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class EnvironmentChecker {

  private static final String CHECK_PASSED_RETURNS = "Check passed";
  private final QuickShop plugin;
  private final List<Method> tests = new ArrayList<>();

  public EnvironmentChecker(final QuickShop plugin) {

    this.plugin = plugin;
    this.registerTests(this.getClass()); //register self
  }

  /**
   * Register tests to QuickShop EnvChecker
   *
   * @param clazz The class contains test
   */
  public void registerTests(@NotNull final Class<?> clazz) {

    for(final Method declaredMethod : clazz.getDeclaredMethods()) {
      registerTest(declaredMethod);
    }
  }

  /**
   * Register test to QuickShop EnvChecker
   *
   * @param method The test method
   */
  public void registerTest(@NotNull final Method method) {

    final EnvCheckEntry envCheckEntry = method.getAnnotation(EnvCheckEntry.class);
    if(envCheckEntry == null) {
      return;
    }
    if(method.getReturnType() != ResultContainer.class) {
      plugin.logger().warn("Failed loading EncCheckEntry [{}]: Illegal test returns", method.getName());
      return;
    }
    tests.add(method);
  }

  @EnvCheckEntry(name = "CoreSupport Test", priority = 6)
  public ResultContainer coreSupportTest() {

    if(!plugin.getGameVersion().isCoreSupports()) {
      return new ResultContainer(CheckResult.STOP_WORKING, "Your Minecraft version is no longer supported: " + plugin.getPlatform().getMinecraftVersion() + " (" + ReflectFactory.getNMSVersion() + ")");
    }
    if(plugin.getGameVersion() == GameVersion.UNKNOWN) {
      return new ResultContainer(CheckResult.WARNING, "QuickShop may not fully support version " + ReflectFactory.getNMSVersion() + "/" + plugin.getPlatform().getMinecraftVersion() + ", Some features may not work.");
    }
    return new ResultContainer(CheckResult.PASSED, CHECK_PASSED_RETURNS);
  }

  @EnvCheckEntry(name = "End of life Test", priority = Integer.MAX_VALUE, stage = EnvCheckEntry.Stage.ON_ENABLE)
  public ResultContainer eolTest() {

    if(plugin.getGameVersion().isEndOfLife()) {
      return new ResultContainer(CheckResult.WARNING, "You're running a Minecraft server with a end of life version, QuickShop may not work on this version in future, consider upgrade your server version!");
    }
    return new ResultContainer(CheckResult.PASSED, CHECK_PASSED_RETURNS);
  }

  @EnvCheckEntry(name = "GameVersion supporting Test", priority = 9)
  public ResultContainer gamerVersionSupportTest() {

    if(plugin.getGameVersion() == GameVersion.UNKNOWN) {
      return new ResultContainer(CheckResult.WARNING, "Your Minecraft server version not tested by developers, QuickShop may ran into issues on this version.");
    }
    return new ResultContainer(CheckResult.PASSED, CHECK_PASSED_RETURNS);
  }

  public boolean isOutdatedJvm() {

    final String jvmVersion = System.getProperty("java.version"); //Use java version not jvm version.
    final String[] splitVersion = jvmVersion.split("\\.");
    if(splitVersion.length < 1) {
      Log.debug("Failed to parse jvm version to check: " + jvmVersion);
      return false;
    }
    try {
      final int majorVersion = Integer.parseInt(splitVersion[0]);
      return majorVersion < 17; //Target JDK/JRE version
    } catch(final NumberFormatException ignored) {
      Log.debug("Failed to parse jvm major version to check: " + splitVersion[0]);
      return false;
    }
  }

  @EnvCheckEntry(name = "ModdedServer Based Test", priority = 4)
  public ResultContainer moddedBasedTest() {

    boolean trigged = false;
    if(isForgeBasedServer()) {
      plugin.logger().warn("WARN: QuickShop is not designed and tested for Forge!");
      plugin.logger().warn("WARN: Use at you own risk!.");
      plugin.logger().warn("WARN: No support will be given!");
      trigged = true;
    }
    if(isFabricBasedServer()) {
      plugin.logger().warn("WARN: QuickShop is not designed and tested for Fabric!");
      plugin.logger().warn("WARN: Use at you own risk!.");
      plugin.logger().warn("WARN: No support will be given!");
      trigged = true;
    }
    if(trigged) {
      return new ResultContainer(CheckResult.WARNING, "No support will be given to modded servers.");
    }
    return new ResultContainer(CheckResult.PASSED, "Server is unmodified.");
  }

  @EnvCheckEntry(name = "ModdedServer Database Driver Test", priority = 5)
  public ResultContainer moddedServerDatabaseDriverTest() {

    final boolean trigged = (isForgeBasedServer() || isFabricBasedServer()) && !plugin.getConfig().getBoolean("database.mysql", false);
    if(trigged && Bukkit.getPluginManager().getPlugin("Mohist") == null) {
      return new ResultContainer(CheckResult.STOP_WORKING, "You can't use H2 database driver on Forge/Fabric hybird server (it's buggy and will destroy your data on Arclight). Use a MySQL server instead. If you're running Mohist or other no-bug software, add -Dcom.ghostchu.quickshop.util.envcheck.EnvironmentChecker.skip.MODDEDSERVER_DATABASE_DRIVER_TEST=true to startup flag to skip this check.");
    }
    return new ResultContainer(CheckResult.PASSED, "OK");
  }

  public boolean isForgeBasedServer() {
    //Forge server detect - Arclight
    if(CommonUtil.isClassAvailable("net.minecraftforge.server.ServerMain")) {
      return true;
    }
    if(CommonUtil.isClassAvailable("net.minecraftforge.fml.loading.ModInfo")) {
      return true;
    }
    if(CommonUtil.isClassAvailable("cpw.mods.modlauncher.serviceapi.ILaunchPluginService")) {
      return true;
    }
    return CommonUtil.isClassAvailable("net.minecraftforge.forgespi.locating.IModLocator");
  }

  public boolean isFabricBasedServer() {
    //Nobody really make it right!?
    return CommonUtil.isClassAvailable("net.fabricmc.loader.launch.knot.KnotClient"); //OMG
  }

  @EnvCheckEntry(name = "Old QuickShop Test", priority = 3)
  public ResultContainer oldQuickShopTest() {

    if(CommonUtil.isClassAvailable("com.ghostchu.quickshop.Util.NMS")) {
      return new ResultContainer(CheckResult.STOP_WORKING, "FATAL: Old QuickShop build is installed! You must remove old QuickShop jar from the plugins folder!");
    }
    return new ResultContainer(CheckResult.PASSED, "No old QuickShop jar installled on this server");
  }

  @EnvCheckEntry(name = "Permission Manager Test", priority = 10, stage = EnvCheckEntry.Stage.ON_ENABLE)
  public ResultContainer permManagerConflictTest() {

    if(Bukkit.getPluginManager().isPluginEnabled("GroupManager")) {
      return new ResultContainer(CheckResult.WARNING, "WARNING: Unsupported plugin management plugin [GroupManager] installed, the permissions may not working.");
    }
    return new ResultContainer(CheckResult.PASSED, CHECK_PASSED_RETURNS);
  }

  @EnvCheckEntry(name = "PacketListenerAPI Conflict Test", priority = 10)
  public ResultContainer plapiConflictTest() {

    if(plugin.isDisplayEnabled() && AbstractDisplayItem.getNowUsing() == DisplayType.VIRTUALITEM && Bukkit.getPluginManager().isPluginEnabled("ProtocolLib") && Bukkit.getPluginManager().isPluginEnabled("PacketListenerAPI")) {
      return new ResultContainer(CheckResult.WARNING, "Virtual DisplayItem may stop working on your server. We are already aware that [PacketListenerAPI] and [ProtocolLib] are conflicting. (QuickShops requirement to send fake items). If your display is not showing, please uninstall [PacketListenerAPI].");
    }
    return new ResultContainer(CheckResult.PASSED, CHECK_PASSED_RETURNS);
  }

  @EnvCheckEntry(name = "Reremake Test", priority = 11, stage = EnvCheckEntry.Stage.ON_ENABLE)
  public ResultContainer rereMakeTest() {

    if(Bukkit.getPluginManager().isPluginEnabled("QuickShop")) {
      return new ResultContainer(CheckResult.WARNING, "WARNING: Multiple QuickShop installed, uninstall one of them.");
    }
    return new ResultContainer(CheckResult.PASSED, CHECK_PASSED_RETURNS);
  }

  @EnvCheckEntry(name = "EcoEnchants V11 Check", priority = 12, stage = EnvCheckEntry.Stage.ON_ENABLE)
  public ResultContainer ecoEnchantsNewerVersionWarning() {

    final Plugin eePluginInstance = Bukkit.getPluginManager().getPlugin("EcoEnchants");
    if(eePluginInstance != null) {
      final Semver semver = new Semver(eePluginInstance.getDescription().getVersion());
      if(semver.getMajor() == 12 && semver.getMinor() <= 2 && semver.getPatch() < 1) {
        plugin.logger().warn("=================================================");
        plugin.logger().warn("WARNING: Risk of irreversible data corruption! Plugin startup is paused!");
        plugin.logger().warn("Your installation of EcoEnchants (version >= 11.0.0) has been detected.");
        plugin.logger().warn("Its plugin's enchantment loading logic was changed on v11 update, but due to EcoEnchants registering their enchantments with Bukkit too late, this would cause ItemStack to permanently lose all EcoEnchants enchantments during deserialization.");
        plugin.logger().warn("For this reason, once any update to QuickShop's store occurs, it will permanently affect the information saved in the database and cause irreversible, permanent damage to items and stores.");
        plugin.logger().warn("Unfortunately, EcoEnchants did not patch this issue until we released this version (see the link below), so to avoid widespread data corruption, we had to abort your server loading process.");
        plugin.logger().warn("https://discordapp.com/channels/452518336627081236/1183846962194300958");
        plugin.logger().warn("This doesn't just affect QuickShop, but also plugins like the one that loads ItemStack data during startup.");
        plugin.logger().warn("There is nothing we can do about this issue - until EcoEnchants fixes it.");
        plugin.logger().warn("If the issue has been fixed, or you are willing to take the risk, add this startup parameter to disable this check:");
        plugin.logger().warn("-Dcom.ghostchu.quickshop.util.envcheck.EnvironmentChecker.skip.ECOENCHANTS_V11_CHECK=true");
        plugin.logger().warn("=================================================");
        plugin.logger().error("Server startup has been terminated.");
        try {
          Thread.sleep(Integer.MAX_VALUE);
        } catch(final InterruptedException e) {
          return new ResultContainer(CheckResult.DISABLE_PLUGIN, "WARNING: Risk of irreversible data corruption! Plugin startup is paused!");
        }
        return new ResultContainer(CheckResult.DISABLE_PLUGIN, "WARNING: Risk of irreversible data corruption! Plugin startup is paused!");
      }

    }
    return new ResultContainer(CheckResult.PASSED, CHECK_PASSED_RETURNS);
  }

//    @EnvCheckEntry(name = "Legal Compliance Check", priority = 12, stage = EnvCheckEntry.Stage.ON_ENABLE)
//    public ResultContainer neteaseRegionTest() {
//        HttpResponse<String> resp = Unirest.get("https://cloudflare.com/cdn-cgi/trace")
//                .connectTimeout(1000 * 10)
//                .socketTimeout(1000 * 10)
//                .asString();
//        if (!resp.isSuccess()) {
//            return new ResultContainer(CheckResult.PASSED, "Failed to check NetEase region.");
//        }
//        String cloudflareResponse = resp.getBody();
//        String[] exploded = cloudflareResponse.split("\n");
//        for (String s : exploded) {
//            if (s.startsWith("loc=")) {
//                String[] kv = s.split("=");
//                if (kv.length != 2) {
//                    continue;
//                }
//                String key = kv[0];
//                String value = kv[1];
//                if (key.equalsIgnoreCase("loc") && value.equalsIgnoreCase("CN")) {
//                    return new ResultContainer(CheckResult.DISABLE_PLUGIN, "自 Hikari-4.1.0.3 开始，由于潜在的法律法规风险，我们暂时停止向处于中国大陆的服务器提供服务，有关更多信息，请参考：https://quickshop-community.github.io/QuickShop-Hikari-Documents/docs/about/netease");
//                }
//            }
//        }
//        return new ResultContainer(CheckResult.PASSED, CHECK_PASSED_RETURNS);
//    }

  public ResultReport run(final EnvCheckEntry.Stage stage) {

    sortTests();

    final Map<EnvCheckEntry, ResultContainer> results = new LinkedHashMap<>();
    boolean skipAllTest = false;
    ResultContainer executeResult = null;

    CheckResult gResult = CheckResult.PASSED;
    for(final Method declaredMethod : this.tests) {
      if(skipAllTest) {
        break;
      }
      CheckResult result = CheckResult.PASSED;
      try {
        final EnvCheckEntry envCheckEntry = declaredMethod.getAnnotation(EnvCheckEntry.class);
        if(Arrays.stream(envCheckEntry.stage()).noneMatch(entry->entry == stage)) {
          Log.debug("Skip test: " + envCheckEntry.name() + ": Except stage: " + Arrays.toString(envCheckEntry.stage()) + " Current stage: " + stage);
          continue;
        }
        if(!PackageUtil.parsePackageProperly("skip." + envCheckEntry.name().toUpperCase(Locale.ROOT).replace(" ", "_")).asBoolean()) {
          executeResult = (ResultContainer)declaredMethod.invoke(this);
          if(executeResult.getResult().ordinal() > result.ordinal()) { //set bad result if its worse than the latest one.
            result = executeResult.getResult();
          }
        } else {
          result = CheckResult.SKIPPED;
        }
        if(executeResult == null) {
          Log.debug("Failed to retrieve executeResult from " + declaredMethod.getName());
          continue;
        }
        switch(result) {
          case SKIPPED -> {
            plugin.logger().info("[SKIP] {}", envCheckEntry.name());
            Log.debug("Runtime check [" + envCheckEntry.name() + "] has been skipped (Startup Flag).");
          }
          case PASSED -> {
            if(Util.isDevEdition() || Util.isDevMode()) {
              plugin.logger().info("[OK] {}", envCheckEntry.name());
              Log.debug("[Pass] " + envCheckEntry.name() + ": " + executeResult.getResultMessage());
            }
          }
          case WARNING -> {
            plugin.logger().warn("[WARN] {}: {}", envCheckEntry.name(), executeResult.getResultMessage());
            Log.debug("[Warning] " + envCheckEntry.name() + ": " + executeResult.getResultMessage());
          }
          case STOP_WORKING -> {
            plugin.logger().warn("[STOP] {}: {}", envCheckEntry.name(), executeResult.getResultMessage());
            Log.debug("[Stop-Freeze] " + envCheckEntry.name() + ": " + executeResult.getResultMessage());
          }
          //It's okay, QuickShop should continue executing checks to collect more data.
          //And show user all errors at once.
          case DISABLE_PLUGIN -> {
            plugin.logger().warn("[FATAL] {}: {}", envCheckEntry.name(), executeResult.getResultMessage());
            Log.debug("[Fatal-Disable] " + envCheckEntry.name() + ": " + executeResult.getResultMessage());
            skipAllTest = true; //We need to disable the plugin NOW! Some HUGE exception is happening here, hurry up!
          }
          default ->
                  plugin.logger().warn("[UNDEFINED] {}: {}", envCheckEntry.name(), executeResult.getResultMessage());
        }
        results.put(envCheckEntry, Objects.requireNonNullElseGet(executeResult, ()->new ResultContainer(CheckResult.SKIPPED, "Startup flag mark this check should be skipped.")));
        if(result.ordinal() > gResult.ordinal()) { //set bad result if its worse than the latest one.
          gResult = result;
        }
      } catch(final Exception e) {
        plugin.logger().warn("Failed to execute EnvCheckEntry [{}]: Exception thrown out without getting caught. Something went wrong!", declaredMethod.getName(), e);
        plugin.logger().warn("[FAIL] {}", declaredMethod.getName());
      }
    }
    return new ResultReport(gResult, results);
  }

  private void sortTests() {

    tests.sort((o1, o2)->{
      final EnvCheckEntry e1 = o1.getAnnotation(EnvCheckEntry.class);
      final EnvCheckEntry e2 = o2.getAnnotation(EnvCheckEntry.class);
      return Integer.compare(e1.priority(), e2.priority());
    });
  }

  @EnvCheckEntry(name = "Spigot Based Server Test", priority = 2)
  public ResultContainer spigotBasedServer() {

    final ResultContainer success = new ResultContainer(CheckResult.PASSED, "Server");
    final ResultContainer failed = new ResultContainer(CheckResult.STOP_WORKING, "Server must be Spigot based, Don't use CraftBukkit!");
    if(!PaperLib.isSpigot()) {
      return failed;
    }
    return success;
  }

  @EnvCheckEntry(name = "Virtual DisplayItem Support Test", priority = 7, stage = EnvCheckEntry.Stage.ON_ENABLE)
  public ResultContainer virtualDisplaySupportTest() {

    if(!plugin.isDisplayEnabled()) {
      return new ResultContainer(CheckResult.PASSED, "The setting shop.display-items is disabled.");
    }
    if(AbstractDisplayItem.getNowUsing() != DisplayType.VIRTUALITEM) {
      return new ResultContainer(CheckResult.PASSED, "The setting shop.display-type is not virtual item.");
    }
    if(!plugin.getGameVersion().isVirtualDisplaySupports()) {
      AbstractDisplayItem.setVirtualDisplayDoesntWork(true);
      return new ResultContainer(CheckResult.WARNING, "Your server version are not supports Virtual DisplayItem, resetting to RealDisplayItem...");
    }
    if(Bukkit.getPluginManager().getPlugin("ProtocolLib") == null && Bukkit.getPluginManager().getPlugin("packetevents") == null) {
      AbstractDisplayItem.setVirtualDisplayDoesntWork(true);
      return new ResultContainer(CheckResult.WARNING, "ProtocolLib and packetevents are not installed on your server, Virtual DisplayItem will not work, resetting to no display items..");
    }

    return new ResultContainer(CheckResult.PASSED, "Passed checks");
  }

  @EnvCheckEntry(name = "Virtual DisplayItem Support Test", priority = 7, stage = EnvCheckEntry.Stage.AFTER_ON_ENABLE)
  public ResultContainer virtualDisplayWorkingTest() {

    if(!plugin.isDisplayEnabled()) {
      return new ResultContainer(CheckResult.PASSED, "The setting shop.display-items is disabled.");
    }
    if(AbstractDisplayItem.getNowUsing() != DisplayType.VIRTUALITEM) {
      return new ResultContainer(CheckResult.PASSED, "The setting shop.display-type is not virtual item.");
    }
    if(plugin.getVirtualDisplayItemManager() == null) {
      AbstractDisplayItem.setVirtualDisplayDoesntWork(true);
      return new ResultContainer(CheckResult.WARNING, "VirtualDisplayItemManager is null, this shouldn't happen, contact with QuickShop-Hikari developer.");
    }

    //TODO: Fix the tests.

    /*final Throwable testResult = plugin.getVirtualDisplayItemManager().getPacketFactory().testFakeItem();
    if(testResult != null) {

      plugin.getVirtualDisplayItemManager().setTestPassed(false);
      AbstractDisplayItem.setVirtualDisplayDoesntWork(true);
      plugin.logger().warn("Failed to load the VirtualDisplayItem, self-test failure", testResult);
      return new ResultContainer(CheckResult.WARNING, "VirtualDisplayItem test failed, turning off displays");
    }*/
    return new ResultContainer(CheckResult.PASSED, "Passed checks");
  }

  @EnvCheckEntry(name = "ProtocolLib Incorrect Locate Test", priority = 7)
  public ResultContainer protocolLibBadLocateTest() {

    try {
      Class.forName("com.comphenix.protocol.ProtocolLibrary");
    } catch(final ClassNotFoundException e) {
      return new ResultContainer(CheckResult.SKIPPED, "ProtocolLib not detected.");
    }
    final String stringClassLoader = ProtocolLibrary.getProtocolManager().getClass().getClassLoader().toString();
    if(stringClassLoader.contains("pluginEnabled=true") && !stringClassLoader.contains("plugin=ProtocolLib")) {
      plugin.logger().warn("Warning! ProtocolLib seems provided by another plugin, This seems to be a wrong packaging problem, " +
                           "QuickShop can't ensure the ProtocolLib is working correctly! Info: {}", stringClassLoader);
      return new ResultContainer(CheckResult.WARNING, "Incorrect locate: " + stringClassLoader);
    }
    return new ResultContainer(CheckResult.PASSED, stringClassLoader);
  }
}
