package org.maxgamer.quickshop.shop;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.reload.Reloadable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class InteractionController implements Reloadable {

    private final QuickShop plugin;
    private final Map<Interaction, InteractionBehavior> behaviorMap = new HashMap<>();

    public InteractionController(@NotNull QuickShop plugin){
        this.plugin = plugin;
        loadInteractionConfig();
        plugin.getReloadManager().register(this);
    }

    public void loadInteractionConfig(){
        File configFile = new File(plugin.getDataFolder(),"interaction.yml");
        if(!configFile.exists()){
            try {
                //noinspection ConstantConditions
                Files.copy(plugin.getResource("interaction.yml"), configFile.toPath());
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING,"Failed to copy interaction.yml to plugin folder!",e);
            }
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        behaviorMap.clear();
        for (Interaction value : Interaction.values()) {
            try {
                behaviorMap.put(value, InteractionBehavior.valueOf(config.getString(value.name())));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.WARNING,"Failed to load interaction behavior for " + value.name() + "! Using NONE behavior!");
                behaviorMap.put(value, InteractionBehavior.NONE);
            }
        }
    }

    /**
     * Getting the behavior of the interaction
     * @param interaction the interaction
     * @return the behavior
     */
    @NotNull
    public InteractionBehavior getBehavior(@NotNull Interaction interaction) {
        return behaviorMap.getOrDefault(interaction, InteractionBehavior.NONE);
    }

    enum Interaction{
        STANDING_LEFT_CLICK_SIGN,
        STANDING_RIGHT_CLICK_SIGN,
        STANDING_LEFT_CLICK_SHOPBLOCK,
        STANDING_RIGHT_CLICK_SHOPBLOCK,
        SNEAKING_LEFT_CLICK_SIGN,
        SNEAKING_RIGHT_CLICK_SIGN,
        SNEAKING_LEFT_CLICK_SHOPBLOCK,
        SNEAKING_RIGHT_CLICK_SHOPBLOCK
    }
    /**
     * The shop that this controller is controlling
     * In normal interaction trade, BUY and SELL have same behavior which ask the user what they want to do.
     * In direct trade mode, BUY will use execute buying directly and SELL will use execute selling directly.
     * In direct trade case, the user will not be asked anything.
     *
     * CONTROL_PANEL will show up a Shop Control Panel which will allow the user to change the shop's settings.
     */
    enum InteractionBehavior {
        BUY,
        SELL,
        CONTROL_PANEL,
        NONE
    }
}
