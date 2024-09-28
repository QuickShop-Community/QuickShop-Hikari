package com.ghostchu.quickshop.addon.discount.command;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.addon.discount.DiscountCode;
import com.ghostchu.quickshop.addon.discount.DiscountCodeManager;
import com.ghostchu.quickshop.addon.discount.Main;
import com.ghostchu.quickshop.addon.discount.type.CodeCreationResponse;
import com.ghostchu.quickshop.addon.discount.type.CodeType;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.ChatSheetPrinter;
import com.ghostchu.quickshop.util.logger.Log;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class DiscountCommand implements CommandHandler<CommandSender> {

  private final Main main;
  private final QuickShop quickshop;

  public DiscountCommand(final Main main, final QuickShop quickshop) {

    this.main = main;
    this.quickshop = quickshop;
  }

  @Override
  public void onCommand(final CommandSender sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if(parser.getArgs().isEmpty()) {
      quickshop.text().of(sender, "command-incorrect", "/quickshop discount <install/uninstall/create/remove/list/config/info/listall>").send();
      return;
    }
    final String[] passThroughArgs = new String[parser.getArgs().size() - 1];
    System.arraycopy(parser.getArgs().toArray(new String[0]), 1, passThroughArgs, 0, passThroughArgs.length);
    switch(parser.getArgs().get(0)) {
      case "install" -> install(sender, passThroughArgs);
      case "uninstall" -> uninstall(sender, passThroughArgs);
      case "create" -> create(sender, passThroughArgs);
      case "remove" -> remove(sender, passThroughArgs);
      case "info" -> info(sender, passThroughArgs);
      case "config" -> config(sender, passThroughArgs);
      case "list" -> list(sender, passThroughArgs);
      case "listall" -> listAll(sender, passThroughArgs);
      default ->
              quickshop.text().of(sender, "command-incorrect", "/quickshop discount <install/uninstall/create/remove/config/info/listall>").send();
    }
  }

  private void install(final CommandSender sender, final String[] passThroughArgs) {

    if(!(sender instanceof Player p)) {
      quickshop.text().of(sender, "command-type-mismatch", "Player").send();
      return;
    }
    if(passThroughArgs.length < 1) {
      quickshop.text().of(sender, "command-incorrect", "/quickshop discount install <code>").send();
      return;
    }
    final String codeStr = passThroughArgs[0];
    final DiscountCode code = main.getCodeManager().getCode(codeStr);
    if(code == null) {
      quickshop.text().of(sender, "addon.discount.invalid-discount-code").send();
      return;
    }
    main.getStatusManager().set(p.getUniqueId(), code);
    quickshop.text().of(sender, "addon.discount.discount-code-installed", code.getCode()).send();
  }

  private void uninstall(final CommandSender sender, final String[] passThroughArgs) {

    if(!(sender instanceof Player p)) {
      quickshop.text().of(sender, "command-type-mismatch", "Player").send();
      return;
    }
    main.getStatusManager().unset(p.getUniqueId());
    quickshop.text().of(sender, "addon.discount.discount-code-uninstalled").send();
  }

  private void create(final CommandSender sender, final String[] passThroughArgs) {

    if(!(sender instanceof Player p)) {
      quickshop.text().of(sender, "command-type-mismatch", "Player").send();
      return;
    }
    if(passThroughArgs.length < 1) {
      quickshop.text().of(sender, "command-incorrect", "/quickshop discount create <code> <code-type> <rate> [max-usage] [threshold] [expired-time]").send();
      return;
    }
    // code, code-type, rate, max-usage, threshold, expired-time
    if(passThroughArgs.length < 3) {
      quickshop.text().of(sender, "command-incorrect", "/quickshop discount create <code> <code-type> <rate> [max-usage] [threshold] [expired-time]").send();
      return;
    }
    final String code = passThroughArgs[0];
    final String codeTypeStr = passThroughArgs[1];
    CodeType codeType = null;
    for(final CodeType value : CodeType.values()) {
      if(value.name().equalsIgnoreCase(codeTypeStr)) {
        codeType = value;
        break;
      }
    }
    if(codeType == null) {
      quickshop.text().of(sender, "addon.discount.invalid-code-type", codeTypeStr).send();
      return;
    }
    int maxUsages = -1;
    double threshold = -1;
    long expiredOn = -1;
    if(passThroughArgs.length >= 4) {
      try {
        maxUsages = Integer.parseInt(passThroughArgs[3]);
      } catch(NumberFormatException e) {
        quickshop.text().of(sender, "not-a-number", passThroughArgs[3]).send();
        return;
      }
    }
    if(passThroughArgs.length >= 5) {
      try {
        threshold = Double.parseDouble(passThroughArgs[4]);
      } catch(NumberFormatException e) {
        quickshop.text().of(sender, "not-a-number", passThroughArgs[4]).send();
        return;
      }
    }
    if(passThroughArgs.length >= 6 && !"-1".equalsIgnoreCase(passThroughArgs[5])) {
      final Date date = CommonUtil.parseTime(passThroughArgs[5]);
      if(date == null) {
        quickshop.text().of(sender, "not-a-valid-time", passThroughArgs[5]).send();
        return;
      }
      expiredOn = date.getTime();
    }
    final CodeCreationResponse response = main.getCodeManager().createDiscountCode(p, p.getUniqueId(), code, codeType, passThroughArgs[2], maxUsages, threshold, expiredOn);
    switch(response) {
      case PERMISSION_DENIED -> quickshop.text().of(sender, "no-permission").send();
      case INVALID_RATE ->
              quickshop.text().of(sender, "addon.discount.invalid-discount-rate").send();
      case REGEX_FAILURE ->
              quickshop.text().of(sender, "addon.discount.invalid-discount-code-regex", DiscountCodeManager.NAME_REG_EXP).send();
      case INVALID_USAGE ->
              quickshop.text().of(sender, "addon.discount.invalid-usage-restriction").send();
      case INVALID_THRESHOLD ->
              quickshop.text().of(sender, "addon.discount.invalid-threshold-restriction").send();
      case INVALID_EXPIRE_TIME ->
              quickshop.text().of(sender, "addon.discount.invalid-expire-time").send();
      case CODE_EXISTS ->
              quickshop.text().of(sender, "addon.discount.discount-code-already-exists").send();
      case SUCCESS ->
              quickshop.text().of(sender, "addon.discount.discount-code-created-successfully",
                                  code,
                                  CommonUtil.prettifyText(codeType.name()),
                                  "/quickshop discount install " + code,
                                  "/quickshop discount config " + code + " addshop").send();
    }
    Log.debug("Discount code created: " + main.getCodeManager().getCode(code));
  }

  private void remove(final CommandSender sender, final String[] passThroughArgs) {

    if(passThroughArgs.length < 1) {
      quickshop.text().of(sender, "command-incorrect", "/quickshop discount remove <code>").send();
      return;
    }
    final String codeStr = passThroughArgs[0];
    final DiscountCode code = main.getCodeManager().getCode(codeStr);
    if(code == null) {
      quickshop.text().of(sender, "addon.discount.invalid-discount-code").send();
      return;
    }
    if(sender instanceof Player p) {
      if(!code.getOwner().equals(p.getUniqueId()) && !quickshop.perm().hasPermission(sender, "quickshopaddon.discount.remove.bypass")) {
        quickshop.text().of(sender, "no-permission").send();
        return;
      }
    } else {
      if(!quickshop.perm().hasPermission(sender, "quickshopaddon.discount.remove.bypass")) {
        quickshop.text().of(sender, "no-permission").send();
        return;
      }
    }
    main.getCodeManager().removeCode(code);
    quickshop.text().of(sender, "addon.discount.discount-code-removed", code.getCode()).send();
  }

  private void info(final CommandSender sender, final String[] passThroughArgs) {

    if(!(sender instanceof Player p)) {
      quickshop.text().of(sender, "command-type-mismatch", "Player").send();
      return;
    }
    final DiscountCode code = main.getStatusManager().get(p.getUniqueId(), main.getCodeManager());
    if(code == null) {
      quickshop.text().of(sender, "addon.discount.discount-code-query-nothing").send();
      return;
    }
    String name = "Unknown";
    final String lookupName = quickshop.getPlayerFinder().uuid2Name(code.getOwner());
    if(lookupName != null) {
      name = lookupName;
    }
    final Component appliedTo = quickshop.text().of(sender, "addon.discount.code-type." + code.getCodeType().name()).forLocale();
    final String remainsUsage;
    final int remains = code.getRemainsUsage(((Player)sender).getUniqueId());
    if(remains == -1) {
      remainsUsage = "Inf.";
    } else {
      remainsUsage = String.valueOf(remains);
    }
    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    final String expiredOn = sdf.format(new Date(code.getExpiredTime()));
    quickshop.text().of(sender, "addon.discount.discount-code-details", code.getCode(), name, appliedTo, remainsUsage, expiredOn, code.getThreshold(), code.getRate().format(sender, quickshop.text())).send();
  }

  private void config(final CommandSender sender, final String[] passThroughArgs) {

    if(!(sender instanceof Player p)) {
      quickshop.text().of(sender, "command-type-mismatch", "Player").send();
      return;
    }
    if(passThroughArgs.length < 2) {
      quickshop.text().of(sender, "command-incorrect", "/quickshop discount config <code> <addshop/removeshop/scope> [args]").send();
      return;
    }
    final DiscountCode code = main.getCodeManager().getCode(passThroughArgs[0]);
    if(code == null) {
      quickshop.text().of(sender, "addon.discount.invalid-discount-code", passThroughArgs[0]).send();
      return;
    }
    switch(passThroughArgs[1]) {
      case "addshop" -> {
        final Shop shop = getLookingShop(sender);
        if(shop == null) {
          quickshop.text().of(sender, "not-looking-at-shop").send();
          return;
        }
        if(!shop.playerAuthorize(((Player)sender).getUniqueId(), main, "discount_code_create")) {
          quickshop.text().of(sender, "no-permission").send();
          return;
        }
        if(code.getShopScope().add(shop.getShopId())) {
          quickshop.text().of(sender, "addon.discount.discount-code-config-shop-added", shop.getShopId()).send();
          main.getCodeManager().saveDatabase();
        } else {
          quickshop.text().of(sender, "addon.discount.discount-code-config-shop-add-failure", shop.getShopId()).send();
        }
      }
      case "removeshop" -> {
        final Shop shop = getLookingShop(sender);
        if(shop == null) {
          quickshop.text().of(sender, "not-looking-at-shop").send();
          return;
        }
        if(!shop.playerAuthorize(((Player)sender).getUniqueId(), main, "discount_code_create")) {
          quickshop.text().of(sender, "no-permission").send();
          return;
        }
        if(code.getShopScope().remove(shop.getShopId())) {
          quickshop.text().of(sender, "addon.discount.discount-code-config-shop-removed", shop.getShopId()).send();
          main.getCodeManager().saveDatabase();
        } else {
          quickshop.text().of(sender, "addon.discount.discount-code-config-shop-remove-failure", shop.getShopId()).send();
        }
      }
      case "scope" -> {
        if(passThroughArgs.length < 3) {
//                    StringJoiner joiner = new StringJoiner("<", "/", ">");
//                    Arrays.stream(CodeType.values()).forEach(t -> joiner.add(t.name()));
          quickshop.text().of(sender, "command-incorrect", "/quickshop discount config <code> scope <scope>").send();
          return;
        }
        if(!code.getOwner().equals(((Player)sender).getUniqueId()) && !quickshop.perm().hasPermission(sender, "quickshopaddon.discount.bypass")) {
          quickshop.text().of(sender, "no-permission").send();
          return;
        }
        final String newScope = passThroughArgs[2].toUpperCase(Locale.ROOT).replace("-", "_");
        try {
          final CodeType type = CodeType.valueOf(newScope);
          if(!quickshop.perm().hasPermission(sender, "quickshopaddon.discount.create." + type.name().toLowerCase())) {
            quickshop.text().of(sender, "no-permission").send();
            return;
          }
          code.setCodeType(type);
          quickshop.text().of(sender, "discount-code-config-applied").send();
          main.getCodeManager().saveDatabase();
        } catch(IllegalArgumentException e) {
          quickshop.text().of(sender, "addon.discount.invalid-code-type", newScope).send();
        }
      }
    }
  }

  private void list(final CommandSender sender, final String[] passThroughArgs) {

    if(!(sender instanceof Player p)) {
      quickshop.text().of(sender, "command-type-mismatch", "Player").send();
      return;
    }
    if(!quickshop.perm().hasPermission(sender, "quickshopaddon.discount.list")) {
      quickshop.text().of(sender, "no-permission").send();
      return;
    }
    final ChatSheetPrinter printer = new ChatSheetPrinter(sender);
    printer.printHeader();
    printer.printLine(quickshop.text().of(sender, "addon.discount.discount-code-list").forLocale());
    main.getCodeManager().getCodes().stream().filter(code->code.getOwner().equals(((Player)sender).getUniqueId())).forEach(code->printer.printLine(Component.text(code.getCode()).color(NamedTextColor.AQUA)));
    printer.printFooter();
  }

  private void listAll(final CommandSender sender, final String[] passThroughArgs) {

    if(!quickshop.perm().hasPermission(sender, "quickshopaddon.discount.listall")) {
      quickshop.text().of(sender, "no-permission").send();
      return;
    }
    final ChatSheetPrinter printer = new ChatSheetPrinter(sender);
    printer.printHeader();
    printer.printLine(quickshop.text().of(sender, "addon.discount.discount-code-list").forLocale());
    main.getCodeManager().getCodes().forEach(code->printer.printLine(Component.text(code.getCode()).color(NamedTextColor.AQUA)));
    printer.printFooter();
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final String commandLabel, @NotNull final String[] cmdArg) {

    if(cmdArg.length == 1) {
      return Arrays.asList("install", "uninstall", "create", "remove", "info", "config", "list");
    }
    if(cmdArg.length == 2) {
      return switch(cmdArg[0]) {
        case "install", "uninstall", "remove", "create", "config" ->
                List.of(PlainTextComponentSerializer
                                .plainText()
                                .serialize(quickshop
                                                   .text()
                                                   .of(sender,
                                                       "addon.discount.tab-complete.discount.general.code"
                                                      ).forLocale()));
        default -> Collections.emptyList();
      };
    }
    if(cmdArg.length == 3) {
      return switch(cmdArg[0]) {
        case "create" -> Arrays.stream(CodeType.values()).map(Enum::name).toList();
        case "config" -> List.of("scope", "addshop", "removeshop");
        default -> Collections.emptyList();
      };
    }
    if(cmdArg.length == 4) {
      return switch(cmdArg[0]) {
        case "create" ->
                tabCompleteHint(sender, "addon.discount.tab-complete.discount.create.rate");
        default -> Collections.emptyList();
      };
    }
    if(cmdArg.length == 5) {
      return switch(cmdArg[0]) {
        case "create" ->
                tabCompleteHint(sender, "addon.discount.tab-complete.discount.create.max-usage");
        default -> Collections.emptyList();
      };
    }
    if(cmdArg.length == 6) {
      return switch(cmdArg[0]) {
        case "create" ->
                tabCompleteHint(sender, "addon.discount.tab-complete.discount.create.threshold");
        default -> Collections.emptyList();
      };
    }
    if(cmdArg.length == 7) {
      return switch(cmdArg[0]) {
        case "create" ->
                tabCompleteHint(sender, "addon.discount.tab-complete.discount.create.expired");
        default -> Collections.emptyList();
      };
    }
    return Collections.emptyList();
  }

  private List<String> tabCompleteHint(@NotNull final CommandSender sender, @NotNull final String key) {

    final String str = PlainTextComponentSerializer.plainText().serialize(quickshop.text().of(sender, key).forLocale());
    final List<String> list = new ArrayList<>();
    final String[] explode = str.split("\n");
    for(int i = 0; i < explode.length; i++) {
      list.add(i + ". " + explode[i]);
    }
    return list;
  }
}
