package com.ghostchu.quickshop.addon.quests;

import com.ghostchu.quickshop.api.event.economy.ShopPurchaseEvent;
import com.ghostchu.quickshop.api.event.management.ShopCreateEvent;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.util.logger.Log;
import me.pikamug.quests.Quests;
import me.pikamug.quests.player.Quester;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

import java.util.UUID;

public final class Main extends CompatibilityModule {

  private Quests plugin;
  private boolean questRequiredCreate = false;
  private boolean questRequiredBuy = false;
  private boolean questRequiredSell = false;

  //Our quest names
  private String questCreateShop = "";
  private String questBuyShop = "";
  private String questSellShop = "";

  @Override
  public void init() {

    plugin = (Quests)Bukkit.getServer().getPluginManager().getPlugin("Quests");
    if(plugin == null) {

      Log.debug("Unable to find Quests. Disabling QuickShop-Quests Addon.");
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }

    questRequiredCreate = getConfig().getBoolean("quest-required-create");
    questRequiredBuy = getConfig().getBoolean("quest-required-buy");
    questRequiredSell = getConfig().getBoolean("quest-required-sell");

    //our quest names
    questCreateShop = getConfig().getString("quest-create-name");
    questBuyShop = getConfig().getString("quest-buy-name");
    questSellShop = getConfig().getString("quest-sell-name");

    //check to see if the quest names are valid
    if(this.questRequiredCreate && plugin.getLoadedQuests().stream().noneMatch(q -> q.getName().equalsIgnoreCase(this.questCreateShop))) {

      Log.debug("Unable to find Quest: " + this.questCreateShop + ". Disabling Create requirement.");
      this.questRequiredCreate = false;
    }

    if(this.questRequiredBuy && plugin.getLoadedQuests().stream().noneMatch(q -> q.getName().equalsIgnoreCase(this.questBuyShop))) {

      Log.debug("Unable to find Quest: " + this.questBuyShop + ". Disabling Buy requirement.");
      this.questRequiredBuy = false;
    }

    if(this.questRequiredSell && plugin.getLoadedQuests().stream().noneMatch(q -> q.getName().equalsIgnoreCase(this.questBuyShop))) {

      Log.debug("Unable to find Quest: " + this.questSellShop + ". Disabling Buy requirement.");
      this.questRequiredSell = false;
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onPreCreation(final ShopCreateEvent event) {

    if(!event.phase().cancellable() || !this.questRequiredCreate) {

      return;
    }

    if(!isQuestComplete(event.user().getUniqueId(), this.questCreateShop)) {

      event.setCancelled(true, getApi().getTextManager().of(event.user(), "addon.quests.create-quest-required", this.questCreateShop).forLocale());
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onTrading(final ShopPurchaseEvent event) {

    if(event.getShop().shopType().isBuying() && this.questRequiredSell
       && !isQuestComplete(event.getPurchaser().getUniqueId(), this.questSellShop)) {

      event.setCancelled(true, getApi().getTextManager().of(event.getPurchaser(), "addon.quests.sell-quest-required", this.questSellShop).forLocale());
    }

    if(!event.getShop().shopType().isBuying() && !event.getShop().shopType().isTradingBlocked() && this.questRequiredBuy
       && !isQuestComplete(event.getPurchaser().getUniqueId(), this.questBuyShop)) {

      event.setCancelled(true, getApi().getTextManager().of(event.getPurchaser(), "addon.quests.buy-quest-required", this.questBuyShop).forLocale());
    }
  }

  private boolean isQuestComplete(final UUID id, final String questName) {

    final Quester quester = plugin.getQuester(id);
    if(quester == null) {

      return false;
    }

    return quester.getCompletedQuests().stream().anyMatch(q -> q.getName().equalsIgnoreCase(questName));
  }
}