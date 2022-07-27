package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.util.ItemMarker;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@AllArgsConstructor
public class SubCommand_Lookup implements CommandHandler<Player> {
    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        ItemStack item = sender.getInventory().getItemInMainHand();
        if (cmdArg.length < 1) {
            plugin.text().of(sender, "command-incorrect", "/qs lookup <create/remove/test> <name>").send();
            return;
        }

        if (cmdArg.length == 1) {
            if ("test".equals(cmdArg[0].toLowerCase(Locale.ROOT))) {
                if (sender.getInventory().getItemInMainHand().getType().isAir()) {
                    plugin.text().of(sender, "no-anythings-in-your-hand").send();
                    return;
                }
                String name = plugin.getItemMarker().get(item);
                if (name == null) {
                    plugin.text().of(sender, "lookup-item-test-not-found").send();
                } else {
                    plugin.text().of(sender, "lookup-item-test-found", name).send();
                }
                return;
            }
            plugin.text().of(sender, "command-incorrect", "/qs lookup <create/remove/test> <name>").send();
            return;
        }
        switch (cmdArg[0].toLowerCase(Locale.ROOT)) {
            case "create" -> {
                if (sender.getInventory().getItemInMainHand().getType().isAir()) {
                    plugin.text().of(sender, "no-anythings-in-your-hand").send();
                    return;
                }
                ItemMarker.OperationResult result = plugin.getItemMarker().save(cmdArg[1], item);
                switch (result) {
                    case SUCCESS -> plugin.text().of(sender, "lookup-item-created", cmdArg[1]).send();
                    case REGEXP_FAILURE ->
                            plugin.text().of(sender, "lookup-item-name-regex", plugin.getItemMarker().getNameRegExp(), cmdArg[1]).send();
                    case NAME_CONFLICT -> plugin.text().of(sender, "lookup-item-exists", cmdArg[1]).send();
                    default -> plugin.text().of(sender, "internal-error", cmdArg[1]).send();
                }
            }
            case "remove" -> {
                ItemMarker.OperationResult result = plugin.getItemMarker().remove(cmdArg[1]);
                switch (result) {
                    case SUCCESS -> plugin.text().of(sender, "lookup-item-removed", cmdArg[1]).send();
                    case NOT_EXISTS -> plugin.text().of(sender, "lookup-item-not-found", cmdArg[1]).send();
                    default -> plugin.text().of(sender, "internal-error", cmdArg[1]).send();
                }
            }
            default -> plugin.text().of(sender, "command-incorrect", "/qs lookup <create/remove/test> <name>").send();
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length == 1) {
            return Arrays.asList("create", "remove", "test");
        }
        if (cmdArg.length > 1) {
            if (cmdArg[0].equalsIgnoreCase("remove")) {
                return plugin.getItemMarker().getRegisteredItems();
            }
        }
        return Collections.emptyList();
    }
}
