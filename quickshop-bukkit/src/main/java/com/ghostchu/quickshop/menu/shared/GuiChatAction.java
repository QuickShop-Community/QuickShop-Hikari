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

import net.tnemc.menu.core.handlers.MenuClickHandler;
import net.tnemc.menu.core.icon.action.ActionType;
import net.tnemc.menu.core.icon.action.IconAction;
import net.tnemc.menu.core.viewer.MenuViewer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

/**
 * A custom icon action that properly handles chat input by using QuickShop's
 * GuiChatInputManager, which cancels the chat event so messages don't appear
 * in public chat. Automatically re-opens the menu after input is processed.
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class GuiChatAction extends IconAction {

  private final Function<String, Boolean> handler;
  private final String prompt;
  private final boolean reopenMenu;

  /**
   * Creates a new GuiChatAction with default ANY action type.
   * Will attempt to re-open the current menu after input.
   *
   * @param handler The handler function. Returns true if input is accepted (stop listening),
   *                false to keep waiting for more input.
   * @param prompt  The prompt message to send to the player (supports legacy color codes)
   */
  public GuiChatAction(@NotNull final Function<String, Boolean> handler, 
                       @Nullable final String prompt) {
    this(handler, prompt, true, ActionType.ANY);
  }

  /**
   * Creates a new GuiChatAction with specified action type.
   * Will attempt to re-open the current menu after input.
   *
   * @param handler    The handler function
   * @param prompt     The prompt message
   * @param actionType The action type that triggers this action
   */
  public GuiChatAction(@NotNull final Function<String, Boolean> handler,
                       @Nullable final String prompt,
                       @NotNull final ActionType actionType) {
    this(handler, prompt, true, actionType);
  }

  /**
   * Creates a new GuiChatAction with control over menu re-opening.
   *
   * @param handler    The handler function
   * @param prompt     The prompt message
   * @param reopenMenu Whether to re-open the menu after input is accepted
   * @param actionType The action type that triggers this action
   */
  public GuiChatAction(@NotNull final Function<String, Boolean> handler,
                       @Nullable final String prompt,
                       final boolean reopenMenu,
                       @NotNull final ActionType actionType) {
    super(actionType);
    this.handler = handler;
    this.prompt = prompt;
    this.reopenMenu = reopenMenu;
  }

  /**
   * Creates a new GuiChatAction with control over menu re-opening (default ANY action type).
   *
   * @param handler    The handler function
   * @param prompt     The prompt message
   * @param reopenMenu Whether to re-open the menu after input is accepted
   */
  public GuiChatAction(@NotNull final Function<String, Boolean> handler,
                       @Nullable final String prompt,
                       final boolean reopenMenu) {
    this(handler, prompt, reopenMenu, ActionType.ANY);
  }

  @Override
  public boolean onClick(final MenuClickHandler clickHandler) {
    final Player bukkitPlayer = Bukkit.getPlayer(clickHandler.player().identifier());
    if(bukkitPlayer == null) {
      return false;
    }

    // Get current menu context for re-opening (only if reopenMenu is true)
    String currentMenu = null;
    int currentPage = 1;
    
    if(reopenMenu) {
      final Optional<MenuViewer> viewer = clickHandler.player().viewer();
      if(viewer.isPresent()) {
        currentMenu = viewer.get().menu();
        currentPage = viewer.get().page();
      }
    }

    // Register with our chat input manager (which properly cancels chat events)
    // Note: We don't set AWAITING_CHAT status because our listener handles chat at LOWEST priority
    // and cancels the event before TNMS's listener can see it. Setting AWAITING_CHAT without
    // setting a chatHandler would cause NPE in TNMS's chat listener.
    // Pass menu context for re-opening after input
    GuiChatInputManager.getInstance().requestInput(
            bukkitPlayer, 
            handler, 
            prompt,
            currentMenu,
            currentPage
    );
    
    // Close the inventory so player can type in chat
    clickHandler.player().inventory().close();
    
    return true;
  }
}
