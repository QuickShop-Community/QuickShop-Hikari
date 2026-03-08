package com.ghostchu.quickshop.util.metric.faststats;

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

import com.ghostchu.quickshop.util.metric.MetricCollectEntry;
import com.ghostchu.quickshop.util.metric.MetricDataType;
import com.ghostchu.quickshop.util.metric.StatCollector;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.CustomChart;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;

/**
 * FastStatsCollector
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class FastStatsCollector {

  private final StatCollector collector;

  //TODO: Fully implement this class.
  public FastStatsCollector(final StatCollector collector) {
    this.collector = collector;
  }

  @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Research - Addons or Compacts Discovered", description = "QuickShop collects the QuickShop's addons/compacts (including 3rd-party) list that installed on your server to discover new addons/compacts so we can contact authors when we have major API changes, or use for improve exists official addons/compacts who have most of users using.")
  public CustomChart researchAddonsCompacts() {

    return new AdvancedPie("research_addons_or_compacts_discovered", collector::researchAddonsCompats);
  }

  @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Research - Item Stacking Shop", description = "We collect this for determine if we should push Item Stacking Shop to a feature that default enabled.")
  public CustomChart researchItemStackingShop() {

    return new SimplePie("research_item_stacking_shop", ()->String.valueOf(collector.researchItemStackingShop()));
  }

  @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Research - Protection Listener Blacklist", description = "We collect this for determine if we should add common listener blacklist entry to default configuration.")
  public CustomChart researchProtectionListenerBlacklist() {

    return new AdvancedPie("research_protection_checker_blacklist", collector::researchProtectionListenerBlacklist);
  }

  @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Research - Command Alias", description = "We collect this for determine if we should add/remove alias to default configuration.")
  public CustomChart researchCommandAlias() {

    return new AdvancedPie("research_command_alias", collector::researchCommandAlias);
  }

  @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - Database Types", description = "We collect this so we can know the percent of different database types users.")
  public CustomChart statisticDatabaseTypes() {

    return new SimplePie("statistic_database_types", collector::statisticDatabaseType);
  }

  @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - Economy Types", description = "We collect this so we can know the percent of different economy types users.")
  public CustomChart statisticEconomyTypes() {

    return new SimplePie("statistic_economy_types", collector::statisticEconomyType);
  }

  @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - ItemMatcher", description = "We collect this so we can know the item matcher that users using, and improve it.")
  public CustomChart statisticItemMatcher() {

    return new SimplePie("statistic_item_matcher", collector::statisticItemMatcher);
  }

  @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - DisplayImpl", description = "We collect this so we can know the which one item display impl most using, and improve it.")
  public CustomChart statisticDisplayImpl() {

    return new SimplePie("statistic_displayimpl", collector::statisticDisplayImpl);
  }

  @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Statistic - All shops hosting across all servers", description = "How many shops we can power across all servers? This research will used for performance tweak for components like shop managing/looking up/caching size etc.")
  public CustomChart statisticAllShops() {

    return new SingleLineChart("statistic_all_shops_hosting_across_all_servers", collector::statisticAllShops);
  }

  @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Statistic - All tags hosted across all servers", description = "How many tags we power across all servers? This research will used for performance tweak for components like tag managing/looking up/caching size etc.")
  public CustomChart statisticAllTags() {

    return new SingleLineChart("statistic_all_tags_hosting_across_all_servers", collector::statisticAllTags);
  }

  @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - Background Debug Logger", description = "We collect this so we can know the which one item display impl most using, and improve it.")
  public CustomChart statisticBackgroundDebugLogger() {

    return new SimplePie("statistic_background_debug_logger", collector::statisticBackgroundDebugLogger);
  }

  @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Research - ProtocolLib Version", description = "We collect this so we can know the which one ProtocolLib is popular. ProtocolLib sometimes releases destructive updates, so we collect this metric to know the distribution of ProtocolLib versions among users and remove unused ProtocolLib workaround code to improve code maintainability and program performance.")
  public CustomChart researchProtocolLibVersion() {

    return new SimplePie("research_protocollib_version", collector::researchProtocolLibVersion);
  }

  @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Research - PacketEvents Version", description = "We collect this so we can know the which one PacketEvents is popular. PacketEvents sometimes releases destructive updates, so we collect this metric to know the distribution of PacketEvents versions among users and remove unused PacketEvents workaround code to improve code maintainability and program performance.")
  public CustomChart researchPacketEventsVersion() {

    return new SimplePie("research_packetevents_version", collector::researchPacketEventsVersion);
  }

  @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - Server Software Build Version", description = "Spigot and Paper always release updates during their version support cycles. Counting the server-side software versions used by users lets us know which builds are popular. And it allows us to be more aggressive with newly added APIs, This can improve code maintainability, stability and program performance.")
  public CustomChart statisticServerSoftwareBuildVersion() {

    return new DrilldownPie("statistic_server_software_build_version", collector::statisticServerSoftwareBuildVersion);
  }

  @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - Publisher", description = "We count the name of the publisher (in BuildInfo) so that we know if someone else is recompiling our plugin without changing the fork name. if you are a QuickShop-Hikari fork developer, please change the return value of your getFork() to something else in order to separate it from the stats. This value is usually fixed to Ghost-chu@Hikari.")
  public CustomChart statisticPublisher() {

    return new SimplePie("statistic_publisher", collector::statisticPublisher);
  }

  @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Research - Geyser", description = "We've released the Suspension Closure expansion for Geyser, but we're ultimately undecided about a Geyser-specific update. The data collected from this study allows us to analyze the QuickShop-Hikari user base to check if Geyser or Floodgate is installed, and with the percentage of users who have the statistics, we will decide whether to add support for Geyser GUIs and the like. We also welcome your feedback on our Discord server.")
  public CustomChart researchGeyser() {

    return new SimplePie("research_geyser", collector::researchGeyser);
  }
}