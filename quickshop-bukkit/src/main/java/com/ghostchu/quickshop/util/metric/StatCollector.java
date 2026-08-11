package com.ghostchu.quickshop.util.metric;

/*
 * QuickShop-Hikari
 * Copyright (C) 2026 Daniel "creatorfromhell" Vidmar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.shop.display.AbstractDisplayItem;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * StatCollector
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class StatCollector {

  private final QuickShop plugin;

  public StatCollector(final QuickShop plugin) {
    this.plugin = plugin;
  }

  @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Research - Addons or Compacts Discovered", description = "QuickShop collects the QuickShop's addons/compacts (including 3rd-party) list that installed on your server to discover new addons/compacts so we can contact authors when we have major API changes, or use for improve exists official addons/compacts who have most of users using.")
  public Map<String, Integer> researchAddonsCompats() {
    final String myName = plugin.getJavaPlugin().getDescription().getName();
    final Map<String, Integer> data = new HashMap<>();
    for(final Plugin discoverPlugin : Bukkit.getPluginManager().getPlugins()) {
      final PluginDescriptionFile descriptionFile = discoverPlugin.getDescription();
      if(descriptionFile.getDepend().contains(myName) || descriptionFile.getSoftDepend().contains(myName)) {
        data.put(descriptionFile.getName(), 1);
      }
    }
    if(data.isEmpty()) {
      data.put("None", 1);
    }
    return data;
  }

  @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Research - Item Stacking Shop", description = "We collect this for determine if we should push Item Stacking Shop to a feature that default enabled.")
  public boolean researchItemStackingShop() {
    return plugin.isAllowStack();
  }

  @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Research - Protection Listener Blacklist", description = "We collect this for determine if we should add common listener blacklist entry to default configuration.")
  public Map<String, Integer> researchProtectionListenerBlacklist() {
    final Map<String, Integer> data = new HashMap<>();
    plugin.getConfig().getStringList("shop.protection-checking-listener-blacklist").forEach(s->data.put(s, 1));
    return data;
  }

  @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Research - Command Alias", description = "We collect this for determine if we should add/remove alias to default configuration.")
  public Map<String, Integer> researchCommandAlias() {
    final Map<String, Integer> data = new HashMap<>();
    plugin.getConfig().getStringList("custom-commands").forEach(s->data.put(s, 1));

    return data;
  }

  @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - Database Types", description = "We collect this so we can know the percent of different database types users.")
  public String statisticDatabaseType() {
    return plugin.getDatabaseDriverType().name();
  }

  @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - Economy Types", description = "We collect this so we can know the percent of different economy types users.")
  public String statisticEconomyType() {
    return plugin.getEconomyManager().provider().name();
  }

  @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - ItemMatcher", description = "We collect this so we can know the item matcher that users using, and improve it.")
  public String statisticItemMatcher() {
    return plugin.getItemMatcher().getName();
  }

  @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - DisplayImpl", description = "We collect this so we can know the which one item display impl most using, and improve it.")
  public String statisticDisplayImpl() {
    return AbstractDisplayItem.getNowUsing().name();
  }

  @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Statistic - All shops hosting across all servers", description = "How many shops we can power across all servers? This research will used for performance tweak for components like shop managing/looking up/caching size etc.")
  public int statisticAllShops() {
    return plugin.getShopManager().getAllShops().size();
  }

  @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Statistic - All tags hosted across all servers", description = "How many tags we power across all servers? This research will used for performance tweak for components like tag managing/looking up/caching size etc.")
  public int statisticAllTags() {
    return plugin.tagManager().totalTags();
  }

  @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - Background Debug Logger", description = "We collect this so we can know the which one item display impl most using, and improve it.")
  public String statisticBackgroundDebugLogger() {
    return plugin.getConfig().getBoolean("debug.disable-debuglogger") ? "Disabled" : "Enabled";
  }

  @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Research - ProtocolLib Version", description = "We collect this so we can know the which one ProtocolLib is popular. ProtocolLib sometimes releases destructive updates, so we collect this metric to know the distribution of ProtocolLib versions among users and remove unused ProtocolLib workaround code to improve code maintainability and program performance.")
  public String researchProtocolLibVersion() {
    final Plugin protocolLib = Bukkit.getPluginManager().getPlugin("ProtocolLib");
    if(protocolLib == null) {
      return "Not Installed";
    }
    return protocolLib.getDescription().getVersion();
  }

  @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Research - PacketEvents Version", description = "We collect this so we can know the which one PacketEvents is popular. PacketEvents sometimes releases destructive updates, so we collect this metric to know the distribution of PacketEvents versions among users and remove unused PacketEvents workaround code to improve code maintainability and program performance.")
  public String researchPacketEventsVersion() {
    final Plugin packetevents = Bukkit.getPluginManager().getPlugin("packetevents");
    if(packetevents == null) {
      return "Not Installed";
    }
    return packetevents.getDescription().getVersion();
  }

  @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - Server Software Build Version", description = "Spigot and Paper always release updates during their version support cycles. Counting the server-side software versions used by users lets us know which builds are popular. And it allows us to be more aggressive with newly added APIs, This can improve code maintainability, stability and program performance.")
  public Map<String, Map<String, Integer>> statisticServerSoftwareBuildVersion() {
    final Map<String, Map<String, Integer>> map = new HashMap<>();
    final Map<String, Integer> entry = new HashMap<>();
    entry.put(Bukkit.getServer().getVersion(), 1);
    map.put(Bukkit.getServer().getName(), entry);
    return map;
  }

  @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - Publisher", description = "We count the name of the publisher (in BuildInfo) so that we know if someone else is recompiling our plugin without changing the fork name. if you are a QuickShop-Hikari fork developer, please change the return value of your getFork() to something else in order to separate it from the stats. This value is usually fixed to Ghost-chu@Hikari.")
  public String statisticPublisher() {
    return plugin.getBuildInfo().getGitInfo().getCommitUsername() + "@" + plugin.getFork();
  }

  @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Research - Geyser", description = "We've released the Suspension Closure expansion for Geyser, but we're ultimately undecided about a Geyser-specific update. The data collected from this study allows us to analyze the QuickShop-Hikari user base to check if Geyser or Floodgate is installed, and with the percentage of users who have the statistics, we will decide whether to add support for Geyser GUIs and the like. We also welcome your feedback on our Discord server.")
  public String researchGeyser() {

    final StringJoiner joiner = new StringJoiner("+");
    joiner.setEmptyValue("Not detected");
    final Plugin geyser = Bukkit.getPluginManager().getPlugin("Geyser-Spigot");
    if(geyser != null) {
      joiner.add("Geyser-Spigot");
    }
    final Plugin floodgate = Bukkit.getPluginManager().getPlugin("floodgate");
    if(floodgate != null) {
      joiner.add("Floodgate");
    }
    return joiner.toString();
  }
}