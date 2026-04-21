package com.ghostchu.quickshop.shop.interaction;
/*
 * QuickShop-Hikari
 * Copyright (C) 2025 Daniel "creatorfromhell" Vidmar
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
import com.ghostchu.quickshop.api.shop.interaction.InteractionBehavior;
import com.ghostchu.quickshop.api.shop.interaction.InteractionClick;
import com.ghostchu.quickshop.api.shop.interaction.InteractionManager;
import com.ghostchu.quickshop.api.shop.interaction.InteractionType;
import com.ghostchu.quickshop.config.InteractionConfig;
import com.ghostchu.quickshop.shop.interaction.behaviors.ControlPanel;
import com.ghostchu.quickshop.shop.interaction.behaviors.ControlPanelUI;
import com.ghostchu.quickshop.shop.interaction.behaviors.TradeDirect;
import com.ghostchu.quickshop.shop.interaction.behaviors.TradeDirectAll;
import com.ghostchu.quickshop.shop.interaction.behaviors.TradeInteraction;
import com.ghostchu.quickshop.shop.interaction.behaviors.TradeUI;
import com.ghostchu.quickshop.shop.interaction.interactions.SneakingLeftClickContainer;
import com.ghostchu.quickshop.shop.interaction.interactions.SneakingLeftClickShop;
import com.ghostchu.quickshop.shop.interaction.interactions.SneakingLeftClickSign;
import com.ghostchu.quickshop.shop.interaction.interactions.SneakingRightClickContainer;
import com.ghostchu.quickshop.shop.interaction.interactions.SneakingRightClickShop;
import com.ghostchu.quickshop.shop.interaction.interactions.SneakingRightClickSign;
import com.ghostchu.quickshop.shop.interaction.interactions.StandingLeftClickContainer;
import com.ghostchu.quickshop.shop.interaction.interactions.StandingLeftClickShop;
import com.ghostchu.quickshop.shop.interaction.interactions.StandingLeftClickSign;
import com.ghostchu.quickshop.shop.interaction.interactions.StandingRightClickContainer;
import com.ghostchu.quickshop.shop.interaction.interactions.StandingRightClickShop;
import com.ghostchu.quickshop.shop.interaction.interactions.StandingRightClickSign;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.paste.item.SubPasteItem;
import com.ghostchu.quickshop.util.paste.util.HTMLTable;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.Reloadable;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * InteractionManager
 *
 * @author creatorfromhell
 * @since 6.2.0.11
 */
public class QuickShopInteractionManager implements InteractionManager, Reloadable, SubPasteItem {

  private final Map<String, InteractionType> interactions = new HashMap<>();
  private final Map<String, InteractionBehavior> behaviors = new HashMap<>();
  private final Map<String, String> behaviorMapping = new HashMap<>();

  private final QuickShop plugin;

  public QuickShopInteractionManager(@NotNull final QuickShop plugin) {

    this.plugin = plugin;
    loadDefaults();
    loadConfig();
    plugin.getReloadManager().register(this);
    plugin.getPasteManager().register(plugin.getJavaPlugin(), this);
  }

  public void loadDefaults() {

    //Container Interactions
    interaction(new SneakingLeftClickContainer());
    interaction(new SneakingRightClickContainer());
    interaction(new StandingLeftClickContainer());
    interaction(new StandingRightClickContainer());
    
    //Shop Interactions
    interaction(new SneakingLeftClickShop());
    interaction(new SneakingRightClickShop());
    interaction(new StandingLeftClickShop());
    interaction(new StandingRightClickShop());
    
    //Sign Interactions
    interaction(new SneakingLeftClickSign());
    interaction(new SneakingRightClickSign());
    interaction(new StandingLeftClickSign());
    interaction(new StandingRightClickSign());

    behavior(new ControlPanel());
    behavior(new ControlPanelUI());
    behavior(new TradeDirect());
    behavior(new TradeDirectAll());
    behavior(new TradeInteraction());
    behavior(new TradeUI());
  }

  /**
   * Registers an InteractionBehavior in the InteractionManager.
   *
   * @param behavior the InteractionBehavior to be registered, not null.
   */
  public void behavior(final @NotNull InteractionBehavior behavior) {

    Log.debug("Behavior Added: " + behavior.identifier().toLowerCase(Locale.ROOT));
    behaviors.put(behavior.identifier().toLowerCase(Locale.ROOT), behavior);
  }

  /**
   * Retrieves the InteractionBehavior associated with the specified key from the
   * InteractionManager's internal Map.
   *
   * @param identifier a String key representing the identifier of the desired InteractionBehavior,
   *                   not null.
   *
   * @return an Optional containing the InteractionBehavior associated with the specified key if
   * found, or an empty Optional if no matching InteractionBehavior exists.
   */
  public Optional<InteractionBehavior> behavior(final @NotNull String identifier) {

    return Optional.ofNullable(behaviors.get(identifier.toLowerCase(Locale.ROOT)));
  }

