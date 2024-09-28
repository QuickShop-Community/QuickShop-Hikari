package com.ghostchu.quickshop.compatibility.bentobox;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.event.ShopAuthorizeCalculateEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.compatibility.CompatibilityModule;
import com.ghostchu.quickshop.obj.QUserImpl;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.events.island.IslandDeletedEvent;
import world.bentobox.bentobox.api.events.island.IslandResettedEvent;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.database.objects.IslandDeletion;

import java.util.List;

public final class Main extends CompatibilityModule implements Listener {

  private boolean deleteShopOnLeave;
  private boolean deleteShopOnReset;

  @Override
  public void init() {

    deleteShopOnLeave = getConfig().getBoolean("delete-shop-on-member-leave");
    deleteShopOnReset = getConfig().getBoolean("delete-shop-on-island-reset");
  }

  @EventHandler(ignoreCancelled = true)
  public void onIslandDeleted(IslandDeletedEvent event) {

    if(!deleteShopOnReset) {
      return;
    }
    getShops(event.getDeletedIslandInfo()).forEach(shop->{
      recordDeletion(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "BentoBox", false), shop, "Island " + event.getIsland().getName() + " was deleted");
      getApi().getShopManager().deleteShop(shop);
    });
  }

  private List<Shop> getShops(IslandDeletion island) {

    return getShops(island.getWorld().getName(), island.getMinX(), island.getMinZ(), island.getMaxX(), island.getMaxZ());
  }

  @EventHandler(ignoreCancelled = true)
  public void onIslandKick(world.bentobox.bentobox.api.events.team.TeamKickEvent event) {

    if(!deleteShopOnLeave) {
      return;
    }
    getShops(event.getIsland()).forEach(shop->{
      if(event.getPlayerUUID().equals(shop.getOwner().getUniqueId())) {
        recordDeletion(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "BentoBox", false), shop, "Player " + event.getPlayerUUID() + " was kicked from the island");
        getApi().getShopManager().deleteShop(shop);
      }
    });
  }

  private List<Shop> getShops(Island island) {

    return getShops(island.getWorld().getName(), island.getMinX(), island.getMinZ(), island.getMaxX(), island.getMaxZ());
  }

  @EventHandler(ignoreCancelled = true)
  public void onIslandLeave(world.bentobox.bentobox.api.events.team.TeamLeaveEvent event) {

    if(!deleteShopOnLeave) {
      return;
    }
    getShops(event.getIsland()).forEach(shop->{
      if(event.getPlayerUUID().equals(shop.getOwner().getUniqueId())) {
        recordDeletion(null, shop, "Player " + event.getPlayerUUID() + " was leaved from the island");
        getApi().getShopManager().deleteShop(shop);
      }
    });
  }

  @EventHandler(ignoreCancelled = true)
  public void onIslandResetted(IslandResettedEvent event) {

    if(!deleteShopOnReset) {
      return;
    }
    getShops(event.getOldIsland()).forEach(shop->{
      recordDeletion(QUserImpl.createFullFilled(CommonUtil.getNilUniqueId(), "BentoBox", false), shop, "Island " + event.getIsland().getName() + " was resetted");
      getApi().getShopManager().deleteShop(shop);
    });
  }

  @EventHandler(ignoreCancelled = true)
  public void permissionOverride(ShopAuthorizeCalculateEvent event) {

    Location shopLoc = event.getShop().getLocation();
    BentoBox.getInstance().getIslandsManager().getIslandAt(shopLoc).ifPresent(island->{
      if(event.getAuthorizer().equals(island.getOwner())) {
        if(event.getNamespace().equals(QuickShop.getInstance().getJavaPlugin()) && event.getPermission().equals(BuiltInShopPermission.DELETE.getRawNode())) {
          event.setResult(true);
        }
      }
    });
  }
}
