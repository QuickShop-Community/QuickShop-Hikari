package com.ghostchu.quickshop.listener;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Info;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopManager;
import com.ghostchu.quickshop.api.shop.interaction.InteractionBehavior;
import com.ghostchu.quickshop.api.shop.interaction.InteractionClick;
import com.ghostchu.quickshop.api.shop.interaction.InteractionType;
import com.ghostchu.quickshop.shop.datatype.ShopSignPersistentDataType;
import com.ghostchu.quickshop.util.ExpiringSet;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.PackageUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.ReloadStatus;
import net.tnemc.menu.core.manager.MenuManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLocaleChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerListener extends AbstractQSListener {

  private final ExpiringSet<UUID> adventureWorkaround = new ExpiringSet<>(1, TimeUnit.SECONDS);
  private final ExpiringSet<UUID> rateLimit = new ExpiringSet<>(125, TimeUnit.MILLISECONDS);

  public PlayerListener(final QuickShop plugin) {

    super(plugin);
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onAdventureClick(final PlayerAnimationEvent event) {

    if(event.getPlayer().getGameMode() != GameMode.ADVENTURE) {
      return;
    }
    // ----Adventure dupe click workaround start----
    if(adventureWorkaround.contains(event.getPlayer().getUniqueId())) {
      return;
    }
    adventureWorkaround.add(event.getPlayer().getUniqueId());
    // ----Adventure dupe click workaround end----
    final Block focused = event.getPlayer().getTargetBlockExact(5);
    if(focused != null) {
      final PlayerInteractEvent interactEvent
              = new PlayerInteractEvent(event.getPlayer(),
                                        focused.getType() == Material.AIR? Action.LEFT_CLICK_AIR : Action.LEFT_CLICK_BLOCK,
                                        event.getPlayer().getInventory().getItemInMainHand(),
                                        focused,
                                        event.getPlayer().getFacing().getOppositeFace());
      onClick(interactEvent);
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onClick(final PlayerInteractEvent event) {
    // Deprecated: Can use useInteractedBlock() == Result.DENY instead
    if(event.isCancelled() && plugin.getConfig().getBoolean("shop.ignore-cancelled-interact-event", true)) {
      return;
    }
    if(event.getHand() != EquipmentSlot.HAND) {
      return;
    }

    // ----Adventure dupe click workaround start----
    if(event.getPlayer().getGameMode() == GameMode.ADVENTURE) {
      if(!adventureWorkaround.contains(event.getPlayer().getUniqueId())) {
        return;
      }
      adventureWorkaround.add(event.getPlayer().getUniqueId());
    }
    // ----Adventure dupe click workaround end----
    if(rateLimit.contains(event.getPlayer().getUniqueId())) {
      return;
    }
    rateLimit.add(event.getPlayer().getUniqueId());

    final Map.Entry<Shop, InteractionClick> shopSearched = searchShop(event.getClickedBlock(), event.getPlayer());
    if(shopSearched.getValue() == null) {
      Log.debug("Interaction: shopSearched value == null");
      return;
    }

    final Optional<InteractionType> interactionType = plugin.getInteractionManager().interaction(event, shopSearched.getValue());
    if(interactionType.isEmpty()) {
      Log.debug("Interaction: InteractionType is empty");
      return;
    }

    final Optional<InteractionBehavior> behavior = plugin.getInteractionManager().behavior(interactionType.get());
    if(behavior.isEmpty()) {
      Log.debug("Interaction: InteractionBehavior is empty");
      return;
    }

    if(Util.isDevMode()) {
      Log.debug("Click: " + interactionType.get().identifier());
      Log.debug("Behavior Mapping: " + behavior.get().identifier());
    }

    behavior.get().handle(plugin, shopSearched.getKey(), event.getPlayer(), event, shopSearched.getValue(), interactionType.get());
  }

  @NotNull
  public Map.Entry<@Nullable Shop, @NotNull InteractionClick> searchShop(@Nullable final Block b, @NotNull final Player p) {

    if(b == null) {
      return new AbstractMap.SimpleEntry<>(null, InteractionClick.AIR);
    }

    Shop shop = plugin.getShopManager().getShop(b.getLocation());

    // If that wasn't a shop, search nearby shops
    if(shop == null) {

      final Block attached;
      if(Util.isWallSign(b.getType())) {

        attached = Util.getAttached(b);
        if(attached != null) {

          shop = plugin.getShopManager().getShop(attached.getLocation());
          return new AbstractMap.SimpleImmutableEntry<>(shop, InteractionClick.SIGN);
        }
      } else if(Util.isDoubleChest(b.getBlockData())) {

        attached = Util.getSecondHalf(b);
        if(attached != null) {

          final Shop secondHalfShop = plugin.getShopManager().getShop(attached.getLocation());
          if(secondHalfShop != null && !p.getUniqueId().equals(secondHalfShop.getOwner().getUniqueId())) {
            // If player not the owner of the shop, make him select the second half of the
            // shop
            // Otherwise owner will be able to create new double chest shop
            shop = secondHalfShop;
          }
        }
      }
    }

    if(shop == null && b.getState(false) instanceof Container) {

      return new AbstractMap.SimpleImmutableEntry<>(shop, InteractionClick.CONTAINER);
    }
    return new AbstractMap.SimpleImmutableEntry<>(shop, InteractionClick.SHOPBLOCK);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onDyeing(final PlayerInteractEvent e) {

    if(e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem() == null || !Util.isDyes(e.getItem().getType())) {
      return;
    }
    final Block block = e.getClickedBlock();
    if(block == null || !Util.isWallSign(block.getType())) {
      return;
    }
    final Block attachedBlock = Util.getAttached(block);
    if(attachedBlock == null || plugin.getShopManager().getShopIncludeAttached(attachedBlock.getLocation()) == null) {
      return;
    }
    e.setCancelled(true);
    Log.debug("Disallow " + e.getPlayer().getName() + " dye the shop sign.");
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onSignEditing(final SignChangeEvent e) {

    final Block block = e.getBlock();
    if(!Util.isWallSign(block.getType())) {
      return;
    }
    final BlockState state = e.getBlock().getState(false);
    if(state instanceof final Sign sign) {
      if(sign.getPersistentDataContainer().has(Shop.SHOP_NAMESPACED_KEY, ShopSignPersistentDataType.INSTANCE)) {
        e.setCancelled(true);
        Log.debug("Disallow " + e.getPlayer().getName() + " editing the shop sign.");
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onInventoryClose(final InventoryCloseEvent e) {

    final UUID id = e.getPlayer().getUniqueId();

    if(MenuManager.instance().inMenu(id) || !QuickShop.inShop.contains(id)) {
      return;
    }

    try {
      final Location location = e.getInventory().getLocation();
      if(location == null) {
        return; /// ignored as workaround, GH-303
      }
      //Cannot tick distance manager while unloading playerchunks
      //https://github.com/pl3xgaming/Purpur/issues/631
      if(!Util.isLoaded(location)) {
        return;
      }
      final Shop shop = plugin.getShopManager().getShopIncludeAttached(location);
      if(shop != null) {
        shop.setSignText(plugin.text().findRelativeLanguages(e.getPlayer()));
      }
    } catch(final NullPointerException ignored) {
    }

    QuickShop.inShop.remove(id);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onJoin(final PlayerLocaleChangeEvent e) {

    Log.debug("Player " + e.getPlayer().getName() + " using new locale " + e.getLocale() + ": " + plugin.text().of(e.getPlayer(), "file-test").plain(e.getLocale()));
    plugin.getDatabaseHelper().updatePlayerProfile(e.getPlayer().getUniqueId(), e.getLocale(), e.getPlayer().getName())
            .exceptionally(throwable->{
              Log.debug("Failed to set player locale: " + throwable.getMessage());
              return null;
            });
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onJoin(final PlayerJoinEvent e) {

    plugin.getPlayerFinder().cache(e.getPlayer().getUniqueId(), e.getPlayer().getName());
    // Notify the player any messages they were sent
    if(plugin.getConfig().getBoolean("shop.auto-fetch-shop-messages")) {
      final long delay = plugin.getConfig().getLong("shop.join-flush-delay", 60L);
      QuickShop.folia().getScheduler().runLaterAsync(()->MsgUtil.flush(e.getPlayer()), delay);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onJoinEasterEgg(final PlayerJoinEvent e) {

    if(plugin.perm().hasPermission(e.getPlayer(), "quickshop.alerts")) {
      final Date date = new Date();
      final LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      if((localDate.getMonthValue() == 4 && localDate.getDayOfMonth() == 1) || PackageUtil.parsePackageProperly("april-rickandroll").asBoolean()) {
        QuickShop.folia().getScheduler().runLater((()->plugin.text().of(e.getPlayer(), "april-rick-and-roll-easter-egg").send()), 80L);
      }
    }
  }


  /*
   * Cancels the menu for broken shop block
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerBreakShopCreationChest(final BlockBreakEvent event) {

    @Nullable final Player player = event.getPlayer();
    if(player == null) {
      return;
    }
    final ShopManager.InteractiveManager actionMap = plugin.getShopManager().getInteractiveManager();
    final Info info = actionMap.get(player.getUniqueId());
    if(info != null && info.getLocation().equals(event.getBlock().getLocation())) {
      actionMap.remove(player.getUniqueId());
      if(info.getAction().isTrading()) {
        plugin.text().of(player, "shop-purchase-cancelled").send();
      } else if(info.getAction().isCreating()) {
        plugin.text().of(player, "shop-creation-cancelled").send();
      }
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerQuit(final PlayerQuitEvent e) {
    // Remove them from the menu
    plugin.getShopManager().getInteractiveManager().remove(e.getPlayer().getUniqueId());
    plugin.getDatabaseHelper().updatePlayerProfile(e.getPlayer().getUniqueId(), e.getPlayer().getLocale(), e.getPlayer().getName())
            .exceptionally(throwable->{
              Log.debug("Failed to set player locale: " + throwable.getMessage());
              return null;
            });
  }

  @EventHandler(ignoreCancelled = true)
  public void onTeleport(final PlayerTeleportEvent e) {

    onMove(new PlayerMoveEvent(e.getPlayer(), e.getFrom(), e.getTo()));
  }

  /*
   * Waits for a player to move too far from a shop, then cancels the menu.
   */
  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onMove(final PlayerMoveEvent e) {

    final Info info = plugin.getShopManager().getInteractiveManager().get(e.getPlayer().getUniqueId());
    if(info == null) {
      return;
    }
    final Player p = e.getPlayer();
    final Location loc1 = info.getLocation();
    final Location loc2 = p.getLocation();
    if(loc1.getWorld() != loc2.getWorld() || loc1.distanceSquared(loc2) > 25) {
      if(info.getAction().isTrading()) {
        plugin.text().of(p, "shop-purchase-cancelled").send();
      } else if(info.getAction().isCreating()) {
        plugin.text().of(p, "shop-creation-cancelled").send();
      }
      plugin.getShopManager().getInteractiveManager().remove(p.getUniqueId());
    }
  }

  /**
   * Callback for reloading
   *
   * @return Reloading success
   */
  @Override
  public ReloadResult reloadModule() {

    return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
  }

  enum ClickType {
    SHOPBLOCK,
    SIGN,
    AIR
  }
}
