package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.Util;
import net.tnemc.menu.bukkit.BukkitPlayer;
import net.tnemc.menu.core.compatibility.MenuPlayer;
import net.tnemc.menu.core.manager.MenuManager;
import net.tnemc.menu.core.viewer.MenuViewer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.ghostchu.quickshop.menu.ShopBrowseMenu.SHOPS_DATA;


public class SubCommand_Browse implements CommandHandler<Player> {

    private final QuickShop plugin;

    public SubCommand_Browse(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {

        final MenuViewer viewer = new MenuViewer(sender.getUniqueId());
        MenuManager.instance().addViewer(viewer);

        final MenuPlayer menuPlayer = new BukkitPlayer(sender, QuickShop.getInstance().getJavaPlugin());

        final boolean world = (!parser.getArgs().isEmpty() && parser.getArgs().get(0).equalsIgnoreCase("world"));

        Util.asyncThreadRun(()->{
            final List<Shop> shops = new ArrayList<>();

            if(world) {
                shops.addAll(plugin.getShopManager().getAllShops().stream().filter(shop -> {
                    if(shop.getLocation().getWorld() == null
                            || sender.getLocation().getWorld() == null) {
                        return false;
                    }
                    return shop.getLocation().getWorld().getUID().equals(sender.getLocation().getWorld().getUID());
                }).toList());
            } else {
                shops.addAll(plugin.getShopManager().getAllShops());
            }

            viewer.addData(SHOPS_DATA, shops);
            MenuManager.instance().open("qs:browse", 1, menuPlayer);
        });
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        if (parser.getArgs().size() == 1) {
            return List.of("world", plugin.text().of(sender, "browse-command-leave-blank").plain());
        }
        return Collections.emptyList();
    }
}
