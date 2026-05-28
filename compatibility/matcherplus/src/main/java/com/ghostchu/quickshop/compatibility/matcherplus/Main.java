package com.ghostchu.quickshop.compatibility.matcherplus;

import com.ghostchu.quickshop.api.event.general.ShopItemMatchEvent;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.compatibility.matcherplus.matchers.ItemCheck;
import com.ghostchu.quickshop.compatibility.matcherplus.matchers.impl.AdvancedItemsCheck;
import com.ghostchu.quickshop.compatibility.matcherplus.matchers.impl.BreweryXCheck;
import com.ghostchu.quickshop.compatibility.matcherplus.matchers.impl.CrazyCratesCheck;
import com.ghostchu.quickshop.compatibility.matcherplus.matchers.impl.ExcellentCratesCheck;
import com.ghostchu.quickshop.compatibility.matcherplus.matchers.impl.NexoCheck;
import com.ghostchu.quickshop.compatibility.matcherplus.matchers.impl.PyroFishingCheck;
import com.ghostchu.quickshop.compatibility.matcherplus.matchers.impl.SilkSpawnerCheck;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public final class Main extends CompatibilityModule implements Listener {

  protected final Map<String, ItemCheck> checks = new HashMap<>();


  @Override
  public void init() {

    checks.put("pyrofishing", new PyroFishingCheck());
    checks.put("advanceditems", new AdvancedItemsCheck());

    if(Bukkit.getPluginManager().isPluginEnabled("BreweryX")) {
      checks.put("breweryx", new BreweryXCheck());
    }

    if(Bukkit.getPluginManager().isPluginEnabled("SilkSpawners")) {

      checks.put("silkspawners", new SilkSpawnerCheck());
    }

    if(Bukkit.getPluginManager().isPluginEnabled("CrazyCrates")) {

      checks.put("crazycrates", new CrazyCratesCheck());
    }

    if(Bukkit.getPluginManager().isPluginEnabled("ExcellentCrates")) {

      checks.put("excellentcrates", new ExcellentCratesCheck());
    }

    if(Bukkit.getPluginManager().isPluginEnabled("Nexo")) {
      checks.put("nexo", new NexoCheck());
    }
  }

  @EventHandler
  public void onMatch(final ShopItemMatchEvent event) {

    if(event.original() == null) {
      return;
    }

    if(event.comparison() == null) {
      return;
    }

    for(final ItemCheck check : checks.values()) {

      if(check.applies(event.original()) || check.applies(event.comparison())) {

        event.matches(check.matches(event.original(), event.comparison()));
        return;
      }
    }
  }
}