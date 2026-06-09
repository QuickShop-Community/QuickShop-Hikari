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
import dev.faststats.core.data.Metric;
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

  //TODO: Fully implement this class for research statistics. Will be implemented in future release.
  public FastStatsCollector(final StatCollector collector) {
    this.collector = collector;
  }

  @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Statistic - All shops hosted across all servers", description = "How many shops we can power across all servers? This research will used for performance tweak for components like shop managing/looking up/caching size etc.")
  public Metric<Number> statisticAllShops() {

    return Metric.number("statistic_all_shops", collector::statisticAllShops);
  }

  @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Statistic - All tags hosted across all servers", description = "How many tags we power across all servers? This research will used for performance tweak for components like tag managing/looking up/caching size etc.")
  public Metric<Number> statisticAllTags() {

    return Metric.number("statistic_all_tags", collector::statisticAllTags);
  }

  @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Research - Item Stacking Shop", description = "We collect this for determine if we should push Item Stacking Shop to a feature that default enabled.")
  public Metric<Boolean> researchItemStackingShop() {

    return Metric.bool("statistic_stacking", collector::researchItemStackingShop);
  }

  @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - Database Types", description = "We collect this so we can know the percent of different database types users.")
  public Metric<String> statisticDatabaseTypes() {

    return Metric.string("statistic_database_types", collector::statisticDatabaseType);
  }

  @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - Economy Types", description = "We collect this so we can know the percent of different economy types users.")
  public Metric<String> statisticEconomyTypes() {

    return Metric.string("statistic_economy_types", collector::statisticEconomyType);
  }

  @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - ItemMatcher", description = "We collect this so we can know the item matcher that users using, and improve it.")
  public Metric<String> statisticItemMatcher() {

    return Metric.string("statistic_item_matcher", collector::statisticItemMatcher);
  }

  @MetricCollectEntry(dataType = MetricDataType.STATISTIC, moduleName = "Statistic - Publisher", description = "We count the name of the publisher (in BuildInfo) so that we know if someone else is recompiling our plugin without changing the fork name. if you are a QuickShop-Hikari fork developer, please change the return value of your getFork() to something else in order to separate it from the stats. This value is usually fixed to Ghost-chu@Hikari.")
  public Metric<String> statisticPublisher() {

    return Metric.string("statistic_publisher", collector::statisticPublisher);
  }

  @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Research - ProtocolLib Version", description = "We collect this so we can know the which one ProtocolLib is popular. ProtocolLib sometimes releases destructive updates, so we collect this metric to know the distribution of ProtocolLib versions among users and remove unused ProtocolLib workaround code to improve code maintainability and program performance.")
  public Metric<String> researchProtocolLibVersion() {

    return Metric.string("statistic_protocollib", collector::researchProtocolLibVersion);
  }

  @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Research - PacketEvents Version", description = "We collect this so we can know the which one PacketEvents is popular. PacketEvents sometimes releases destructive updates, so we collect this metric to know the distribution of PacketEvents versions among users and remove unused PacketEvents workaround code to improve code maintainability and program performance.")
  public Metric<String> researchPacketEventsVersion() {

    return Metric.string("statistic_packetvents", collector::researchPacketEventsVersion);
  }

  @MetricCollectEntry(dataType = MetricDataType.RESEARCH, moduleName = "Research - Addons or Compacts Discovered", description = "QuickShop collects the QuickShop's addons/compacts (including 3rd-party) list that installed on your server to discover new addons/compacts so we can contact authors when we have major API changes, or use for improve exists official addons/compacts who have most of users using.")
  public Metric<String[]> researchAddonsCompacts() {

    return Metric.stringArray("statistics_extra", ()->collector.researchAddonsCompats().keySet().toArray(String[]::new));
  }
}