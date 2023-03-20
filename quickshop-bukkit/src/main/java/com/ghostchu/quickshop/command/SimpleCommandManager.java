package com.ghostchu.quickshop.command;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandContainer;
import com.ghostchu.quickshop.api.command.CommandManager;
import com.ghostchu.quickshop.command.subcommand.*;
import com.ghostchu.quickshop.command.subcommand.silent.SubCommand_SilentBuy;
import com.ghostchu.quickshop.command.subcommand.silent.SubCommand_SilentEmpty;
import com.ghostchu.quickshop.command.subcommand.silent.SubCommand_SilentPreview;
import com.ghostchu.quickshop.command.subcommand.silent.SubCommand_SilentRemove;
import com.ghostchu.quickshop.command.subcommand.silent.SubCommand_SilentSell;
import com.ghostchu.quickshop.command.subcommand.silent.SubCommand_SilentToggleDisplay;
import com.ghostchu.quickshop.command.subcommand.silent.SubCommand_SilentUnlimited;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.paste.item.SubPasteItem;
import com.ghostchu.quickshop.util.paste.util.HTMLTable;
import com.ghostchu.quickshop.util.performance.PerfMonitor;
import com.ghostchu.simplereloadlib.ReloadResult;
import com.ghostchu.simplereloadlib.Reloadable;
import com.google.common.collect.ImmutableList;
import lombok.Data;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

@Data
@SuppressWarnings("unchecked")
public class SimpleCommandManager implements CommandManager, TabCompleter, CommandExecutor, SubPasteItem, Reloadable {
    private static final String[] EMPTY_ARGS = new String[0];
    private final List<CommandContainer> cmds = new CopyOnWriteArrayList<>(); //Because we open to allow register, so this should be thread-safe
    private final QuickShop plugin;
    private final CommandContainer rootContainer;

    private boolean playSoundOnTabComplete;
    private boolean playSoundOnCommand;

