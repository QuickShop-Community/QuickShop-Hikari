package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.util.ShopUtil;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class SubCommand_Price implements CommandHandler<Player> {

    private final QuickShop plugin;

    public SubCommand_Price(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        if (parser.getArgs().isEmpty()) {
            plugin.text().of(sender, "no-price-given").send();
            return;
        }

        final double price;

        try {
            price = Double.parseDouble(parser.getArgs().get(0));
        } catch (NumberFormatException ex) {
            // No number input
            Log.debug(ex.getMessage());
            plugin.text().of(sender, "not-a-number", parser.getArgs().get(0)).send();
            return;
        }
        // No number input
        if (Double.isInfinite(price) || Double.isNaN(price)) {
            plugin.text().of(sender, "not-a-number", parser.getArgs().get(0)).send();
            return;
        }
        final Shop shop = getLookingShop(sender);
        if (shop == null) {
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
        }

        ShopUtil.setPrice(plugin, QUserImpl.createFullFilled(sender), price, shop);
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
            @NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        return parser.getArgs().size() == 1 ? Collections.singletonList(plugin.text().of(sender, "tabcomplete.price").plain()) : Collections.emptyList();
    }

}