  /**
   * Retrieves the InteractionBehavior that applies to the provided InteractionType.
   *
   * @param interaction The InteractionType to find behavior for, not null.
   *
   * @return an Optional containing the InteractionBehavior that applies to the provided
   * InteractionType if found, or an empty Optional if no matching InteractionBehavior exists.
   */
  public Optional<InteractionBehavior> behavior(final @NotNull InteractionType interaction) {

    if(behaviorMapping.containsKey(interaction.identifier().toLowerCase(Locale.ROOT))) {

      return Optional.ofNullable(behaviors.get(behaviorMapping.get(interaction.identifier().toLowerCase(Locale.ROOT))));
    }
    return Optional.empty();
  }

  /**
   * Checks if the provided key exists in the behaviors Map.
   *
   * @param identifier a String representing the key to be checked for existence in the behaviors
   *                   Map, not null.
   *
   * @return true if the key exists in the behaviors Map (case-insensitive), false otherwise.
   */
  public boolean hasBehavior(final @NotNull String identifier) {

    return behaviors.containsKey(identifier.toLowerCase(Locale.ROOT));
  }

  /**
   * Returns an unmodifiable collection of all InteractionBehaviors stored in this
   * InteractionManager.
   *
   * @return an unmodifiable collection of all InteractionBehaviors.
   */
  public Collection<InteractionBehavior> getBehaviors() {

    return Collections.unmodifiableCollection(behaviors.values());
  }

  /**
   * Adds an interaction to the InteractionManager.
   *
   * @param interaction the InteractionType to be added, not null.
   */
  public void interaction(final @NotNull InteractionType interaction) {

    Log.debug("InteractionType Added: " + interaction.identifier().toLowerCase(Locale.ROOT));
    interactions.put(interaction.identifier().toLowerCase(Locale.ROOT), interaction);
  }

  /**
   * Retrieves the InteractionType associated with the specified identifier.
   *
   * @param identifier the identifier of the desired InteractionType, not null.
   *
   * @return an Optional containing the InteractionType associated with the specified identifier if
   * found, or an empty Optional if no matching InteractionType exists.
   */
  public Optional<InteractionType> interaction(final @NotNull String identifier) {

    return Optional.ofNullable(interactions.get(identifier.toLowerCase(Locale.ROOT)));
  }

  public Optional<InteractionType> interaction(final @NotNull PlayerInteractEvent event, final @NotNull InteractionClick click) {

    for(final InteractionType interaction : interactions.values()) {

      if(interaction.applies(event, click)) {
        return Optional.of(interaction);
      }
    }
    return Optional.empty();
  }

  /**
   * Checks if an interaction with the specified identifier exists in the InteractionManager.
   *
   * @param identifier The identifier of the interaction to check for existence, not null.
   *
   * @return true if an interaction with the specified identifier exists in the InteractionManager
   * (case-insensitive), false otherwise.
   */
  public boolean hasInteraction(final @NotNull String identifier) {

    return interactions.containsKey(identifier.toLowerCase(Locale.ROOT));
  }

  /**
   * Retrieves the interactions that have been registered in the InteractionManager.
   *
   * @return an unmodifiable collection of all registered InteractionTypes.
   */
  public Collection<InteractionType> getInteractions() {

    return Collections.unmodifiableCollection(interactions.values());
  }

  @Override
  public ReloadResult reloadModule() throws Exception {

    loadConfig();
    return Reloadable.super.reloadModule();
  }

  public void loadConfig() {

    Log.debug("Interaction Config Loading.");
    final InteractionConfig config = new InteractionConfig();
    if(!config.load()) {

      plugin.logger().warn("Failed to copy interaction.yml to plugin folder!");
    }

    behaviorMapping.clear();

    for(final String interaction : interactions.keySet()) {

      final String behavior = config.getYaml().getString(interaction.toUpperCase(Locale.ROOT));
      if(behavior == null) {
        continue;
      }

      Log.debug("Behavior Mapper: " + interaction + " -> " + behavior);

      behaviorMapping.put(interaction.toLowerCase(Locale.ROOT), behavior.toLowerCase(Locale.ROOT));

      if(interaction.toUpperCase(Locale.ROOT).contains("RIGHT_CLICK_SHOPBLOCK") && !behavior.toUpperCase(Locale.ROOT).contains("NONE")) {
        plugin.logger().warn("Define a action for right shopblock clicking may prevent player from opening the shop container!");
      }
    }
  }

  /**
   * Generate and render the body part of this item
   *
   * @return the rendered body
   */
  @Override
  public @NotNull String genBody() {

    final HTMLTable table = new HTMLTable(2);
    table.setTableTitle("Interaction", "Behavior");
    for(final InteractionType interaction : interactions.values()) {

      final Optional<InteractionBehavior> behavior = behavior(interaction);
      final String behaviorID = (behavior.isEmpty())? "None" : behavior.get().identifier();

      table.insert(interaction.identifier(), behaviorID);
    }
    return table.render();
  }

  /**
   * Returns this item's title (plain text), and will render to HTML
   *
   * @return the title
   */
  @Override
  public @NotNull String getTitle() {

    return "Interaction Manager";
  }
}