    public SimpleCommandManager(QuickShop plugin) {
        this.plugin = plugin;
        this.plugin.getReloadManager().register(this);
        this.rootContainer = CommandContainer.builder()
                .prefix("")
                .permission(null)
                .executor(new SubCommand_ROOT(plugin))
                .build();
        registerCmd(
                CommandContainer.builder()
                        .prefix("help")
                        .permission(null)
                        .executor(new SubCommand_Help(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("unlimited")
                        .permission("quickshop.unlimited")
                        .executor(new SubCommand_Unlimited(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("silentunlimited")
                        .hidden(true)
                        .permission("quickshop.unlimited")
                        .executor(new SubCommand_SilentUnlimited(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("transfer")
                        .permission("quickshop.transfer")
                        .executor(new SubCommand_Transfer(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("setowner")
                        .permission("quickshop.setowner")
                        .executor(new SubCommand_SetOwner(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("owner")
                        .hidden(true)
                        .permission("quickshop.setowner")
                        .executor(new SubCommand_SetOwner(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("amount")
                        .permission(null)
                        .executor(new SubCommand_Amount(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("buy")
                        .permission("quickshop.create.buy")
                        .executor(new SubCommand_Buy(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("sell")
                        .permission("quickshop.create.sell")
                        .executor(new SubCommand_Sell(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("silentbuy")
                        .hidden(true)
                        .permission("quickshop.create.buy")
                        .executor(new SubCommand_SilentBuy(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("silentsell")
                        .hidden(true)
                        .permission("quickshop.create.sell")
                        .executor(new SubCommand_SilentSell(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("price")
                        .permission("quickshop.create.changeprice")
                        .executor(new SubCommand_Price(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("remove")
                        .permission(null)
                        .executor(new SubCommand_Remove(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("silentremove")
                        .hidden(true)
                        .permission(null)
                        .executor(new SubCommand_SilentRemove(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("empty")
                        .permission("quickshop.empty")
                        .executor(new SubCommand_Empty(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("refill")
                        .permission("quickshop.refill")
                        .executor(new SubCommand_Refill(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("silentempty")
                        .hidden(true)
                        .permission("quickshop.empty")
                        .executor(new SubCommand_SilentEmpty(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("silentpreview")
                        .hidden(true)
                        .permission("quickshop.preview")
                        .executor(new SubCommand_SilentPreview(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("silenttoggledisplay")
                        .hidden(true)
                        .permission("quickshop.toggledisplay")
                        .executor(new SubCommand_SilentToggleDisplay(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("clean")
                        .permission("quickshop.clean")
                        .executor(new SubCommand_Clean(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("reload")
                        .permission("quickshop.reload")
                        .executor(new SubCommand_Reload(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("about")
                        .permission("quickshop.about")
                        .executor(new SubCommand_About(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("debug")
                        .permission("quickshop.debug")
                        .executor(new SubCommand_Debug(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("fetchmessage")
                        .permission("quickshop.fetchmessage")
                        .executor(new SubCommand_FetchMessage())
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("info")
                        .permission("quickshop.info")
                        .executor(new SubCommand_Info(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("paste")
                        .permission("quickshop.paste")
                        .executor(new SubCommand_Paste(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("staff")
                        .permission("quickshop.staff")
                        .executor(new SubCommand_Staff(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("create")
                        .permission("quickshop.create.cmd")
                        .permission("quickshop.create.sell")
                        .executor(new SubCommand_Create(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("find")
                        .permission("quickshop.find")
                        .executor(new SubCommand_Find(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("supercreate")
                        .permission("quickshop.create.admin")
                        .permission("quickshop.create.sell")
                        .executor(new SubCommand_SuperCreate(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("cleanghost")
                        .permission("quickshop.cleanghost")
                        .hidden(true)
                        .executor(new SubCommand_CleanGhost(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("reset")
                        .hidden(true)
                        .permission("quickshop.reset")
                        .executor(new SubCommand_Reset(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("recovery")
                        .hidden(true)
                        .permission("quickshop.recovery")
                        .executor(new SubCommand_Recovery(plugin))
                        .build());
        registerCmd(
                CommandContainer.builder()
                        .prefix("export")
                        .hidden(true)
                        .permission("quickshop.export")
                        .executor(new SubCommand_Export(plugin))
                        .build());
        registerCmd(CommandContainer.builder()
                .prefix("size")
                .permission("quickshop.create.stacks")
                .permission("quickshop.create.changeamount")
                .executor(new SubCommand_Size(plugin))
                .disabledSupplier(() -> !plugin.isAllowStack())
                .build());
        registerCmd(CommandContainer.builder()
                .prefix("item")
                .permission("quickshop.create.changeitem")
                .executor(new SubCommand_Item(plugin))
                .build());
        registerCmd(CommandContainer.builder()
                .prefix("removeall")
                .selectivePermission("quickshop.removeall.other")
                .selectivePermission("quickshop.removeall.self")
                .executor(new SubCommand_RemoveAll(plugin))
                .build());
        registerCmd(CommandContainer.builder()
                .prefix("name")
                .selectivePermission("quickshop.shopnaming")
                .selectivePermission("quickshop.other.shopnaming")
                .executor(new SubCommand_Name(plugin))
                .build());
        registerCmd(CommandContainer.builder()
                .prefix("removeworld")
                .permission("quickshop.removeworld")
                .executor(new SubCommand_RemoveWorld(plugin))
                .build());
        registerCmd(CommandContainer.builder()
                .prefix("currency")
                .permission("quickshop.currency")
                .executor(new SubCommand_Currency(plugin))
                .build());
        registerCmd(CommandContainer.builder()
                .prefix("taxaccount")
                .permission("quickshop.taxaccount")
                .executor(new SubCommand_TaxAccount(plugin))
                .build());
        registerCmd(CommandContainer.builder()
                .prefix("toggledisplay")
                .permission("quickshop.toggledisplay")
                .executor(new SubCommand_ToggleDisplay(plugin))
                .build());
        registerCmd(CommandContainer.builder()
                .prefix("purge")
                .permission("quickshop.purge")
                .disabledSupplier(() -> !plugin.getConfig().getBoolean("purge.enabled"))
                .executor(new SubCommand_Purge(plugin))
                .build());
        registerCmd(CommandContainer.builder()
                .prefix("permission")
                .permission("quickshop.permission")
                .executor(new SubCommand_Permission(plugin))
                .build());
        registerCmd(CommandContainer.builder()
                .prefix("lookup")
                .permission("quickshop.lookup")
                .executor(new SubCommand_Lookup(plugin))
                .build());
        registerCmd(CommandContainer.builder()
                .prefix("database")
                .permission("quickshop.database")
                .executor(new SubCommand_Database(plugin))
                .build());
        registerCmd(CommandContainer.builder()
                .prefix("benefit")
                .permission("quickshop.benefit")
                .executor(new SubCommand_Benefit(plugin))
                .build());

    }

    /**
     * Gets a list contains all registered commands
     *
     * @return All registered commands.
     */
    @Override
    @NotNull
    @Unmodifiable
    public List<CommandContainer> getRegisteredCommands() {
        return ImmutableList.copyOf(this.getCmds());
    }

    private void init(){
        this.playSoundOnCommand = plugin.getConfig().getBoolean("effect.sound.oncommand");
        this.playSoundOnTabComplete = plugin.getConfig().getBoolean("effect.sound.ontabcomplete");
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String commandLabel,
            @NotNull String[] cmdArg) {
        if (plugin.getBootError() != null) {
            if (cmdArg.length == 0) {
                plugin.getBootError().printErrors(sender);
                return true;
            }
            if (!"paste".equalsIgnoreCase(cmdArg[0])) {
                plugin.getBootError().printErrors(sender);
                return true;
            }
        }
        if (sender instanceof Player player && playSoundOnCommand) {
            ((Player) sender)
                    .playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 80.0F, 1.0F);
        }

        if (cmdArg.length == 0) {
            //Handle main command
            rootContainer.getExecutor().onCommand_Internal(capture(sender), commandLabel, EMPTY_ARGS);
        } else {
            //Handle subcommand
            String[] passThroughArgs = new String[cmdArg.length - 1];
            System.arraycopy(cmdArg, 1, passThroughArgs, 0, passThroughArgs.length);
            for (CommandContainer container : cmds) {
                if (!container.getPrefix().equalsIgnoreCase(cmdArg[0])) {
                    continue;
                }
                if (container.isDisabled() || (container.getDisabledSupplier() != null && container.getDisabledSupplier().get())) {
                    MsgUtil.sendDirectMessage(sender, container.getDisableText(sender));
                    return true;
                }
                if (!isAdapt(container, sender)) {
                    plugin.text().of(sender, "command-type-mismatch", container.getExecutorType().getSimpleName()).send();
                    return true;
                }
                List<String> requirePermissions = container.getPermissions();
                List<String> selectivePermissions = container.getSelectivePermissions();
                if (!checkPermissions(sender, commandLabel, passThroughArgs, requirePermissions, PermissionType.REQUIRE, Action.EXECUTE)) {
                    plugin.text().of(sender, "no-permission").send();
                    return true;
                }
                if (!checkPermissions(sender, commandLabel, passThroughArgs, selectivePermissions, PermissionType.SELECTIVE, Action.EXECUTE)) {
                    plugin.text().of(sender, "no-permission").send();
                    return true;
                }
                Log.debug("Execute container: " + container.getPrefix() + " - " + cmdArg[0]);
                try (PerfMonitor ignored = new PerfMonitor("Execute command " + container.getPrefix() + " " + CommonUtil.array2String(passThroughArgs), Duration.of(2, ChronoUnit.SECONDS))) {
                    container.getExecutor().onCommand_Internal(capture(sender), commandLabel, passThroughArgs);
                }
                return true;
            }
            rootContainer.getExecutor().onCommand_Internal(capture(sender), commandLabel, passThroughArgs);
        }
        return true;
    }

    /**
     * Method for capturing generic type
     */
    private <T1, T2 extends T1> T2 capture(T1 type) {
        return (T2) type;
    }

    private boolean isAdapt(CommandContainer container, CommandSender sender) {
        return container.getExecutorType().isInstance(sender);
    }

    private boolean checkPermissions(CommandSender sender, String commandLabel, String[] cmdArg, List<String> permissionList, PermissionType permissionType, Action action) {
        if (permissionList == null || permissionList.isEmpty()) {
            return true;
        }
        if (permissionType == PermissionType.REQUIRE) {
            for (String requirePermission : permissionList) {
                if (requirePermission != null
                        && !requirePermission.isEmpty()
                        && !plugin.perm().hasPermission(sender, requirePermission)) {
                    Log.debug(
                            "Sender "
                                    + sender.getName()
                                    + " trying " + action.getName() + " the command: "
                                    + commandLabel
                                    + " "
                                    + CommonUtil.array2String(cmdArg)
                                    + ", but no permission "
                                    + requirePermission);
                    return false;
                }
            }
            return true;
        } else {
            for (String selectivePermission : permissionList) {
                if (selectivePermission != null && !selectivePermission.isEmpty()) {
                    if (plugin.perm().hasPermission(sender, selectivePermission)) {
                        return true;
                    }
                }
            }
            if (Util.isDevMode()) {
                Log.debug(
                        "Sender "
                                + sender.getName()
                                + " trying " + action + " the command: "
                                + commandLabel
                                + " "
                                + CommonUtil.array2String(cmdArg)
                                + ", but does no have one of those permissions: "
                                + permissionList);
            }
            return false;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String commandLabel,
            @NotNull String[] cmdArg) {
        // No args, it shouldn't happened
        if (plugin.getBootError() != null) {
            return Collections.emptyList();
        }
        if (sender instanceof Player player && playSoundOnTabComplete) {
            ((Player) sender).playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 80.0F, 1.0F);
        }
        if (cmdArg.length <= 1) {
            return getRootContainer().getExecutor().onTabComplete_Internal(capture(sender), commandLabel, cmdArg);
        } else {
            // Tab-complete subcommand args
            String[] passThroughArgs = new String[cmdArg.length - 1];
            System.arraycopy(cmdArg, 1, passThroughArgs, 0, passThroughArgs.length);
            for (CommandContainer container : cmds) {
                if (!container.getPrefix().toLowerCase().startsWith(cmdArg[0])) {
                    continue;
                }
                if (!isAdapt(container, sender)) {
                    return Collections.emptyList();
                }
                List<String> requirePermissions = container.getPermissions();
                List<String> selectivePermissions = container.getSelectivePermissions();
                if (!checkPermissions(sender, commandLabel, passThroughArgs, requirePermissions, PermissionType.REQUIRE, Action.TAB_COMPLETE)) {
                    return Collections.emptyList();
                }
                if (!checkPermissions(sender, commandLabel, passThroughArgs, selectivePermissions, PermissionType.SELECTIVE, Action.TAB_COMPLETE)) {
                    return Collections.emptyList();
                }
                if (Util.isDevMode()) {
                    Log.debug("Tab-complete container: " + container.getPrefix());
                }
                return container.getExecutor().onTabComplete_Internal(capture(sender), commandLabel, passThroughArgs);

            }
            return Collections.emptyList();
        }
    }

    /**
     * This is a interface to allow addons to register the subcommand into quickshop command manager.
     *
     * @param container The command container to register
     * @throws IllegalStateException Will throw the error if register conflict.
     */
    @Override
    public void registerCmd(@NotNull CommandContainer container) {
        if (cmds.contains(container)) {
            Log.debug("Dupe subcommand registering: " + container);
            return;
        }
        container.bakeExecutorType();
        cmds.removeIf(commandContainer -> commandContainer.getPrefix().equalsIgnoreCase(container.getPrefix()));
        cmds.removeIf(container::equals);
        cmds.add(container);
        cmds.sort(Comparator.comparing(CommandContainer::getPrefix));
        Log.debug(Level.INFO, "Registered subcommand: " + container.getPrefix() + " - " + container.getExecutor().getClass().getName(), Log.Caller.create());
    }

    @Override
    public void unregisterCmd(@NotNull String prefix) {
        cmds.removeIf(commandContainer -> commandContainer.getPrefix().equalsIgnoreCase(prefix));
        Log.debug(Level.INFO, "Unregistered subcommand: " + prefix, Log.Caller.create());
    }

    /**
     * This is a interface to allow addons to unregister the registered/butil-in subcommand from command manager.
     *
     * @param container The command container to unregister
     */
    @Override
    public void unregisterCmd(@NotNull CommandContainer container) {
        cmds.remove(container);
    }

    @Override
    public @NotNull String genBody() {
        HTMLTable table = new HTMLTable(2);
        table.setTableTitle("Prefix", "Permissions", "Selective Permissions", "Executor Type", "Binding");
        for (CommandContainer cmd : this.cmds) {
            table.insert(cmd.getPrefix(), CommonUtil.list2String(cmd.getPermissions()), CommonUtil.list2String(cmd.getSelectivePermissions()), cmd.getExecutorType(), cmd.getExecutor().getClass().getName());
        }
        return table.render();
    }

    @Override
    public @NotNull String getTitle() {
        return "Command Manager";
    }

    private enum Action {
        EXECUTE("execute"),
        TAB_COMPLETE("tab-complete");
        final String name;

        Action(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private enum PermissionType {
        REQUIRE,
        SELECTIVE
    }

    @Override
    public ReloadResult reloadModule() throws Exception {
        init();
        return Reloadable.super.reloadModule();
    }
}
