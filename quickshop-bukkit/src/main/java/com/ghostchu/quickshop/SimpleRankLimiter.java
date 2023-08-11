package com.ghostchu.quickshop;

import com.ghostchu.quickshop.api.RankLimiter;
import com.ghostchu.quickshop.common.obj.QUser;
import com.ghostchu.quickshop.util.paste.item.SubPasteItem;
import com.ghostchu.quickshop.util.paste.util.HTMLTable;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.Reloadable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SimpleRankLimiter implements Reloadable, RankLimiter, SubPasteItem {
    private final QuickShop plugin;
    private final Map<String, Integer> limits = new HashMap<>();
    /**
     * Whether or not to limit players shop amounts
     */
    private boolean limit = false;
    private int def = 0;

    public SimpleRankLimiter(QuickShop plugin) {
        this.plugin = plugin;
        plugin.getReloadManager().register(this);
        plugin.getPasteManager().register(plugin.getJavaPlugin(), this);
        load();
    }

    private void load() {
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(new File(this.plugin.getDataFolder(), "config.yml"));
        ConfigurationSection limitCfg = yamlConfiguration.getConfigurationSection("limits");
        if (limitCfg != null) {
            this.limit = limitCfg.getBoolean("use", false);
            def = limitCfg.getInt("default");
            limitCfg = limitCfg.getConfigurationSection("ranks");
            for (String key : Objects.requireNonNull(limitCfg).getKeys(true)) {
                limits.put(key, limitCfg.getInt(key));
            }
        } else {
            this.limit = false;
            limits.clear();
        }
    }

    /**
     * Get the Player's Shop limit.
     *
     * @param p The player you want get limit.
     * @return int Player's shop limit
     */
    @Override
    public int getShopLimit(@NotNull QUser p) {
        int count = def;
        for (Map.Entry<String, Integer> entry : limits.entrySet()) {
            if ((entry.getValue() > count) && plugin.perm().hasPermission(p, entry.getKey())) {
                count = entry.getValue();
            }
        }
        return count;
    }

    @SuppressWarnings("removal")
    @Override
    @Deprecated(forRemoval = true)
    @ApiStatus.Internal
    @ApiStatus.Obsolete
    @NotNull
    public Map<String, Integer> getLimits() {
        return limits;
    }

    @Override
    public boolean isLimit() {
        return limit;
    }

    @Override
    public ReloadResult reloadModule() throws Exception {
        load();
        return Reloadable.super.reloadModule();
    }

    @Override
    public @NotNull String genBody() {
        HTMLTable table = new HTMLTable(2);
        table.setTableTitle("Permission", "Amount");
        for (Map.Entry<String, Integer> entry : limits.entrySet()) {
            table.insert(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return table.render();
    }

    @Override
    public @NotNull String getTitle() {
        return "Rank Limiter";
    }
}
