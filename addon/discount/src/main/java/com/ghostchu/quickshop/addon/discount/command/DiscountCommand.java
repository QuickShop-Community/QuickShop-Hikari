package com.ghostchu.quickshop.addon.discount.command;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.discount.DiscountCode;
import com.ghostchu.quickshop.addon.discount.DiscountCodeManager;
import com.ghostchu.quickshop.addon.discount.Main;
import com.ghostchu.quickshop.addon.discount.type.CodeCreationResponse;
import com.ghostchu.quickshop.addon.discount.type.CodeType;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.ChatSheetPrinter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.enginehub.squirrelid.Profile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.*;

public class DiscountCommand implements CommandHandler<CommandSender> {
    private final Main main;
    private final QuickShop quickshop;

    public DiscountCommand(Main main, QuickShop quickshop) {
        this.main = main;
        this.quickshop = quickshop;
    }

    @Override
    public void onCommand(CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            quickshop.text().of(sender, "command-incorrect", "/qs discount <install/uninstall/create/remove/list/config/query>").send();
            return;
        }
        String[] passThroughArgs = new String[cmdArg.length - 1];
        System.arraycopy(cmdArg, 1, passThroughArgs, 0, passThroughArgs.length);
        switch (cmdArg[0]) {
            case "install" -> install(sender, passThroughArgs);
            case "uninstall" -> uninstall(sender, passThroughArgs);
            case "create" -> create(sender, passThroughArgs);
            case "remove" -> remove(sender, passThroughArgs);
            case "info" -> info(sender, passThroughArgs);
            case "config" -> config(sender, passThroughArgs);
            case "list" -> list(sender, passThroughArgs);
            default ->
                    quickshop.text().of(sender, "command-incorrect", "/qs discount <install/uninstall/create/remove/config/query>").send();
        }
    }

    private void list(CommandSender sender, String[] passThroughArgs) {
        if (!(sender instanceof Player p)) {
            quickshop.text().of(sender, "command-type-mismatch", "Player").send();
            return;
        }
        if (!p.hasPermission("quickshop.discount.list")) {
            quickshop.text().of(sender, "no-permission").send();
            return;
        }
        ChatSheetPrinter printer = new ChatSheetPrinter(sender);
        printer.printHeader();
        printer.printLine(quickshop.text().of(sender, "addon.discount.discount-code-list").forLocale());
        main.getCodeManager().getCodes().stream().filter(code -> code.getOwner().equals(((Player) sender).getUniqueId()))
                .forEach(code -> printer.printLine(Component.text(code.getCode()).color(NamedTextColor.AQUA)));
        printer.printFooter();
    }

    private void config(CommandSender sender, String[] passThroughArgs) {
        if (!(sender instanceof Player p)) {
            quickshop.text().of(sender, "command-type-mismatch", "Player").send();
            return;
        }
        if (passThroughArgs.length < 2) {
            quickshop.text().of(sender, "command-incorrect", "/qs discount config <code> <addshop/removeshop/scope> [args]").send();
            return;
        }
        DiscountCode code = main.getCodeManager().getCode(passThroughArgs[0]);
        if (code == null) {
            quickshop.text().of(sender, "addon.discount.invalid-discount-code", passThroughArgs[0]).send();
            return;
        }
        switch (passThroughArgs[1]) {
            case "addshop" -> {
                Shop shop = getLookingShop(sender);
                if (shop == null) {
                    quickshop.text().of(sender, "not-looking-at-shop").send();
                    return;
                }
                if (!shop.playerAuthorize(((Player) sender).getUniqueId(), main, "discount_code_create")) {
                    quickshop.text().of(sender, "no-permission").send();
                    return;
                }
                if (code.getShopScope().add(shop.getShopId())) {
                    quickshop.text().of(sender, "addon.discount.discount-code-config-shop-added", shop.getShopId()).send();
                    main.getCodeManager().saveDatabase();
                } else {
                    quickshop.text().of(sender, "addon.discount.discount-code-config-shop-add-failure", shop.getShopId()).send();
                }
            }
            case "removeshop" -> {
                Shop shop = getLookingShop(sender);
                if (shop == null) {
                    quickshop.text().of(sender, "not-looking-at-shop").send();
                    return;
                }
                if (!shop.playerAuthorize(((Player) sender).getUniqueId(), main, "discount_code_create")) {
                    quickshop.text().of(sender, "no-permission").send();
                    return;
                }
                if (code.getShopScope().remove(shop.getShopId())) {
                    quickshop.text().of(sender, "addon.discount.discount-code-config-shop-removed", shop.getShopId()).send();
                    main.getCodeManager().saveDatabase();
                } else {
                    quickshop.text().of(sender, "addon.discount.discount-code-config-shop-remove-failure", shop.getShopId()).send();
                }
            }
            case "scope" -> {
                if (passThroughArgs.length < 3) {
                    StringJoiner joiner = new StringJoiner("<", "/", ">");
                    Arrays.stream(CodeType.values()).forEach(t -> joiner.add(t.name()));
                    quickshop.text().of(sender, "command-incorrect", "/qs discount config <code> scope <scope>").send();
                    return;
                }
                if (!code.getOwner().equals(((Player) sender).getUniqueId()) && !QuickShop.getPermissionManager().hasPermission(sender, "quickshopaddon.discount.bypass")) {
                    quickshop.text().of(sender, "no-permission").send();
                    return;
                }
                String newScope = passThroughArgs[2].toUpperCase(Locale.ROOT).replace("-", "_");
                try {
                    CodeType type = CodeType.valueOf(newScope);
                    if (!QuickShop.getPermissionManager().hasPermission(sender, "quickshopaddon.discount.create." + type.name().toLowerCase())) {
                        quickshop.text().of(sender, "no-permission").send();
                        return;
                    }
                    code.setCodeType(type);
                    quickshop.text().of(sender, "discount-code-config-applied").send();
                    main.getCodeManager().saveDatabase();
                } catch (IllegalArgumentException e) {
                    quickshop.text().of(sender, "addon.discount.invalid-code-type", newScope).send();
                    return;
                }
            }
//            case "expire" -> {
//                if (passThroughArgs.length < 3) {
//                    quickshop.text().of(sender, "command-incorrect", "/qs discount config <code> expire <time>").send();
//                    return;
//                }
//                if(!code.getOwner().equals(((Player) sender).getUniqueId()) && !QuickShop.getPermissionManager().hasPermission(sender,"quickshopaddon.discount.bypass") && ){
//                    quickshop.text().of(sender, "no-permission").send();
//                    return;
//                }
//                String expireTimeStr = passThroughArgs[2];
//                if (expireTimeStr.equals("-1")) {
//                    code.setExpiredTime(-1);
//                    quickshop.text().of(sender, "addon.discount.discount-code-config-expire-time-set", "Never").send();
//                    return;
//                }
//                Date date = CommonUtil.zuluTime2Date(passThroughArgs[5]);
//                if (date == null) {
//                    quickshop.text().of(sender, "invalid-time-format", passThroughArgs[5]).send();
//                    return;
//                }
//                code.setExpiredTime(date.getTime());
//                quickshop.text().of(sender,"discount-code-config-applied").send();
//            }
        }
    }

    private void info(CommandSender sender, String[] passThroughArgs) {
        if (!(sender instanceof Player p)) {
            quickshop.text().of(sender, "command-type-mismatch", "Player").send();
            return;
        }
        DiscountCode code = main.getStatusManager().get(p.getUniqueId(), main.getCodeManager());
        if (code == null) {
            quickshop.text().of(sender, "addon.discount.discount-code-query-nothing").send();
            return;
        }
        String name = "Unknown";
        Profile profile = quickshop.getPlayerFinder().find(code.getOwner());
        if (profile != null && !profile.getName().isEmpty())
            name = profile.getName();
        Component appliedTo = quickshop.text().of(sender, "addon.discount.code-type." + code.getCodeType().name()).forLocale();
        String remainsUsage;
        int remains = code.getRemainsUsage(((Player) sender).getUniqueId());
        if (remains == -1) remainsUsage = "Inf.";
        else remainsUsage = String.valueOf(remains);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String expiredOn = sdf.format(new Date(code.getExpiredTime()));
        quickshop.text().of(sender, "addon.discount.discount-code-details"
                , code.getCode(), name, appliedTo, remainsUsage, expiredOn, code.getThreshold(),
                code.getRate().format(sender, quickshop.text())).send();
    }


    private void create(CommandSender sender, String[] passThroughArgs) {
        if (!(sender instanceof Player p)) {
            quickshop.text().of(sender, "command-type-mismatch", "Player").send();
            return;
        }
        if (passThroughArgs.length < 1) {
            quickshop.text().of(sender, "command-incorrect", "/qs discount create <code> <code-type> <rate> [max-usage] [threshold] [expired-time]").send();
            return;
        }
        // code, code-type, rate, max-usage, threshold, expired-time
        if (passThroughArgs.length < 3) {
            quickshop.text().of(sender, "command-incorrect", "/qs discount create <code> <code-type> <rate> [max-usage] [threshold] [expired-time]").send();
            return;
        }
        String code = passThroughArgs[0];
        String codeTypeStr = passThroughArgs[1];
        CodeType codeType = null;
        for (CodeType value : CodeType.values()) {
            if (value.name().equalsIgnoreCase(codeTypeStr)) {
                codeType = value;
                break;
            }
        }
        if (codeType == null) {
            quickshop.text().of(sender, "addon.discount.invalid-code-type", codeTypeStr).send();
            return;
        }
        int maxUsages = -1;
        double threshold = -1;
        long expiredOn = -1;
        if (passThroughArgs.length >= 4) {
            try {
                maxUsages = Integer.parseInt(passThroughArgs[3]);
            } catch (NumberFormatException e) {
                quickshop.text().of(sender, "not-a-number", passThroughArgs[3]).send();
                return;
            }
        }
        if (passThroughArgs.length >= 5) {
            try {
                threshold = Double.parseDouble(passThroughArgs[4]);
            } catch (NumberFormatException e) {
                quickshop.text().of(sender, "not-a-number", passThroughArgs[4]).send();
                return;
            }
        }
        if (passThroughArgs.length >= 6 && !passThroughArgs[5].equalsIgnoreCase("-1")) {
            Date date = CommonUtil.parseTime(passThroughArgs[5]);
            if (date == null) {
                quickshop.text().of(sender, "not-a-valid-time", passThroughArgs[5]).send();
                return;
            }
            expiredOn = date.getTime();
        }
        CodeCreationResponse response = main.getCodeManager().createDiscountCode(p, p.getUniqueId(), code, codeType, passThroughArgs[2], maxUsages, threshold, expiredOn);
        switch (response) {
            case PERMISSION_DENIED -> quickshop.text().of(sender, "no-permission").send();
            case INVALID_RATE -> quickshop.text().of(sender, "addon.discount.invalid-discount-rate").send();
            case REGEX_FAILURE ->
                    quickshop.text().of(sender, "addon.discount.invalid-discount-code-regex", DiscountCodeManager.NAME_REG_EXP).send();
            case INVALID_USAGE -> quickshop.text().of(sender, "addon.discount.invalid-usage-restriction").send();
            case INVALID_THRESHOLD ->
                    quickshop.text().of(sender, "addon.discount.invalid-threshold-restriction").send();
            case INVALID_EXPIRE_TIME -> quickshop.text().of(sender, "addon.discount.invalid-expire-time").send();
            case CODE_EXISTS -> quickshop.text().of(sender, "addon.discount.discount-code-already-exists").send();
            case SUCCESS -> quickshop.text().of(sender, "addon.discount.discount-code-created"
                    , code
                    , CommonUtil.prettifyText(codeType.name())
                    , "/qs discount install " + code).send();
        }
    }

    private void uninstall(CommandSender sender, String[] passThroughArgs) {
        if (!(sender instanceof Player p)) {
            quickshop.text().of(sender, "command-type-mismatch", "Player").send();
            return;
        }
        main.getStatusManager().unset(p.getUniqueId());
        quickshop.text().of(sender, "addon.discount.discount-code-uninstalled").send();
    }

    private void install(CommandSender sender, String[] passThroughArgs) {
        if (!(sender instanceof Player p)) {
            quickshop.text().of(sender, "command-type-mismatch", "Player").send();
            return;
        }
        if (passThroughArgs.length < 1) {
            quickshop.text().of(sender, "command-incorrect", "/qs discount install <code>").send();
            return;
        }
        String codeStr = passThroughArgs[0];
        DiscountCode code = main.getCodeManager().getCode(codeStr);
        if (code == null) {
            quickshop.text().of(sender, "addon.discount.invalid-discount-code").send();
            return;
        }
        main.getStatusManager().set(p.getUniqueId(), code);
        quickshop.text().of(sender, "addon.discount.discount-code-installed", code.getCode()).send();
    }

    private void remove(CommandSender sender, String[] passThroughArgs) {
        if (passThroughArgs.length < 1) {
            quickshop.text().of(sender, "command-incorrect", "/qs discount remove <code>").send();
            return;
        }
        String codeStr = passThroughArgs[0];
        DiscountCode code = main.getCodeManager().getCode(codeStr);
        if (code == null) {
            quickshop.text().of(sender, "addon.discount.invalid-discount-code").send();
            return;
        }
        if (sender instanceof Player p) {
            if (!code.getOwner().equals(p.getUniqueId()) &&
                    !quickshop.perm().hasPermission(sender, "quickshopaddon.discount.remove.bypass")) {
                quickshop.text().of(sender, "no-permission").send();
                return;
            }
        } else {
            if (!quickshop.perm().hasPermission(sender, "quickshopaddon.discount.remove.bypass")) {
                quickshop.text().of(sender, "no-permission").send();
                return;
            }
        }
        main.getCodeManager().removeCode(code);
        quickshop.text().of(sender, "addon.discount.discount-code-removed", code.getCode()).send();
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
//        case "install" -> install(sender, passThroughArgs);
//        case "uninstall" -> uninstall(sender, passThroughArgs);
//        case "create" -> create(sender, passThroughArgs);
//        case "remove" -> remove(sender, passThroughArgs);
//        case "query" -> query(sender, passThroughArgs);
//        case "config" -> config(sender, passThroughArgs);
//        case "list" -> list(sender, passThroughArgs);
        if (cmdArg.length == 1) {
            return Arrays.asList("install", "uninstall", "create", "remove", "info", "config", "list");
        }
        if (cmdArg.length == 2) {
            return switch (cmdArg[0]) {
                case "list", "info" -> Collections.emptyList();
                case "install", "uninstall", "remove", "create", "config" -> List.of("<code>");
                default -> Collections.emptyList();
            };
        }
        if (cmdArg.length == 3) {
            return switch (cmdArg[0]) {
                case "create" -> Arrays.stream(CodeType.values()).map(Enum::name).toList();
                case "config" -> List.of("scope", "addshop", "removeshop");
                default -> Collections.emptyList();
            };
        }
        if (cmdArg.length == 4) {
            return switch (cmdArg[0]) {
                case "create" -> List.of(
                        "1. Command Hint:",
                        "2. Argument: <rate>",
                        "3. Description: The actual percentage or money you will earn",
                        "4. Input `30%` = price * 0.3",
                        "5. Input `50` = price-50");
                default -> Collections.emptyList();
            };
        }
        if (cmdArg.length == 5) {
            return switch (cmdArg[0]) {
                case "create" -> List.of("[max-usage], -1 for unlimited");
                default -> Collections.emptyList();
            };
        }
        if (cmdArg.length == 6) {
            return switch (cmdArg[0]) {
                case "create" -> List.of(
                        "1. Command Hint:",
                        "2. Argument: [threshold]",
                        "3. Description: min price to apply discount",
                        "4. -1 for unlimited usage");
                default -> Collections.emptyList();
            };
        }
        if (cmdArg.length == 7) {
            return switch (cmdArg[0]) {
                case "create" -> List.of(
                        "1. Command Hint:",
                        "2. Argument: [expired]",
                        "3. Description: The discount code expired time.",
                        "4. -1 for unlimited duration.",
                        "5. Accept both Zulu time and UNIX timestamp in seconds.",
                        "6. Zulu Example: 2022-12-17T10:31:37Z",
                        "7. UNIX Example: 1671273097");
                default -> Collections.emptyList();
            };
        }
        return Collections.emptyList();
    }
}
