package org.maxgamer.quickshop.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.Util;

import java.util.UUID;

public class QuickShopPAPI extends PlaceholderExpansion {
    private QuickShop plugin;

    @Override
    public boolean canRegister() {
        this.plugin = QuickShop.getInstance();
        return true;
    }

    /**
     * The placeholder identifier of this expansion. May not contain {@literal %},
     * {@literal {}} or _
     *
     * @return placeholder identifier that is associated with this expansion
     */
    @Override
    public @NotNull String getIdentifier() {
        return "qs";
    }

    /**
     * The author of this expansion
     *
     * @return name of the author for this expansion
     */
    @Override
    public @NotNull String getAuthor() {
        return "QuickShopBundled";
    }

    /**
     * The version of this expansion
     *
     * @return current version of this expansion
     */
    @Override
    public @NotNull String getVersion() {
        return QuickShop.getVersion();
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        String[] args = params.split("_");
        if (args.length < 1) {
            return null;
        }
        switch (args[0]) {
            case "server-total" -> {
                return String.valueOf(plugin.getShopManager().getAllShops().size());
            }
            case "server-loaded" -> {
                return String.valueOf(plugin.getShopManager().getLoadedShops().size());
            }
            case "default-currency" -> {
                return String.valueOf(plugin.getCurrency());
            }
            case "player" -> {
                if (args.length >= 3) {
                    if (Util.isUUID(args[1])) {
                        UUID uuid = UUID.fromString(args[1]);
                        //noinspection SwitchStatementWithTooFewBranches
                        switch (args[2]) {
                            case "count" -> {
                                return String.valueOf(plugin.getShopManager().getPlayerAllShops(uuid).size());
                            }
                        }
                    }
                }
                return null;
            }
        }

        return super.onRequest(player, params);
    }
}
