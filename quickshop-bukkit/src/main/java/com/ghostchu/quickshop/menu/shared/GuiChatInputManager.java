package com.ghostchu.quickshop.menu.shared;
/*
 * QuickShop-Hikari
 * Copyright (C) 2024 Daniel "creatorfromhell" Vidmar
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
import com.ghostchu.quickshop.util.logger.Log;
import net.tnemc.menu.core.compatibility.MenuPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Manages chat input for GUI interactions.
 * This properly cancels chat events so messages don't appear in public chat.
 * Also handles re-opening the menu after input is processed.
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class GuiChatInputManager implements Listener {

  private static GuiChatInputManager instance;
  
  private final Map<UUID, ChatInputContext> pendingInputs = new ConcurrentHashMap<>();
  private final QuickShop plugin;
  private boolean registered = false;

  private GuiChatInputManager(@NotNull final QuickShop plugin) {
    this.plugin = plugin;
  }

  /**
   * Gets or creates the singleton instance.
   */
  @NotNull
  public static GuiChatInputManager getInstance() {
    if (instance == null) {
      instance = new GuiChatInputManager(QuickShop.getInstance());
    }
    return instance;
  }

  /**
   * Registers a chat input handler for a player with menu context for re-opening.
   * The handler will be called when the player sends a chat message.
   *
   * @param player   The player to listen for
   * @param handler  The handler function. Returns true if input is accepted (stop listening),
   *                 false to keep waiting for more input.
   * @param prompt   The prompt message to send to the player
   * @param menuName The menu name to re-open after input (e.g., "qs:keeper")
   * @param menuPage The menu page to re-open
   */
  public void requestInput(@NotNull final Player player, 
                           @NotNull final Function<String, Boolean> handler,
                           @Nullable final String prompt,
                           @Nullable final String menuName,
                           final int menuPage) {
    ensureRegistered();
    
    pendingInputs.put(player.getUniqueId(), new ChatInputContext(handler, menuName, menuPage));
    
    if (prompt != null && !prompt.isEmpty()) {
      player.sendMessage(prompt);
    }
    
    Log.debug("GuiChatInputManager: Registered input handler for player " + player.getName() + 
              " (menu: " + menuName + ", page: " + menuPage + ")");
  }

  /**
   * Registers a chat input handler for a player without menu context.
   * The menu will not be re-opened after input.
   *
   * @param player  The player to listen for
   * @param handler The handler function. Returns true if input is accepted (stop listening),
   *                false to keep waiting for more input.
   * @param prompt  The prompt message to send to the player
   */
  public void requestInput(@NotNull final Player player, 
                           @NotNull final Function<String, Boolean> handler,
                           @Nullable final String prompt) {
    requestInput(player, handler, prompt, null, 1);
  }

  /**
   * Cancels any pending input request for a player and optionally re-opens the menu.
   */
  public void cancelInput(@NotNull final UUID playerId, final boolean reopenMenu) {
    final ChatInputContext context = pendingInputs.remove(playerId);
    Log.debug("GuiChatInputManager: Cancelled input handler for player " + playerId);
    
    if (reopenMenu && context != null && context.menuName() != null) {
      reopenMenu(playerId, context);
    }
  }

  /**
   * Cancels any pending input request for a player without re-opening menu.
   */
  public void cancelInput(@NotNull final UUID playerId) {
    cancelInput(playerId, false);
  }

  /**
   * Checks if a player has a pending input request.
   */
  public boolean hasPendingInput(@NotNull final UUID playerId) {
    return pendingInputs.containsKey(playerId);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onChat(final AsyncPlayerChatEvent event) {
    final UUID playerId = event.getPlayer().getUniqueId();
    final ChatInputContext context = pendingInputs.get(playerId);
    
    if (context == null) {
      return;
    }
    
    // Cancel the event so it doesn't show in chat
    event.setCancelled(true);
    
    final String message = event.getMessage().trim();
    final Player eventPlayer = event.getPlayer();
    Log.debug("GuiChatInputManager: Received input from " + eventPlayer.getName() + ": " + message);
    
    // Process on the player's region thread for Folia compatibility
    QuickShop.folia().getScheduler().runAtEntityLater(eventPlayer, () -> {
      try {
        final boolean accepted = context.handler().apply(message);
        if (accepted) {
          pendingInputs.remove(playerId);
          Log.debug("GuiChatInputManager: Input accepted, removed handler for " + playerId);
          
          // Re-open the menu if configured
          if (context.menuName() != null) {
            reopenMenu(playerId, context);
          }
        }
      } catch (final Exception e) {
        plugin.logger().warn("Error processing GUI chat input for player " + playerId, e);
        pendingInputs.remove(playerId);
        
        // Re-open menu even on error
        if (context.menuName() != null) {
          reopenMenu(playerId, context);
        }
      }
    }, 1);
  }

  /**
   * Re-opens the menu for a player after chat input is processed.
   */
  private void reopenMenu(@NotNull final UUID playerId, @NotNull final ChatInputContext context) {
    final Player player = Bukkit.getPlayer(playerId);
    if (player == null || !player.isOnline()) {
      return;
    }
    
    Log.debug("GuiChatInputManager: Re-opening menu " + context.menuName() + 
              " page " + context.menuPage() + " for player " + player.getName());
    
    // Small delay on player's region thread to ensure everything is cleaned up before re-opening
    QuickShop.folia().getScheduler().runAtEntityLater(player, () -> {
      final Player onlinePlayer = Bukkit.getPlayer(playerId);
      if (onlinePlayer == null || !onlinePlayer.isOnline()) {
        return;
      }
      final MenuPlayer menuPlayer = QuickShop.getInstance().createMenuPlayer(onlinePlayer);
      menuPlayer.inventory().openMenu(menuPlayer, context.menuName(), context.menuPage());
    }, 2);
  }

  @EventHandler
  public void onPlayerQuit(final PlayerQuitEvent event) {
    pendingInputs.remove(event.getPlayer().getUniqueId());
  }

  private void ensureRegistered() {
    if (!registered) {
      Bukkit.getPluginManager().registerEvents(this, plugin.getJavaPlugin());
      registered = true;
      Log.debug("GuiChatInputManager: Registered event listener");
    }
  }

  /**
   * Unregisters the listener (call on plugin disable).
   */
  public void shutdown() {
    if (registered) {
      HandlerList.unregisterAll(this);
      registered = false;
    }
    pendingInputs.clear();
  }

  /**
   * Record to hold the chat input context including handler and menu info.
   */
  private record ChatInputContext(
          Function<String, Boolean> handler,
          @Nullable String menuName,
          int menuPage
  ) {}
}
