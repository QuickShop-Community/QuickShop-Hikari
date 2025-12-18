package com.ghostchu.quickshop.addon.betonquest;

import com.ghostchu.quickshop.api.event.economy.ShopPurchaseEvent;
import com.ghostchu.quickshop.api.event.management.ShopCreateEvent;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.util.logger.Log;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.profiles.Profile;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

import java.util.UUID;

public final class Main extends CompatibilityModule {

  private BetonQuest plugin;
  private boolean questRequiredCreate = false;
  private boolean questRequiredBuy = false;
  private boolean questRequiredSell = false;

  //Our quest names
  private String questCreateShop = "";
  private String questBuyShop = "";
  private String questSellShop = "";

  @Override
  public void init() {

    if(plugin == null) {

      Log.debug("Unable to find BetonQuest. Disabling QuickShop-BetonQuest Addon.");
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
  }

  @EventHandler(ignoreCancelled = true)
  public void onPreCreation(final ShopCreateEvent event) {

    if(!event.phase().cancellable() || !this.questRequiredCreate) {

      return;
    }

    if(!isQuestComplete(event.user().getUniqueId(), this.questCreateShop, ">")) {

      event.setCancelled(true, getApi().getTextManager().of(event.user(), "addon.quests.create-quest-required", this.questCreateShop).forLocale());
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onTrading(final ShopPurchaseEvent event) {

    if(event.getShop().shopType().isBuying() && this.questRequiredSell
       && !isQuestComplete(event.getPurchaser().getUniqueId(), this.questSellShop, ">")) {

      event.setCancelled(true, getApi().getTextManager().of(event.getPurchaser(), "addon.quests.sell-quest-required", this.questSellShop).forLocale());
    }

    if(!event.getShop().shopType().isBuying() && !event.getShop().shopType().isTradingBlocked() && this.questRequiredBuy
       && !isQuestComplete(event.getPurchaser().getUniqueId(), this.questBuyShop, ">")) {

      event.setCancelled(true, getApi().getTextManager().of(event.getPurchaser(), "addon.quests.buy-quest-required", this.questBuyShop).forLocale());
    }
  }

  private boolean isQuestComplete(final UUID id, final String questTag, final String separator) {

    final Profile profile = plugin.getProfileProvider().getProfile(id);
    if(profile == null) {

      return false;
    }

    return plugin.getPlayerDataStorage().get(profile).getTags().stream().anyMatch(t -> {

      if(t.contains(separator)) {

        final String[] split = t.split(separator);

        return split[0].equalsIgnoreCase(questTag) && split[1].equalsIgnoreCase("finished");
      }

      return false;
    });
  }
}