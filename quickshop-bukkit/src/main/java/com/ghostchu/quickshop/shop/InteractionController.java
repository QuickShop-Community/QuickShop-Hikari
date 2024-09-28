package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.paste.item.SubPasteItem;
import com.ghostchu.quickshop.util.paste.util.HTMLTable;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.Reloadable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.EnumMap;
import java.util.Map;

public class InteractionController implements Reloadable, SubPasteItem {

  private final QuickShop plugin;
  private final Map<Interaction, InteractionBehavior> behaviorMap = new EnumMap<>(Interaction.class);

  public InteractionController(@NotNull QuickShop plugin) {

    this.plugin = plugin;
    loadInteractionConfig();
    plugin.getReloadManager().register(this);
    plugin.getPasteManager().register(plugin.getJavaPlugin(), this);
  }

  public void loadInteractionConfig() {

    final File configFile = new File(plugin.getDataFolder(), "interaction.yml");
    if(!configFile.exists()) {
      try {
        Files.copy(plugin.getJavaPlugin().getResource("interaction.yml"), configFile.toPath());
      } catch(IOException e) {
        plugin.logger().warn("Failed to copy interaction.yml to plugin folder!", e);
      }
    }
    final FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
    behaviorMap.clear();
    for(Interaction value : Interaction.values()) {
      try {
        behaviorMap.put(value, InteractionBehavior.valueOf(config.getString(value.name())));
        if(value.isRightLick() && value.isShopBlock() && InteractionBehavior.valueOf(config.getString(value.name())) != InteractionBehavior.NONE) {
          plugin.logger().warn("Define a action for right shopblock clicking may prevent player from opening the shop container!");
        }
      } catch(IllegalArgumentException e) {
        plugin.logger().warn("Failed to load interaction behavior for {}! Using NONE behavior!", value.name());
        behaviorMap.put(value, InteractionBehavior.NONE);
      }
    }
  }

  @Override
  public ReloadResult reloadModule() throws Exception {

    loadInteractionConfig();
    return Reloadable.super.reloadModule();
  }

  @Override
  public @NotNull String genBody() {

    HTMLTable table = new HTMLTable(2);
    table.setTableTitle("Interaction", "Behavior");
    for(Interaction interaction : Interaction.values()) {
      table.insert(interaction.name(), getBehavior(interaction).name());
    }
    return table.render();
  }

  /**
   * Getting the behavior of the interaction
   *
   * @param interaction the interaction
   *
   * @return the behavior
   */
  @NotNull
  public InteractionBehavior getBehavior(@NotNull Interaction interaction) {

    return behaviorMap.getOrDefault(interaction, InteractionBehavior.NONE);
  }

  @Override
  public @NotNull String getTitle() {

    return "Interaction Controller";
  }

  public enum Interaction {
    STANDING_LEFT_CLICK_SIGN,
    STANDING_RIGHT_CLICK_SIGN,
    STANDING_LEFT_CLICK_SHOPBLOCK,
    STANDING_RIGHT_CLICK_SHOPBLOCK,
    SNEAKING_LEFT_CLICK_SIGN,
    SNEAKING_RIGHT_CLICK_SIGN,
    SNEAKING_LEFT_CLICK_SHOPBLOCK,
    SNEAKING_RIGHT_CLICK_SHOPBLOCK;

    public boolean isLeftClick() {

      return this.name().contains("_LEFT_CLICK_");
    }

    public boolean isRightLick() {

      return this.name().contains("_RIGHT_CLICK_");
    }

    public boolean isShopBlock() {

      return this.name().endsWith("SHOPBLOCK");
    }

    public boolean isSign() {

      return this.name().endsWith("SIGN");
    }

    public boolean isSneaking() {

      return this.name().startsWith("SNEAKING");
    }
  }

  /**
   * The shop that this controller is controlling In normal interaction trade, BUY and SELL have
   * same behavior which ask the user what they want to do. In direct trade mode, BUY will use
   * execute buying directly and SELL will use execute selling directly. In direct trade case, the
   * user will not be asked anything.
   * <p>
   * CONTROL_PANEL will show up a Shop Control Panel which will allow the user to change the shop's
   * settings.
   */
  public enum InteractionBehavior {
    TRADE_INTERACTION,
    TRADE_UI,
    TRADE_DIRECT,
    TRADE_DIRECT_ALL,
    CONTROL_PANEL,
    CONTROL_PANEL_UI,
    NONE
  }
}
